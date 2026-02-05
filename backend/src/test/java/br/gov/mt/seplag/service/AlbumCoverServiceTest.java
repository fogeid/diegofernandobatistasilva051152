package br.gov.mt.seplag.service;

import br.gov.mt.seplag.dto.AlbumCoverResponse;
import br.gov.mt.seplag.entity.Album;
import br.gov.mt.seplag.entity.AlbumCover;
import br.gov.mt.seplag.exception.BadRequestException;
import br.gov.mt.seplag.exception.ResourceNotFoundException;
import br.gov.mt.seplag.repository.AlbumCoverRepository;
import br.gov.mt.seplag.repository.AlbumRepository;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlbumCoverService Tests")
class AlbumCoverServiceTest {

    @Mock
    private AlbumCoverRepository albumCoverRepository;

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private MinioClient minioInternalClient;

    @InjectMocks
    private AlbumCoverService service;

    private Album album;
    private AlbumCover cover;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(service, "bucketName", "albums");
        ReflectionTestUtils.setField(service, "minioPublicUrl", "http://localhost:9000");

        album = Album.builder()
                .id(10L)
                .title("Hybrid Theory")
                .build();

        cover = AlbumCover.builder()
                .id(1L)
                .album(album)
                .fileName("cover.jpg")
                .minioKey("albums/10/abc.jpg")
                .contentType("image/jpeg")
                .fileSize(1234L)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("findByAlbumId deve retornar lista de responses com imageUrl (public URL)")
    void findByAlbumId_shouldReturnResponses() {
        when(albumRepository.findById(10L)).thenReturn(Optional.of(album));
        when(albumCoverRepository.findByAlbumId(10L)).thenReturn(List.of(cover));

        List<AlbumCoverResponse> result = service.findByAlbumId(10L);

        assertThat(result).hasSize(1);
        AlbumCoverResponse r = result.get(0);

        assertThat(r.getId()).isEqualTo(1L);
        assertThat(r.getAlbumId()).isEqualTo(10L);
        assertThat(r.getFileName()).isEqualTo("cover.jpg");

        assertThat(r.getImageUrl()).isEqualTo("http://localhost:9000/albums/10/abc.jpg");

        verify(albumRepository).findById(10L);
        verify(albumCoverRepository).findByAlbumId(10L);
        verifyNoInteractions(minioInternalClient);
    }

    @Test
    @DisplayName("findByAlbumId deve lançar ResourceNotFound quando álbum não existir")
    void findByAlbumId_shouldThrowWhenAlbumNotFound() {
        when(albumRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByAlbumId(10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Álbum não encontrado");

        verify(albumRepository).findById(10L);
        verify(albumCoverRepository, never()).findByAlbumId(anyLong());
        verifyNoInteractions(minioInternalClient);
    }

    @Test
    @DisplayName("findById deve retornar response quando existir e pertencer ao álbum")
    void findById_shouldReturnResponse() {
        when(albumCoverRepository.findById(1L)).thenReturn(Optional.of(cover));

        AlbumCoverResponse r = service.findById(10L, 1L);

        assertThat(r.getId()).isEqualTo(1L);
        assertThat(r.getAlbumId()).isEqualTo(10L);
        assertThat(r.getImageUrl()).isEqualTo("http://localhost:9000/albums/10/abc.jpg");

        verify(albumCoverRepository).findById(1L);
        verifyNoInteractions(minioInternalClient);
    }

    @Test
    @DisplayName("findById deve lançar BadRequest quando capa não pertence ao álbum")
    void findById_albumCover_shouldThrowWhenNotBelongsToAlbum() {
        when(albumCoverRepository.findById(1L)).thenReturn(Optional.of(cover));

        assertThatThrownBy(() -> service.findById(999L, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("não pertence ao álbum");

        verify(albumCoverRepository).findById(1L);
        verifyNoInteractions(minioInternalClient);
    }

    @Test
    @DisplayName("findById deve lançar ResourceNotFound quando não existir")
    void findById_shouldThrowWhenNotFound() {
        when(albumCoverRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(10L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Capa não encontrada");

        verify(albumCoverRepository).findById(1L);
        verifyNoInteractions(minioInternalClient);
    }

    @Test
    @DisplayName("uploadCover deve fazer upload, salvar e notificar")
    void uploadCover_shouldUploadSaveAndNotify() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cover.jpg",
                "image/jpeg",
                "fake-image-content".getBytes()
        );

        when(albumRepository.findById(10L)).thenReturn(Optional.of(album));

        when(albumCoverRepository.save(any(AlbumCover.class))).thenAnswer(inv -> {
            AlbumCover c = inv.getArgument(0, AlbumCover.class);
            return AlbumCover.builder()
                    .id(99L)
                    .album(c.getAlbum())
                    .fileName(c.getFileName())
                    .minioKey(c.getMinioKey())
                    .contentType(c.getContentType())
                    .fileSize(c.getFileSize())
                    .createdAt(LocalDateTime.now())
                    .build();
        });

        AlbumCoverResponse r = service.uploadCover(10L, file, "diego");

        ArgumentCaptor<PutObjectArgs> putCaptor = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioInternalClient).putObject(putCaptor.capture());
        PutObjectArgs putArgs = putCaptor.getValue();

        assertThat(putArgs.bucket()).isEqualTo("albums");
        assertThat(putArgs.object()).startsWith("10/");
        assertThat(putArgs.object()).endsWith(".jpg");

        // Repository save recebeu uma entidade com os metadados esperados
        ArgumentCaptor<AlbumCover> coverCaptor = ArgumentCaptor.forClass(AlbumCover.class);
        verify(albumCoverRepository).save(coverCaptor.capture());
        AlbumCover savedEntity = coverCaptor.getValue();

        assertThat(savedEntity.getAlbum().getId()).isEqualTo(10L);
        assertThat(savedEntity.getFileName()).isEqualTo("cover.jpg");
        assertThat(savedEntity.getContentType()).isEqualTo("image/jpeg");
        assertThat(savedEntity.getFileSize()).isEqualTo(file.getSize());
        assertThat(savedEntity.getMinioKey()).startsWith("10/");
        assertThat(savedEntity.getMinioKey()).endsWith(".jpg");

        assertThat(r.getId()).isEqualTo(99L);
        assertThat(r.getAlbumId()).isEqualTo(10L);
        assertThat(r.getImageUrl()).startsWith("http://localhost:9000/albums/10/");
        assertThat(r.getImageUrl()).endsWith(".jpg");

        verify(notificationService).notifyCoverUploaded(10L, "Hybrid Theory", "diego");
    }

    @Test
    @DisplayName("uploadCover deve lançar ResourceNotFound quando álbum não existir")
    void uploadCover_shouldThrowWhenAlbumNotFound() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cover.jpg",
                "image/jpeg",
                "x".getBytes()
        );

        when(albumRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.uploadCover(10L, file, "diego"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Álbum não encontrado");

        verify(albumRepository).findById(10L);
        verifyNoInteractions(minioInternalClient);
        verifyNoInteractions(notificationService);
        verify(albumCoverRepository, never()).save(any());
    }

    @Test
    @DisplayName("uploadCover deve lançar RuntimeException quando MinIO falhar")
    void uploadCover_shouldThrowWhenMinioFails() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cover.jpg",
                "image/jpeg",
                "fake-image-content".getBytes()
        );

        when(albumRepository.findById(10L)).thenReturn(Optional.of(album));
        doThrow(new RuntimeException("boom")).when(minioInternalClient).putObject(any(PutObjectArgs.class));

        assertThatThrownBy(() -> service.uploadCover(10L, file, "diego"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erro ao fazer upload da imagem");

        verify(minioInternalClient).putObject(any(PutObjectArgs.class));
        verify(albumCoverRepository, never()).save(any());
        verify(notificationService, never()).notifyCoverUploaded(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("delete(albumId, coverId) deve remover no MinIO e deletar do banco quando ok")
    void delete_albumCover_shouldRemoveAndDelete() throws Exception {
        when(albumCoverRepository.findById(1L)).thenReturn(Optional.of(cover));

        service.delete(10L, 1L);

        ArgumentCaptor<RemoveObjectArgs> removeCaptor = ArgumentCaptor.forClass(RemoveObjectArgs.class);
        verify(minioInternalClient).removeObject(removeCaptor.capture());

        RemoveObjectArgs removeArgs = removeCaptor.getValue();
        assertThat(removeArgs.bucket()).isEqualTo("albums");
        assertThat(removeArgs.object()).isEqualTo("10/abc.jpg");

        verify(albumCoverRepository).delete(cover);
    }

    @Test
    @DisplayName("delete deve lançar ResourceNotFound quando capa não existir")
    void delete_albumCover_shouldThrowWhenNotFound() {
        when(albumCoverRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(10L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Capa não encontrada");

        verify(albumCoverRepository).findById(1L);
        verifyNoInteractions(minioInternalClient);
        verify(albumCoverRepository, never()).delete(any());
    }

    @Test
    @DisplayName("delete deve lançar BadRequest quando capa não pertence ao álbum")
    void delete_albumCover_shouldThrowWhenNotBelongsToAlbum() {
        when(albumCoverRepository.findById(1L)).thenReturn(Optional.of(cover));

        assertThatThrownBy(() -> service.delete(999L, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("não pertence ao álbum");

        verify(albumCoverRepository).findById(1L);
        verifyNoInteractions(minioInternalClient);
        verify(albumCoverRepository, never()).delete(any());
    }

    @Test
    @DisplayName("delete deve lançar RuntimeException quando MinIO falhar")
    void delete_albumCover_shouldThrowWhenMinioFails() throws Exception {
        when(albumCoverRepository.findById(1L)).thenReturn(Optional.of(cover));
        doThrow(new RuntimeException("boom")).when(minioInternalClient).removeObject(any(RemoveObjectArgs.class));

        assertThatThrownBy(() -> service.delete(10L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erro ao deletar imagem");

        verify(minioInternalClient).removeObject(any(RemoveObjectArgs.class));
        verify(albumCoverRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteByAlbumId deve deletar todas as capas")
    void deleteByAlbumId_shouldDeleteAllCovers() throws Exception {
        AlbumCover cover2 = AlbumCover.builder()
                .id(2L)
                .album(album)
                .fileName("c2.png")
                .minioKey("/albums/10/def.png")
                .contentType("image/png")
                .fileSize(10L)
                .createdAt(LocalDateTime.now())
                .build();

        when(albumCoverRepository.findByAlbumId(10L)).thenReturn(List.of(cover, cover2));

        service.deleteByAlbumId(10L);

        verify(minioInternalClient, times(2)).removeObject(any(RemoveObjectArgs.class));
        verify(albumCoverRepository).delete(cover);
        verify(albumCoverRepository).delete(cover2);

        ArgumentCaptor<RemoveObjectArgs> removeCaptor = ArgumentCaptor.forClass(RemoveObjectArgs.class);
        verify(minioInternalClient, times(2)).removeObject(removeCaptor.capture());

        List<RemoveObjectArgs> all = removeCaptor.getAllValues();
        assertThat(all).extracting(RemoveObjectArgs::object)
                .containsExactlyInAnyOrder("10/abc.jpg", "10/def.png");
    }

    @Nested
    @DisplayName("Validações de arquivo")
    class FileValidationTests {

        @Test
        @DisplayName("Deve lançar se arquivo for null")
        void shouldThrowIfFileNull() {
            when(albumRepository.findById(10L)).thenReturn(Optional.of(album));

            assertThatThrownBy(() -> service.uploadCover(10L, null, "diego"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Arquivo não pode ser vazio");

            verifyNoInteractions(minioInternalClient);
            verify(albumCoverRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar se arquivo estiver vazio")
        void shouldThrowIfFileEmpty() {
            when(albumRepository.findById(10L)).thenReturn(Optional.of(album));

            MockMultipartFile empty = new MockMultipartFile(
                    "file", "cover.jpg", "image/jpeg", new byte[0]
            );

            assertThatThrownBy(() -> service.uploadCover(10L, empty, "diego"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Arquivo não pode ser vazio");

            verifyNoInteractions(minioInternalClient);
            verify(albumCoverRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar se contentType não for imagem")
        void shouldThrowIfNotImageContentType() {
            when(albumRepository.findById(10L)).thenReturn(Optional.of(album));

            MockMultipartFile file = new MockMultipartFile(
                    "file", "cover.jpg", "text/plain", "x".getBytes()
            );

            assertThatThrownBy(() -> service.uploadCover(10L, file, "diego"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("deve ser uma imagem");

            verifyNoInteractions(minioInternalClient);
            verify(albumCoverRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar se filename for null")
        void shouldThrowIfFilenameNull() {
            when(albumRepository.findById(10L)).thenReturn(Optional.of(album));

            MockMultipartFile file = new MockMultipartFile(
                    "file", null, "image/jpeg", "x".getBytes()
            );

            assertThatThrownBy(() -> service.uploadCover(10L, file, "diego"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Formato de imagem não suportado");

            verifyNoInteractions(minioInternalClient);
            verify(albumCoverRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar se extensão não suportada")
        void shouldThrowIfUnsupportedExtension() {
            when(albumRepository.findById(10L)).thenReturn(Optional.of(album));

            MockMultipartFile file = new MockMultipartFile(
                    "file", "cover.bmp", "image/bmp", "x".getBytes()
            );

            assertThatThrownBy(() -> service.uploadCover(10L, file, "diego"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Formato de imagem não suportado");

            verifyNoInteractions(minioInternalClient);
            verify(albumCoverRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar se arquivo for maior que 10MB")
        void shouldThrowIfTooLarge() {
            when(albumRepository.findById(10L)).thenReturn(Optional.of(album));

            byte[] big = new byte[(int) (10L * 1024 * 1024) + 1];
            MockMultipartFile file = new MockMultipartFile(
                    "file", "cover.jpg", "image/jpeg", big
            );

            assertThatThrownBy(() -> service.uploadCover(10L, file, "diego"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Imagem muito grande");

            verifyNoInteractions(minioInternalClient);
            verify(albumCoverRepository, never()).save(any());
        }
    }
}

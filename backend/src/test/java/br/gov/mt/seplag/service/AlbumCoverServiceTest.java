package br.gov.mt.seplag.service;

import br.gov.mt.seplag.dto.AlbumCoverResponse;
import br.gov.mt.seplag.entity.Album;
import br.gov.mt.seplag.entity.AlbumCover;
import br.gov.mt.seplag.exception.BadRequestException;
import br.gov.mt.seplag.exception.ResourceNotFoundException;
import br.gov.mt.seplag.repository.AlbumCoverRepository;
import br.gov.mt.seplag.repository.AlbumRepository;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

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

    @Mock
    private MinioClient minioPublicClient;

    @InjectMocks
    private AlbumCoverService service;

    private Album album;
    private AlbumCover cover;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(service, "bucketName", "albums");
        ReflectionTestUtils.setField(service, "presignedUrlExpiration", 1800);

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

    private void mockPresignedUrl(String url) throws Exception {
        when(minioPublicClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn(url);
    }

    @Test
    @DisplayName("findByAlbumId deve retornar lista de responses com imageUrl")
    void findByAlbumId_shouldReturnResponses() throws Exception {
        mockPresignedUrl("http://signed-url");

        when(albumRepository.findById(10L)).thenReturn(Optional.of(album));
        when(albumCoverRepository.findByAlbumId(10L)).thenReturn(List.of(cover));

        List<AlbumCoverResponse> result = service.findByAlbumId(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getAlbumId()).isEqualTo(10L);
        assertThat(result.get(0).getFileName()).isEqualTo("cover.jpg");
        assertThat(result.get(0).getImageUrl()).isEqualTo("http://signed-url");

        verify(albumRepository).findById(10L);
        verify(albumCoverRepository).findByAlbumId(10L);
        verify(minioPublicClient).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
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
        verifyNoInteractions(minioPublicClient);
        verifyNoInteractions(minioInternalClient);
    }

    @Test
    @DisplayName("findById (id) deve retornar response quando existir")
    void findById_shouldReturnResponse() throws Exception {
        mockPresignedUrl("http://signed-url");

        when(albumCoverRepository.findById(1L)).thenReturn(Optional.of(cover));

        AlbumCoverResponse result = service.findById(1L, 1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getImageUrl()).isEqualTo("http://signed-url");

        verify(albumCoverRepository).findById(1L);
        verify(minioPublicClient).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
        verifyNoInteractions(minioInternalClient);
    }

    @Test
    @DisplayName("findById (albumId, coverId) deve retornar response quando capa pertence ao álbum")
    void findById_albumCover_shouldReturnResponse() throws Exception {
        mockPresignedUrl("http://signed-url");

        when(albumCoverRepository.findById(1L)).thenReturn(Optional.of(cover));

        AlbumCoverResponse result = service.findById(10L, 1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAlbumId()).isEqualTo(10L);
        assertThat(result.getImageUrl()).isEqualTo("http://signed-url");

        verify(albumCoverRepository).findById(1L);
        verify(minioPublicClient).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
        verifyNoInteractions(minioInternalClient);
    }

    @Test
    @DisplayName("findById (albumId, coverId) deve lançar BadRequest se capa não pertence ao álbum")
    void findById_albumCover_shouldThrowWhenNotBelongsToAlbum() {
        Album otherAlbum = Album.builder().id(999L).title("Other").build();
        AlbumCover otherCover = AlbumCover.builder().id(1L).album(otherAlbum).minioKey("k").build();

        when(albumCoverRepository.findById(1L)).thenReturn(Optional.of(otherCover));

        assertThatThrownBy(() -> service.findById(10L, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("não pertence ao álbum");

        verify(albumCoverRepository).findById(1L);
        verifyNoInteractions(minioPublicClient);
        verifyNoInteractions(minioInternalClient);
    }

    @Test
    @DisplayName("findById deve lançar ResourceNotFound quando não existir")
    void findById_shouldThrowWhenNotFound() {
        when(albumCoverRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(1L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Capa não encontrada");

        verify(albumCoverRepository).findById(1L);
        verifyNoInteractions(minioPublicClient);
        verifyNoInteractions(minioInternalClient);
    }

    @Test
    @DisplayName("uploadCover deve subir no MinIO, salvar e notificar")
    void uploadCover_shouldUploadSaveAndNotify() throws Exception {
        mockPresignedUrl("http://signed-url");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cover.jpg",
                "image/jpeg",
                "fake-image-content".getBytes()
        );

        when(albumRepository.findById(10L)).thenReturn(Optional.of(album));

        when(minioInternalClient.putObject(any(PutObjectArgs.class)))
                .thenReturn(mock(ObjectWriteResponse.class));

        when(albumCoverRepository.save(any(AlbumCover.class))).thenAnswer(inv -> {
            AlbumCover c = inv.getArgument(0);
            c.setId(99L);
            c.setCreatedAt(LocalDateTime.now());
            return c;
        });

        AlbumCoverResponse response = service.uploadCover(10L, file, "diego");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(99L);
        assertThat(response.getAlbumId()).isEqualTo(10L);
        assertThat(response.getFileName()).isEqualTo("cover.jpg");
        assertThat(response.getImageUrl()).isEqualTo("http://signed-url");
        assertThat(response.getContentType()).isEqualTo("image/jpeg");
        assertThat(response.getFileSize()).isEqualTo(file.getSize());

        verify(albumRepository).findById(10L);
        verify(minioInternalClient).putObject(any(PutObjectArgs.class));
        verify(albumCoverRepository).save(any(AlbumCover.class));
        verify(notificationService).notifyCoverUploaded(10L, "Hybrid Theory", "diego");
        verify(minioPublicClient).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
    }

    @Test
    @DisplayName("uploadCover deve lançar ResourceNotFound se álbum não existir")
    void uploadCover_shouldThrowWhenAlbumNotFound() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.jpg", "image/jpeg", "x".getBytes()
        );

        when(albumRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.uploadCover(10L, file, "diego"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Álbum não encontrado");

        verify(albumRepository).findById(10L);
        verifyNoInteractions(minioInternalClient);
        verifyNoInteractions(minioPublicClient);
        verify(albumCoverRepository, never()).save(any());
        verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("uploadCover deve lançar RuntimeException se MinIO falhar")
    void uploadCover_shouldThrowWhenMinioFails() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.jpg", "image/jpeg", "x".getBytes()
        );

        when(albumRepository.findById(10L)).thenReturn(Optional.of(album));
        doThrow(new RuntimeException("minio down")).when(minioInternalClient).putObject(any(PutObjectArgs.class));

        assertThatThrownBy(() -> service.uploadCover(10L, file, "diego"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erro ao fazer upload da imagem");

        verify(minioInternalClient).putObject(any(PutObjectArgs.class));
        verify(albumCoverRepository, never()).save(any());
        verifyNoInteractions(notificationService);
        verifyNoInteractions(minioPublicClient);
    }

    @Nested
    @DisplayName("Validações de arquivo")
    class FileValidationTests {

        @BeforeEach
        void albumExists() {
            when(albumRepository.findById(10L)).thenReturn(Optional.of(album));
        }

        @Test
        @DisplayName("Deve lançar BadRequest se file for null")
        void shouldThrowIfFileNull() {
            assertThatThrownBy(() -> service.uploadCover(10L, null, "diego"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Arquivo não pode ser vazio");

            verifyNoInteractions(minioInternalClient);
            verifyNoInteractions(minioPublicClient);
        }

        @Test
        @DisplayName("Deve lançar BadRequest se file estiver vazio")
        void shouldThrowIfFileEmpty() {
            MultipartFile empty = new MockMultipartFile("file", "cover.jpg", "image/jpeg", new byte[0]);

            assertThatThrownBy(() -> service.uploadCover(10L, empty, "diego"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Arquivo não pode ser vazio");

            verifyNoInteractions(minioInternalClient);
            verifyNoInteractions(minioPublicClient);
        }

        @Test
        @DisplayName("Deve lançar BadRequest se contentType não for image/*")
        void shouldThrowIfNotImageContentType() {
            MultipartFile file = new MockMultipartFile("file", "cover.jpg", "application/pdf", "x".getBytes());

            assertThatThrownBy(() -> service.uploadCover(10L, file, "diego"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Arquivo deve ser uma imagem");

            verifyNoInteractions(minioInternalClient);
            verifyNoInteractions(minioPublicClient);
        }

        @Test
        @DisplayName("Deve lançar BadRequest se extensão não for suportada")
        void shouldThrowIfUnsupportedExtension() {
            MultipartFile file = new MockMultipartFile("file", "cover.bmp", "image/bmp", "x".getBytes());

            assertThatThrownBy(() -> service.uploadCover(10L, file, "diego"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Formato de imagem não suportado");

            verifyNoInteractions(minioInternalClient);
            verifyNoInteractions(minioPublicClient);
        }

        @Test
        @DisplayName("Deve lançar BadRequest se filename for null")
        void shouldThrowIfFilenameNull() {
            MultipartFile file = new MockMultipartFile("file", null, "image/jpeg", "x".getBytes());

            assertThatThrownBy(() -> service.uploadCover(10L, file, "diego"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Formato de imagem não suportado");

            verifyNoInteractions(minioInternalClient);
            verifyNoInteractions(minioPublicClient);
        }

        @Test
        @DisplayName("Deve lançar BadRequest se tamanho for maior que 10MB")
        void shouldThrowIfTooLarge() {
            MultipartFile bigFile = mock(MultipartFile.class);
            when(bigFile.isEmpty()).thenReturn(false);
            when(bigFile.getContentType()).thenReturn("image/jpeg");
            when(bigFile.getOriginalFilename()).thenReturn("cover.jpg");
            when(bigFile.getSize()).thenReturn(10L * 1024 * 1024 + 1); // > 10MB

            assertThatThrownBy(() -> service.uploadCover(10L, bigFile, "diego"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Imagem muito grande");

            verifyNoInteractions(minioInternalClient);
            verifyNoInteractions(minioPublicClient);
        }
    }

    @Test
    @DisplayName("delete(albumId, coverId) deve remover do MinIO e deletar do banco quando pertencer ao álbum")
    void delete_albumCover_shouldRemoveAndDelete() throws Exception {
        when(albumCoverRepository.findById(1L)).thenReturn(Optional.of(cover));
        doNothing().when(minioInternalClient).removeObject(any(RemoveObjectArgs.class));
        doNothing().when(albumCoverRepository).delete(cover);

        service.delete(10L, 1L);

        verify(albumCoverRepository).findById(1L);
        verify(minioInternalClient).removeObject(any(RemoveObjectArgs.class));
        verify(albumCoverRepository).delete(cover);
        verifyNoInteractions(minioPublicClient);
    }

    @Test
    @DisplayName("delete(albumId, coverId) deve lançar BadRequest se não pertencer ao álbum")
    void delete_albumCover_shouldThrowWhenNotBelongsToAlbum() {
        Album otherAlbum = Album.builder().id(999L).title("Other").build();
        AlbumCover otherCover = AlbumCover.builder().id(1L).album(otherAlbum).minioKey("k").build();

        when(albumCoverRepository.findById(1L)).thenReturn(Optional.of(otherCover));

        assertThatThrownBy(() -> service.delete(10L, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("não pertence ao álbum");

        verify(albumCoverRepository).findById(1L);
        verifyNoInteractions(minioInternalClient);
        verifyNoInteractions(minioPublicClient);
        verify(albumCoverRepository, never()).delete(any());
    }

    @Test
    @DisplayName("delete(albumId, coverId) deve lançar ResourceNotFound se não existir")
    void delete_albumCover_shouldThrowWhenNotFound() {
        when(albumCoverRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(10L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Capa não encontrada");

        verify(albumCoverRepository).findById(1L);
        verifyNoInteractions(minioInternalClient);
        verifyNoInteractions(minioPublicClient);
    }

    @Test
    @DisplayName("delete(albumId, coverId) deve lançar RuntimeException se MinIO falhar")
    void delete_albumCover_shouldThrowWhenMinioFails() throws Exception {
        when(albumCoverRepository.findById(1L)).thenReturn(Optional.of(cover));
        doThrow(new RuntimeException("minio fail")).when(minioInternalClient).removeObject(any(RemoveObjectArgs.class));

        assertThatThrownBy(() -> service.delete(10L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erro ao deletar imagem");

        verify(albumCoverRepository).findById(1L);
        verify(minioInternalClient).removeObject(any(RemoveObjectArgs.class));
        verify(albumCoverRepository, never()).delete(any());
        verifyNoInteractions(minioPublicClient);
    }

    @Test
    @DisplayName("deleteByAlbumId deve deletar todas as capas do álbum")
    void deleteByAlbumId_shouldDeleteAllCovers() throws Exception {
        AlbumCover c1 = AlbumCover.builder().id(1L).album(album).minioKey("k1").build();
        AlbumCover c2 = AlbumCover.builder().id(2L).album(album).minioKey("k2").build();

        when(albumCoverRepository.findByAlbumId(10L)).thenReturn(List.of(c1, c2));
        doNothing().when(minioInternalClient).removeObject(any(RemoveObjectArgs.class));

        service.deleteByAlbumId(10L);

        verify(albumCoverRepository).findByAlbumId(10L);
        verify(minioInternalClient, times(2)).removeObject(any(RemoveObjectArgs.class));
        verify(albumCoverRepository, times(2)).delete(any(AlbumCover.class));
        verifyNoInteractions(minioPublicClient);
    }

    @Test
    @DisplayName("toResponse deve lançar RuntimeException se falhar ao gerar URL")
    void toResponse_shouldThrowIfPresignedUrlFails() throws Exception {
        when(minioPublicClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenThrow(new RuntimeException("boom"));

        when(albumCoverRepository.findById(1L)).thenReturn(Optional.of(cover));

        assertThatThrownBy(() -> service.findById(1L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erro ao gerar URL de acesso à imagem");

        verify(albumCoverRepository).findById(1L);
        verify(minioPublicClient).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
        verifyNoInteractions(minioInternalClient);
    }
}

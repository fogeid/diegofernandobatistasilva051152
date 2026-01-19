package br.gov.mt.seplag.service;

import br.gov.mt.seplag.dto.AlbumRequest;
import br.gov.mt.seplag.dto.AlbumResponse;
import br.gov.mt.seplag.entity.Album;
import br.gov.mt.seplag.entity.Artist;
import br.gov.mt.seplag.exception.ResourceNotFoundException;
import br.gov.mt.seplag.repository.AlbumRepository;
import br.gov.mt.seplag.repository.ArtistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlbumService Tests")
class AlbumServiceTest {

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AlbumService albumService;

    private Album album;
    private Artist artist;
    private AlbumRequest albumRequest;

    @BeforeEach
    void setUp() {
        artist = Artist.builder()
                .id(1L)
                .name("Test Artist")
                .isBand(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .albums(new HashSet<>())
                .build();

        album = Album.builder()
                .id(1L)
                .title("Test Album")
                .releaseYear(2024)
                .artists(new HashSet<>(Set.of(artist)))
                .covers(new HashSet<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        albumRequest = AlbumRequest.builder()
                .title("Test Album")
                .releaseYear(2024)
                .artistIds(Set.of(1L))
                .build();
    }

    @Test
    @DisplayName("Deve criar álbum com sucesso usando insert()")
    void shouldInsertAlbumSuccessfully() {
        // Given
        String username = "admin";
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(albumRepository.saveAndFlush(any(Album.class))).thenReturn(album);
        doNothing().when(notificationService).notifyAlbumCreated(anyLong(), anyString(), anyString());

        // When
        AlbumResponse response = albumService.insert(albumRequest, username);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Test Album");
        assertThat(response.getReleaseYear()).isEqualTo(2024);
        assertThat(response.getArtists()).hasSize(1);

        verify(artistRepository).findById(1L);
        verify(albumRepository, times(2)).saveAndFlush(any(Album.class));
        verify(notificationService).notifyAlbumCreated(
                eq(album.getId()),
                eq(album.getTitle()),
                eq(username)
        );
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao criar álbum com artista inexistente")
    void shouldThrowResourceNotFoundWhenInsertingWithNonExistentArtist() {
        // Given
        String username = "admin";
        when(artistRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> albumService.insert(albumRequest, username))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Artista não encontrado");

        verify(albumRepository, never()).saveAndFlush(any(Album.class));
        verify(notificationService, never()).notifyAlbumCreated(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve buscar álbum por ID")
    void shouldFindAlbumById() {
        // Given
        when(albumRepository.findById(1L)).thenReturn(Optional.of(album));

        // When
        AlbumResponse response = albumService.findById(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Test Album");

        verify(albumRepository).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao buscar álbum inexistente")
    void shouldThrowResourceNotFoundWhenAlbumNotFound() {
        // Given
        when(albumRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> albumService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Álbum não encontrado");
    }

    @Test
    @DisplayName("Deve listar todos os álbuns")
    void shouldListAllAlbums() {
        // Given
        List<Album> albums = Arrays.asList(album);
        when(albumRepository.findAll()).thenReturn(albums);

        // When
        List<AlbumResponse> responses = albumService.findAll();

        // Then
        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getTitle()).isEqualTo("Test Album");

        verify(albumRepository).findAll();
    }

    @Test
    @DisplayName("Deve atualizar álbum com sucesso e notificar")
    void shouldUpdateAlbumSuccessfully() {
        // Given
        String username = "admin";
        AlbumRequest updateRequest = AlbumRequest.builder()
                .title("Updated Album")
                .releaseYear(2025)
                .artistIds(Set.of(1L))
                .build();

        when(albumRepository.findById(1L)).thenReturn(Optional.of(album));
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(albumRepository.save(any(Album.class))).thenReturn(album);
        doNothing().when(notificationService).notifyAlbumUpdated(anyLong(), anyString(), anyString());

        // When
        AlbumResponse response = albumService.update(1L, updateRequest, username);

        // Then
        assertThat(response).isNotNull();
        verify(albumRepository).findById(1L);
        verify(albumRepository).save(any(Album.class));
        verify(notificationService).notifyAlbumUpdated(
                eq(album.getId()),
                anyString(),
                eq(username)
        );
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao atualizar álbum inexistente")
    void shouldThrowResourceNotFoundWhenUpdatingNonExistentAlbum() {
        // Given
        String username = "admin";
        when(albumRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> albumService.update(999L, albumRequest, username))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Álbum não encontrado");

        verify(albumRepository, never()).save(any(Album.class));
    }

    @Test
    @DisplayName("Deve deletar álbum com sucesso e notificar")
    void shouldDeleteAlbumSuccessfully() {
        // Given
        String username = "admin";
        when(albumRepository.findById(1L)).thenReturn(Optional.of(album));
        doNothing().when(albumRepository).delete(any(Album.class));
        doNothing().when(notificationService).notifyAlbumDeleted(anyLong(), anyString(), anyString());

        // When
        albumService.delete(1L, username);

        // Then
        verify(albumRepository).findById(1L);
        verify(albumRepository).delete(album);
        verify(notificationService).notifyAlbumDeleted(
                eq(album.getId()),
                eq(album.getTitle()),
                eq(username)
        );
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao deletar álbum inexistente")
    void shouldThrowResourceNotFoundWhenDeletingNonExistentAlbum() {
        // Given
        String username = "admin";
        when(albumRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> albumService.delete(999L, username))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Álbum não encontrado");

        verify(albumRepository, never()).delete(any(Album.class));
        verify(notificationService, never()).notifyAlbumDeleted(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve buscar álbuns por título")
    void shouldSearchAlbumsByTitle() {
        // Given
        when(albumRepository.findByTitleContainingIgnoreCase("Test"))
                .thenReturn(Arrays.asList(album));

        // When
        List<AlbumResponse> responses = albumService.searchByTitle("Test");

        // Then
        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getTitle()).contains("Test");

        verify(albumRepository).findByTitleContainingIgnoreCase("Test");
    }

    @Test
    @DisplayName("Deve buscar álbuns por ano")
    void shouldFindAlbumsByYear() {
        // Given
        when(albumRepository.findByReleaseYear(2024)).thenReturn(Arrays.asList(album));

        // When
        List<AlbumResponse> responses = albumService.findByYear(2024);

        // Then
        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getReleaseYear()).isEqualTo(2024);

        verify(albumRepository).findByReleaseYear(2024);
    }

    @Test
    @DisplayName("Deve carregar múltiplos artistas ao criar álbum")
    void shouldLoadMultipleArtistsWhenCreatingAlbum() {
        // Given
        String username = "admin";
        Artist artist2 = Artist.builder()
                .id(2L)
                .name("Artist 2")
                .isBand(true)
                .build();

        AlbumRequest multiArtistRequest = AlbumRequest.builder()
                .title("Collab Album")
                .releaseYear(2024)
                .artistIds(Set.of(1L, 2L))
                .build();

        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(artistRepository.findById(2L)).thenReturn(Optional.of(artist2));
        when(albumRepository.saveAndFlush(any(Album.class))).thenReturn(album);

        // When
        AlbumResponse response = albumService.insert(multiArtistRequest, username);

        // Then
        assertThat(response).isNotNull();
        verify(artistRepository).findById(1L);
        verify(artistRepository).findById(2L);
    }
}
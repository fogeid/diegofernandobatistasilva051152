package br.gov.mt.seplag.service;

import br.gov.mt.seplag.dto.ArtistRequest;
import br.gov.mt.seplag.dto.ArtistResponse;
import br.gov.mt.seplag.entity.Artist;
import br.gov.mt.seplag.exception.ResourceNotFoundException;
import br.gov.mt.seplag.repository.ArtistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ArtistService Tests")
class ArtistServiceTest {

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ArtistService artistService;

    private Artist artist;
    private ArtistRequest artistRequest;

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

        artistRequest = ArtistRequest.builder()
                .name("Test Artist")
                .isBand(false)
                .build();
    }

    @Test
    @DisplayName("Deve criar artista com sucesso usando insert() e notificar")
    void shouldInsertArtistSuccessfully() {
        // Given
        String username = "admin";
        when(artistRepository.save(any(Artist.class))).thenReturn(artist);
        doNothing().when(notificationService).notifyArtistCreated(anyLong(), anyString(), anyString());

        // When
        ArtistResponse response = artistService.insert(artistRequest, username);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Test Artist");
        assertThat(response.getIsBand()).isFalse();

        verify(artistRepository).save(any(Artist.class));
        verify(notificationService).notifyArtistCreated(
                eq(artist.getId()),
                eq(artist.getName()),
                eq(username)
        );
    }

    @Test
    @DisplayName("Deve criar banda corretamente (isBand = true)")
    void shouldInsertBandCorrectly() {
        // Given
        String username = "admin";
        ArtistRequest bandRequest = ArtistRequest.builder()
                .name("Test Band")
                .isBand(true)
                .build();

        Artist band = Artist.builder()
                .id(2L)
                .name("Test Band")
                .isBand(true)
                .build();

        when(artistRepository.save(any(Artist.class))).thenReturn(band);

        // When
        ArtistResponse response = artistService.insert(bandRequest, username);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getIsBand()).isTrue();

        verify(artistRepository).save(argThat(a -> Boolean.TRUE.equals(a.getIsBand())));
    }

    @Test
    @DisplayName("Deve tratar isBand null como false")
    void shouldTreatNullIsBandAsFalse() {
        // Given
        String username = "admin";
        ArtistRequest requestWithNullBand = ArtistRequest.builder()
                .name("Test Artist")
                .isBand(null)
                .build();

        when(artistRepository.save(any(Artist.class))).thenReturn(artist);

        // When
        ArtistResponse response = artistService.insert(requestWithNullBand, username);

        // Then
        verify(artistRepository).save(argThat(a -> Boolean.FALSE.equals(a.getIsBand())));
    }

    @Test
    @DisplayName("Deve buscar artista por ID")
    void shouldFindArtistById() {
        // Given
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));

        // When
        ArtistResponse response = artistService.findById(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Test Artist");

        verify(artistRepository).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao buscar artista inexistente")
    void shouldThrowResourceNotFoundWhenArtistNotFound() {
        // Given
        when(artistRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> artistService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Artista não encontrado");
    }

    @Test
    @DisplayName("Deve listar todos os artistas")
    void shouldListAllArtists() {
        // Given
        List<Artist> artists = Arrays.asList(artist);
        when(artistRepository.findAll()).thenReturn(artists);

        // When
        List<ArtistResponse> responses = artistService.findAll();

        // Then
        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getName()).isEqualTo("Test Artist");

        verify(artistRepository).findAll();
    }

    @Test
    @DisplayName("Deve atualizar artista com sucesso")
    void shouldUpdateArtistSuccessfully() {
        // Given
        ArtistRequest updateRequest = ArtistRequest.builder()
                .name("Updated Artist")
                .isBand(true)
                .build();

        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(artistRepository.save(any(Artist.class))).thenReturn(artist);

        // When
        ArtistResponse response = artistService.update(1L, updateRequest);

        // Then
        assertThat(response).isNotNull();
        verify(artistRepository).findById(1L);
        verify(artistRepository).save(any(Artist.class));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao atualizar artista inexistente")
    void shouldThrowResourceNotFoundWhenUpdatingNonExistentArtist() {
        // Given
        when(artistRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> artistService.update(999L, artistRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Artista não encontrado");

        verify(artistRepository, never()).save(any(Artist.class));
    }

    @Test
    @DisplayName("Deve deletar artista com sucesso")
    void shouldDeleteArtistSuccessfully() {
        // Given
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        doNothing().when(artistRepository).delete(any(Artist.class));

        // When
        artistService.delete(1L);

        // Then
        verify(artistRepository).findById(1L);
        verify(artistRepository).delete(artist);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao deletar artista inexistente")
    void shouldThrowResourceNotFoundWhenDeletingNonExistentArtist() {
        // Given
        when(artistRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> artistService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Artista não encontrado");

        verify(artistRepository, never()).delete(any(Artist.class));
    }

    @Test
    @DisplayName("Deve buscar artistas por nome")
    void shouldSearchArtistsByName() {
        // Given
        when(artistRepository.findByNameContainingIgnoreCase("Test"))
                .thenReturn(Arrays.asList(artist));

        // When
        List<ArtistResponse> responses = artistService.searchByName("Test");

        // Then
        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getName()).contains("Test");

        verify(artistRepository).findByNameContainingIgnoreCase("Test");
    }

    @Test
    @DisplayName("Deve listar apenas bandas")
    void shouldListOnlyBands() {
        // Given
        Artist band = Artist.builder()
                .id(2L)
                .name("Test Band")
                .isBand(true)
                .build();

        when(artistRepository.findByIsBandTrue()).thenReturn(Arrays.asList(band));

        // When
        List<ArtistResponse> responses = artistService.findBands();

        // Then
        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getIsBand()).isTrue();

        verify(artistRepository).findByIsBandTrue();
    }

    @Test
    @DisplayName("Deve listar apenas artistas solo")
    void shouldListOnlySoloArtists() {
        // Given
        when(artistRepository.findByIsBandFalse()).thenReturn(Arrays.asList(artist));

        // When
        List<ArtistResponse> responses = artistService.findSoloArtists();

        // Then
        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getIsBand()).isFalse();

        verify(artistRepository).findByIsBandFalse();
    }
}
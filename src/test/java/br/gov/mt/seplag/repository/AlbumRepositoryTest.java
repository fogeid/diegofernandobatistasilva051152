package br.gov.mt.seplag.repository;

import br.gov.mt.seplag.entity.Album;
import br.gov.mt.seplag.entity.Artist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração para AlbumRepository
 * Usa banco H2 em memória para testes
 */
@DataJpaTest(properties = {
        "spring.flyway.enabled=false"
})
@ActiveProfiles("test")
@DisplayName("AlbumRepository Integration Tests")
class AlbumRepositoryTest {

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private ArtistRepository artistRepository;

    private Artist soloArtist;
    private Artist band;
    private Album album1;
    private Album album2;

    @BeforeEach
    void setUp() {
        // Limpa o banco
        albumRepository.deleteAll();
        artistRepository.deleteAll();

        // Cria artista solo
        soloArtist = Artist.builder()
                .name("Solo Artist")
                .isBand(false)
                .albums(new HashSet<>())
                .build();
        soloArtist = artistRepository.save(soloArtist);

        // Cria banda
        band = Artist.builder()
                .name("Test Band")
                .isBand(true)
                .albums(new HashSet<>())
                .build();
        band = artistRepository.save(band);

        // Cria álbum de artista solo
        album1 = Album.builder()
                .title("Solo Album")
                .releaseYear(2020)
                .artists(new HashSet<>(Set.of(soloArtist)))
                .covers(new HashSet<>())
                .build();
        album1 = albumRepository.save(album1);

        // Cria álbum de banda
        album2 = Album.builder()
                .title("Band Album")
                .releaseYear(2021)
                .artists(new HashSet<>(Set.of(band)))
                .covers(new HashSet<>())
                .build();
        album2 = albumRepository.save(album2);
    }

    @Test
    @DisplayName("Deve salvar e recuperar álbum")
    void shouldSaveAndRetrieveAlbum() {
        // Given
        Album newAlbum = Album.builder()
                .title("New Album")
                .releaseYear(2024)
                .artists(new HashSet<>(Set.of(soloArtist)))
                .covers(new HashSet<>())
                .build();

        // When
        Album saved = albumRepository.save(newAlbum);
        Optional<Album> found = albumRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("New Album");
        assertThat(found.get().getReleaseYear()).isEqualTo(2024);
    }

    @Test
    @DisplayName("Deve buscar álbuns por título (case-insensitive)")
    void shouldFindAlbumsByTitle() {
        // When
        List<Album> found = albumRepository.findByTitleContainingIgnoreCase("solo");

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getTitle()).isEqualTo("Solo Album");
    }

    @Test
    @DisplayName("Deve buscar álbuns por ano de lançamento")
    void shouldFindAlbumsByReleaseYear() {
        // When
        List<Album> found = albumRepository.findByReleaseYear(2020);

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getTitle()).isEqualTo("Solo Album");
    }

    @Test
    @DisplayName("Deve buscar álbuns de bandas com paginação")
    void shouldFindAlbumsByBands() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Album> page = albumRepository.findAlbumsByBands(pageable);

        // Then
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getTitle()).isEqualTo("Band Album");
    }

    @Test
    @DisplayName("Deve buscar álbuns de artistas solo com paginação")
    void shouldFindAlbumsBySoloArtists() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Album> page = albumRepository.findAlbumsBySoloArtists(pageable);

        // Then
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getTitle()).isEqualTo("Solo Album");
    }

    @Test
    @DisplayName("Deve deletar álbum")
    void shouldDeleteAlbum() {
        // Given
        Long albumId = album1.getId();

        // When
        albumRepository.deleteById(albumId);
        Optional<Album> found = albumRepository.findById(albumId);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Deve atualizar álbum")
    void shouldUpdateAlbum() {
        // Given
        Album album = albumRepository.findById(album1.getId()).orElseThrow();
        album.setTitle("Updated Title");
        album.setReleaseYear(2025);

        // When
        Album updated = albumRepository.save(album);

        // Then
        assertThat(updated.getTitle()).isEqualTo("Updated Title");
        assertThat(updated.getReleaseYear()).isEqualTo(2025);
    }

    @Test
    @DisplayName("Deve manter relacionamento com artistas ao salvar álbum")
    void shouldMaintainArtistRelationship() {
        // Given
        Album album = albumRepository.findById(album1.getId()).orElseThrow();

        // When
        Set<Artist> artists = album.getArtists();

        // Then
        assertThat(artists).hasSize(1);
        assertThat(artists).extracting(Artist::getName).contains("Solo Artist");
    }

    @Test
    @DisplayName("Deve listar todos os álbuns")
    void shouldListAllAlbums() {
        // When
        List<Album> albums = albumRepository.findAll();

        // Then
        assertThat(albums).hasSize(2);
    }

    @Test
    @DisplayName("Deve verificar se álbum existe por ID")
    void shouldCheckIfAlbumExists() {
        // When
        boolean exists = albumRepository.existsById(album1.getId());
        boolean notExists = albumRepository.existsById(999L);

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Busca por título vazio deve retornar lista vazia")
    void shouldReturnEmptyListForNonExistentTitle() {
        // When
        List<Album> found = albumRepository.findByTitleContainingIgnoreCase("NonExistent");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Busca por ano inexistente deve retornar lista vazia")
    void shouldReturnEmptyListForNonExistentYear() {
        // When
        List<Album> found = albumRepository.findByReleaseYear(1999);

        // Then
        assertThat(found).isEmpty();
    }
}
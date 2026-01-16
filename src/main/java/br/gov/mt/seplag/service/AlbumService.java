package br.gov.mt.seplag.service;

import br.gov.mt.seplag.dto.*;
import br.gov.mt.seplag.entity.Album;
import br.gov.mt.seplag.entity.Artist;
import br.gov.mt.seplag.repository.AlbumRepository;
import br.gov.mt.seplag.repository.ArtistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;

    public List<AlbumResponse> findAll() {
        return albumRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PageResponse<AlbumResponse> findAlbumsByBands(Pageable pageable) {
        Page<Album> page = albumRepository.findAlbumsByBands(pageable);
        return toPageResponse(page);
    }

    public PageResponse<AlbumResponse> findAlbumsBySoloArtists(Pageable pageable) {
        Page<Album> page = albumRepository.findAlbumsBySoloArtists(pageable);
        return toPageResponse(page);
    }

    public AlbumResponse findById(Long id) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Álbum não encontrado com ID: " + id));

        return toResponse(album);
    }

    public List<AlbumResponse> searchByTitle(String title) {
        return albumRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<AlbumResponse> findByYear(Integer year) {
        return albumRepository.findByReleaseYear(year)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public AlbumResponse insert(AlbumRequest request) {
        Set<Artist> artists = loadArtists(request.getArtistIds());

        Album album = Album.builder()
                .title(request.getTitle())
                .releaseYear(request.getReleaseYear())
                .artists(artists)
                .build();

        Album saved = albumRepository.save(album);
        return toResponse(saved);
    }

    @Transactional
    public AlbumResponse update(Long id, AlbumRequest request) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Álbum não encontrado com ID: " + id));

        album.setTitle(request.getTitle());
        album.setReleaseYear(request.getReleaseYear());

        Set<Artist> artists = loadArtists(request.getArtistIds());
        album.setArtists(artists);

        Album updated = albumRepository.save(album);

        return toResponse(updated);
    }

    public void delete(Long id) {
        if (!albumRepository.existsById(id)) {
            throw new RuntimeException("Álbum não encontrado com ID: " + id);
        }

        albumRepository.deleteById(id);
    }

    private Set<Artist> loadArtists(Set<Long> artistIds) {
        Set<Artist> artists = new HashSet<>();

        for (Long artistId : artistIds) {
            Artist artist = artistRepository.findById(artistId)
                    .orElseThrow(() -> new RuntimeException("Artista não encontrado com ID: " + artistId));
            artists.add(artist);
        }

        return artists;
    }

    private AlbumResponse toResponse(Album album) {

        List<ArtistResponse> artistResponses = album.getArtists().stream()
                .map(artist -> ArtistResponse.builder()
                        .id(artist.getId())
                        .name(artist.getName())
                        .isBand(artist.getIsBand())
                        .createdAt(artist.getCreatedAt())
                        .updatedAt(artist.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        List<AlbumCoverResponse> coverResponses = album.getCovers() != null
                ? album.getCovers().stream()
                .map(cover -> AlbumCoverResponse.builder()
                        .id(cover.getId())
                        .albumId(cover.getAlbum().getId())
                        .fileName(cover.getFileName())
                        .contentType(cover.getContentType())
                        .fileSize(cover.getFileSize())
                        .createdAt(cover.getCreatedAt())
                        .build())
                .collect(Collectors.toList())
                : List.of();

        return AlbumResponse.builder()
                .id(album.getId())
                .title(album.getTitle())
                .releaseYear(album.getReleaseYear())
                .artists(artistResponses)
                .covers(coverResponses)
                .createdAt(album.getCreatedAt())
                .updatedAt(album.getUpdatedAt())
                .build();
    }

    private PageResponse<AlbumResponse> toPageResponse(Page<Album> page) {
        List<AlbumResponse> content = page.getContent()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PageResponse.<AlbumResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}

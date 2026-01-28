package br.gov.mt.seplag.service;

import br.gov.mt.seplag.dto.*;
import br.gov.mt.seplag.entity.Album;
import br.gov.mt.seplag.entity.AlbumCover;
import br.gov.mt.seplag.entity.Artist;
import br.gov.mt.seplag.exception.ResourceNotFoundException;
import br.gov.mt.seplag.repository.AlbumRepository;
import br.gov.mt.seplag.repository.ArtistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
@Slf4j
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final NotificationService notificationService;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.public-url}")
    private String minioPublicUrl;

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
                .orElseThrow(() -> new ResourceNotFoundException("Álbum não encontrado com ID: " + id));

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

    @Transactional
    public AlbumResponse insert(AlbumRequest request, String username) {
        Set<Artist> artists = loadArtists(request.getArtistIds());

        Album album = Album.builder()
                .title(request.getTitle())
                .releaseYear(request.getReleaseYear())
                .artists(new HashSet<>())
                .build();

        Album saved = albumRepository.saveAndFlush(album);
        saved.getArtists().addAll(artists);
        saved = albumRepository.saveAndFlush(saved);

        notificationService.notifyAlbumCreated(saved.getId(), saved.getTitle(), username);

        log.info("Álbum criado e notificado: {}", saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public AlbumResponse update(Long id, AlbumRequest request, String username) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Álbum não encontrado com ID: " + id));

        album.setTitle(request.getTitle());
        album.setReleaseYear(request.getReleaseYear());
        album.setArtists(loadArtists(request.getArtistIds()));

        Album updated = albumRepository.save(album);

        notificationService.notifyAlbumUpdated(updated.getId(), updated.getTitle(), username);
        return toResponse(updated);
    }

    @Transactional
    public void delete(Long id, String username) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Álbum não encontrado com ID: " + id));

        albumRepository.delete(album);
        notificationService.notifyAlbumDeleted(album.getId(), album.getTitle(), username);
    }

    private Set<Artist> loadArtists(Set<Long> artistIds) {
        Set<Artist> artists = new HashSet<>();
        if (artistIds == null) return artists;

        for (Long artistId : artistIds) {
            Artist artist = artistRepository.findById(artistId)
                    .orElseThrow(() -> new ResourceNotFoundException("Artista não encontrado com ID: " + artistId));
            artists.add(artist);
        }

        return artists;
    }

    private String publicUrl(String minioKey) {
        if (minioKey == null) return null;

        String base = (minioPublicUrl == null ? "" : minioPublicUrl.trim());
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);

        String key = normalizeObjectKey(minioKey);
        if (key == null || key.isBlank()) return null;

        if (key.startsWith("/")) key = key.substring(1);

        return base + "/" + bucketName + "/" + key;
    }

    private AlbumCoverResponse toAlbumCoverResponse(AlbumCover cover) {
        return AlbumCoverResponse.builder()
                .id(cover.getId())
                .albumId(cover.getAlbum().getId())
                .fileName(cover.getFileName())
                .imageUrl(publicUrl(cover.getMinioKey()))
                .contentType(cover.getContentType())
                .fileSize(cover.getFileSize())
                .createdAt(cover.getCreatedAt())
                .build();
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
                .map(this::toAlbumCoverResponse)
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

    private String normalizeObjectKey(String key) {
        if (key == null) return null;

        String k = key.trim();
        if (k.startsWith("/")) k = k.substring(1);

        String bucketPrefix = bucketName + "/";
        if (k.startsWith(bucketPrefix)) {
            k = k.substring(bucketPrefix.length());
        }

        if (k.startsWith("albums/")) {
            k = k.substring("albums/".length());
        }

        return k;
    }
}

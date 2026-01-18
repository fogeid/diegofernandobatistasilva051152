package br.gov.mt.seplag.service;

import br.gov.mt.seplag.dto.ArtistRequest;
import br.gov.mt.seplag.dto.ArtistResponse;
import br.gov.mt.seplag.entity.Artist;
import br.gov.mt.seplag.exception.ResourceNotFoundException;
import br.gov.mt.seplag.repository.ArtistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArtistService {

    private final ArtistRepository artistRepository;
    private final NotificationService notificationService;

    public List<ArtistResponse> findAll() {
        return artistRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ArtistResponse findById(Long id) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artista não encontrado com ID: " + id));
        return toResponse(artist);
    }

    public List<ArtistResponse> searchByName(String name) {
        return artistRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ArtistResponse> findBands() {
        return artistRepository.findByIsBandTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ArtistResponse> findSoloArtists() {
        return artistRepository.findByIsBandFalse()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ArtistResponse insert(ArtistRequest request, String username) {

        Artist artist = Artist.builder()
                .name(request.getName())
                .isBand(Boolean.TRUE.equals(request.getIsBand()))
                .build();

        Artist saved = artistRepository.save(artist);

        notificationService.notifyArtistCreated(
                saved.getId(),
                saved.getName(),
                username
        );

        return toResponse(saved);
    }

    @Transactional
    public ArtistResponse update(Long id, ArtistRequest request) {

        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artista não encontrado com ID: " + id));

        artist.setName(request.getName());
        artist.setIsBand(Boolean.TRUE.equals(request.getIsBand()));

        Artist updated = artistRepository.save(artist);

        return toResponse(updated);
    }

    @Transactional
    public void delete(Long id) {

        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artista não encontrado com ID: " + id));

        artistRepository.delete(artist);
    }

    private ArtistResponse toResponse(Artist artist) {
        return ArtistResponse.builder()
                .id(artist.getId())
                .name(artist.getName())
                .isBand(artist.getIsBand())
                .createdAt(artist.getCreatedAt())
                .updatedAt(artist.getUpdatedAt())
                .build();
    }
}

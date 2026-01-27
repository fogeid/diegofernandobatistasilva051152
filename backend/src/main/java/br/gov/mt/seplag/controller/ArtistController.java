package br.gov.mt.seplag.controller;

import br.gov.mt.seplag.dto.ArtistRequest;
import br.gov.mt.seplag.dto.ArtistResponse;
import br.gov.mt.seplag.service.ArtistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/artists")
@RequiredArgsConstructor
@Tag(name = "Artistas", description = "Gerenciamento de artistas e bandas")
@SecurityRequirement(name = "Bearer Authentication")
public class ArtistController {

    private final ArtistService artistService;

    @GetMapping
    @Operation(summary = "Listar artistas", description = "Retorna todos os artistas cadastrados")
    public ResponseEntity<List<ArtistResponse>> findAll() {
        List<ArtistResponse> artists = artistService.findAll();
        return ResponseEntity.ok(artists);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar artista", description = "Busca artista por ID")
    public ResponseEntity<ArtistResponse> findById(@PathVariable Long id) {
        ArtistResponse artist = artistService.findById(id);
        return ResponseEntity.ok(artist);
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar por nome", description = "Busca artistas por nome (case-insensitive)")
    public ResponseEntity<List<ArtistResponse>> searchByName(@RequestParam String name) {
        List<ArtistResponse> artists = artistService.searchByName(name);
        return ResponseEntity.ok(artists);
    }

    @GetMapping("/bands")
    @Operation(summary = "Listar bandas", description = "Retorna apenas artistas que são bandas")
    public ResponseEntity<List<ArtistResponse>> findBands() {
        List<ArtistResponse> bands = artistService.findBands();
        return ResponseEntity.ok(bands);
    }

    @GetMapping("/solo")
    @Operation(summary = "Listar artistas solo", description = "Retorna apenas artistas solo")
    public ResponseEntity<List<ArtistResponse>> findSoloArtists() {
        List<ArtistResponse> soloArtists = artistService.findSoloArtists();
        return ResponseEntity.ok(soloArtists);
    }

    @PostMapping
    @Operation(summary = "Criar artista", description = "Cadastra um novo artista ou banda")
    public ResponseEntity<ArtistResponse> create(@Valid @RequestBody ArtistRequest request, Authentication authentication) {
        ArtistResponse artist = artistService.insert(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(artist);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar artista", description = "Atualiza dados de um artista existente")
    public ResponseEntity<ArtistResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ArtistRequest request) {
        ArtistResponse artist = artistService.update(id, request);
        return ResponseEntity.ok(artist);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar artista", description = "Remove um artista do sistema")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        artistService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
package br.gov.mt.seplag.controller;

import br.gov.mt.seplag.dto.AlbumRequest;
import br.gov.mt.seplag.dto.AlbumResponse;
import br.gov.mt.seplag.dto.PageResponse;
import br.gov.mt.seplag.service.AlbumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/albums")
@RequiredArgsConstructor
@Tag(name = "Álbuns", description = "Gerenciamento de álbuns musicais")
@SecurityRequirement(name = "Bearer Authentication")
public class AlbumController {

    private final AlbumService albumService;

    @GetMapping
    @Operation(summary = "Listar álbuns", description = "Retorna todos os álbuns cadastrados")
    public ResponseEntity<List<AlbumResponse>> findAll() {
        List<AlbumResponse> albums = albumService.findAll();
        return ResponseEntity.ok(albums);
    }

    @GetMapping("/bands")
    @Operation(summary = "Listar álbuns de bandas", description = "Retorna álbuns de bandas com paginação")
    public ResponseEntity<PageResponse<AlbumResponse>> findAlbumsByBands(
            @Parameter(description = "Número da página (começa em 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo para ordenação")
            @RequestParam(defaultValue = "title") String sort,
            @Parameter(description = "Direção da ordenação (asc ou desc)")
            @RequestParam(defaultValue = "asc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        PageResponse<AlbumResponse> albums = albumService.findAlbumsByBands(pageable);

        return ResponseEntity.ok(albums);
    }

    @GetMapping("/solo")
    @Operation(summary = "Listar álbuns de artistas solo", description = "Retorna álbuns de artistas solo com paginação")
    public ResponseEntity<PageResponse<AlbumResponse>> findAlbumsBySoloArtists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        PageResponse<AlbumResponse> albums = albumService.findAlbumsBySoloArtists(pageable);

        return ResponseEntity.ok(albums);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar álbum", description = "Busca álbum por ID")
    public ResponseEntity<AlbumResponse> findById(@PathVariable Long id) {
        AlbumResponse album = albumService.findById(id);
        return ResponseEntity.ok(album);
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar por título", description = "Busca álbuns por título (case-insensitive)")
    public ResponseEntity<List<AlbumResponse>> searchByTitle(@RequestParam String title) {
        List<AlbumResponse> albums = albumService.searchByTitle(title);
        return ResponseEntity.ok(albums);
    }

    @GetMapping("/year/{year}")
    @Operation(summary = "Buscar por ano", description = "Busca álbuns por ano de lançamento")
    public ResponseEntity<List<AlbumResponse>> findByYear(@PathVariable Integer year) {
        List<AlbumResponse> albums = albumService.findByYear(year);
        return ResponseEntity.ok(albums);
    }

    @PostMapping
    @Operation(summary = "Criar álbum", description = "Cadastra um novo álbum com seus artistas")
    public ResponseEntity<AlbumResponse> create(@Valid @RequestBody AlbumRequest request) {
        AlbumResponse album = albumService.insert(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(album);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar álbum", description = "Atualiza dados de um álbum existente")
    public ResponseEntity<AlbumResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AlbumRequest request) {
        AlbumResponse album = albumService.update(id, request);
        return ResponseEntity.ok(album);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar álbum", description = "Remove um álbum do sistema")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        albumService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
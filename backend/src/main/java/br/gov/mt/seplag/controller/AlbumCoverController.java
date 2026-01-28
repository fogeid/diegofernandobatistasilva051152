package br.gov.mt.seplag.controller;

import br.gov.mt.seplag.dto.AlbumCoverResponse;
import br.gov.mt.seplag.service.AlbumCoverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/albums/{albumId}/covers")
@RequiredArgsConstructor
@Tag(name = "Capas de Álbuns", description = "Upload e gerenciamento de capas de álbuns")
@SecurityRequirement(name = "Bearer Authentication")
public class AlbumCoverController {

    private final AlbumCoverService albumCoverService;

    @GetMapping
    @Operation(summary = "Listar capas", description = "Lista todas as capas de um álbum")
    public ResponseEntity<List<AlbumCoverResponse>> findByAlbumId(@PathVariable Long albumId) {
        List<AlbumCoverResponse> covers = albumCoverService.findByAlbumId(albumId);
        return ResponseEntity.ok(covers);
    }

    @GetMapping("/{coverId}")
    @Operation(summary = "Buscar capa", description = "Busca capa por ID (validando se pertence ao álbum)")
    public ResponseEntity<AlbumCoverResponse> findById(
            @PathVariable Long albumId,
            @PathVariable Long coverId) {

        AlbumCoverResponse cover = albumCoverService.findById(albumId, coverId);
        return ResponseEntity.ok(cover);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload de capa",
            description = "Faz upload de uma imagem de capa para o álbum (max 10MB, formatos: jpg, png, gif, webp)"
    )
    public ResponseEntity<AlbumCoverResponse> upload(
            @PathVariable Long albumId,
            @Parameter(description = "Arquivo de imagem")
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        AlbumCoverResponse cover = albumCoverService.uploadCover(albumId, file, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(cover);
    }

    @DeleteMapping("/{coverId}")
    @Operation(summary = "Deletar capa", description = "Remove uma capa do álbum (validando se pertence ao álbum)")
    public ResponseEntity<Void> delete(
            @PathVariable Long albumId,
            @PathVariable Long coverId) {

        albumCoverService.delete(albumId, coverId);
        return ResponseEntity.noContent().build();
    }
}

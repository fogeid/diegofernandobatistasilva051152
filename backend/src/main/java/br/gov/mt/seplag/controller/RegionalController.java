package br.gov.mt.seplag.controller;

import br.gov.mt.seplag.dto.RegionalRequest;
import br.gov.mt.seplag.dto.RegionalResponse;
import br.gov.mt.seplag.service.RegionalService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/v1/regionais")
@RequiredArgsConstructor
@Tag(name = "Regionais", description = "Gerenciamento de regionais")
@SecurityRequirement(name = "Bearer Authentication")
public class RegionalController {

    private final RegionalService regionalService;

    @GetMapping
    @Operation(summary = "Listar regionais", description = "Retorna todas as regionais cadastradas")
    public ResponseEntity<List<RegionalResponse>> findAll() {
        List<RegionalResponse> regionais = regionalService.findAll();
        return ResponseEntity.ok(regionais);
    }

    @GetMapping("/active")
    @Operation(summary = "Listar regionais ativas", description = "Retorna apenas regionais ativas")
    public ResponseEntity<List<RegionalResponse>> findActive() {
        List<RegionalResponse> regionais = regionalService.findActive();
        return ResponseEntity.ok(regionais);
    }

    @GetMapping("/inactive")
    @Operation(summary = "Listar regionais inativas", description = "Retorna apenas regionais inativas")
    public ResponseEntity<List<RegionalResponse>> findInactive() {
        List<RegionalResponse> regionais = regionalService.findInactive();
        return ResponseEntity.ok(regionais);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar regional", description = "Busca regional por ID")
    public ResponseEntity<RegionalResponse> findById(@PathVariable Integer id) {
        RegionalResponse regional = regionalService.findById(id);
        return ResponseEntity.ok(regional);
    }

    @PostMapping
    @Operation(summary = "Criar regional", description = "Cadastra uma nova regional manualmente")
    public ResponseEntity<RegionalResponse> insert(
            @Valid @RequestBody RegionalRequest request,
            Authentication authentication) {
        RegionalResponse regional = regionalService.insert(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(regional);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar regional", description = "Atualiza dados de uma regional existente")
    public ResponseEntity<RegionalResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody RegionalRequest request,
            Authentication authentication) {
        RegionalResponse regional = regionalService.update(id, request, authentication.getName());
        return ResponseEntity.ok(regional);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar regional", description = "Remove uma regional do sistema (hard delete)")
    public ResponseEntity<Void> delete(
            @PathVariable Integer id,
            Authentication authentication) {
        regionalService.delete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/inactivate")
    @Operation(summary = "Inativar regional", description = "Inativa uma regional (soft delete)")
    public ResponseEntity<RegionalResponse> inactivate(
            @PathVariable Integer id,
            Authentication authentication) {
        RegionalResponse regional = regionalService.inactivate(id, authentication.getName());
        return ResponseEntity.ok(regional);
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Reativar regional", description = "Reativa uma regional inativa")
    public ResponseEntity<RegionalResponse> activate(
            @PathVariable Integer id,
            Authentication authentication) {
        RegionalResponse regional = regionalService.activate(id, authentication.getName());
        return ResponseEntity.ok(regional);
    }

    @PostMapping("/sync")
    @Operation(
            summary = "Sincronizar com API externa",
            description = "Sincroniza as regionais com a API externa do Argus"
    )
    public ResponseEntity<Map<String, Object>> synchronize(Authentication authentication) {
        RegionalService.SyncResult result = regionalService.synchronize(authentication.getName());

        Map<String, Object> response = Map.of(
                "message", "Sincronização concluída com sucesso",
                "novos", result.novos,
                "atualizados", result.atualizados,
                "inativados", result.inativados,
                "total", result.novos + result.atualizados + result.inativados
        );

        return ResponseEntity.ok(response);
    }
}
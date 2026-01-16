package br.gov.mt.seplag.controller;

import br.gov.mt.seplag.dto.RegionalResponse;
import br.gov.mt.seplag.service.RegionalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/regionais")
@RequiredArgsConstructor
@Tag(name = "Regionais", description = "Gerenciamento de regionais e sincronização com API externa")
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

    @PostMapping("/sync")
    @Operation(
            summary = "Sincronizar com API externa",
            description = "Busca regionais da API externa e sincroniza com o banco local"
    )
    public ResponseEntity<Map<String, Object>> synchronize() {
        RegionalService.SyncResult result = regionalService.synchronize();

        Map<String, Object> response = Map.of(
                "success", true,
                "message", "Sincronização concluída com sucesso",
                "novos", result.novos,
                "atualizados", result.atualizados,
                "inativados", result.inativados
        );

        return ResponseEntity.ok(response);
    }
}
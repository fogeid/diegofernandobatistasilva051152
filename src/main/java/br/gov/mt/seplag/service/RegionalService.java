package br.gov.mt.seplag.service;

import br.gov.mt.seplag.dto.RegionalRequest;
import br.gov.mt.seplag.dto.RegionalResponse;
import br.gov.mt.seplag.entity.Regional;
import br.gov.mt.seplag.exception.ResourceNotFoundException;
import br.gov.mt.seplag.repository.RegionalRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RegionalService {

    private final RegionalRepository regionalRepository;

    private static final String API_URL = "https://integrador-argus-api.geia.vip/v1/regionais";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final WebClient webClient = WebClient.builder()
            .baseUrl(API_URL)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<RegionalResponse> findAll() {
        return regionalRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<RegionalResponse> findActive() {
        return regionalRepository.findByAtivoTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<RegionalResponse> findInactive() {
        return regionalRepository.findByAtivoFalse()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public RegionalResponse findById(Integer id) {
        Regional regional = regionalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Regional não encontrada com ID: " + id));

        return toResponse(regional);
    }

    @Transactional
    public RegionalResponse insert(RegionalRequest request, String username) {
        log.info("Criando nova regional: {} por {}", request.getNome(), username);

        if (request.getId() != null && regionalRepository.existsById(request.getId())) {
            throw new RuntimeException("Já existe uma regional com ID: " + request.getId());
        }

        Regional regional = Regional.builder()
                .id(request.getId())
                .nome(request.getNome())
                .ativo(request.getAtivo() != null ? request.getAtivo() : true)
                .build();

        Regional saved = regionalRepository.save(regional);

        log.info("Regional criada com sucesso: ID={}, Nome={}", saved.getId(), saved.getNome());

        return toResponse(saved);
    }

    @Transactional
    public RegionalResponse update(Integer id, RegionalRequest request, String username) {
        log.info("Atualizando regional ID={} por {}", id, username);

        Regional regional = regionalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Regional não encontrada com ID: " + id));

        String nomeAntigo = regional.getNome();
        Boolean ativoAntigo = regional.getAtivo();

        regional.setNome(request.getNome());

        if (request.getAtivo() != null) {
            regional.setAtivo(request.getAtivo());
        }

        Regional updated = regionalRepository.save(regional);

        log.info("Regional atualizada: ID={}, Nome: {} -> {}, Ativo: {} -> {}",
                id, nomeAntigo, updated.getNome(), ativoAntigo, updated.getAtivo());

        return toResponse(updated);
    }

    @Transactional
    public void delete(Integer id, String username) {
        log.info("Deletando regional ID={} por {}", id, username);

        Regional regional = regionalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Regional não encontrada com ID: " + id));

        regionalRepository.delete(regional);

        log.info("Regional deletada: ID={}, Nome={}", id, regional.getNome());
    }

    @Transactional
    public RegionalResponse inactivate(Integer id, String username) {
        log.info("Inativando regional ID={} por {}", id, username);

        Regional regional = regionalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Regional não encontrada com ID: " + id));

        regional.setAtivo(false);
        Regional updated = regionalRepository.save(regional);

        log.info("Regional inativada: ID={}, Nome={}", id, regional.getNome());

        return toResponse(updated);
    }

    @Transactional
    public RegionalResponse activate(Integer id, String username) {
        log.info("Reativando regional ID={} por {}", id, username);

        Regional regional = regionalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Regional não encontrada com ID: " + id));

        regional.setAtivo(true);
        Regional updated = regionalRepository.save(regional);

        log.info("Regional reativada: ID={}, Nome={}", id, regional.getNome());

        return toResponse(updated);
    }

    @Transactional
    public SyncResult synchronize(String username) {
        log.info("Iniciando sincronização com API externa: {} por {}", API_URL, username);

        try {
            String jsonResponse = webClient
                    .get()
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(TIMEOUT)
                    .block();

            if (jsonResponse == null || jsonResponse.isEmpty()) {
                throw new RuntimeException("Resposta vazia da API externa");
            }

            log.debug("Resposta da API: {}", jsonResponse);

            JsonNode root = objectMapper.readTree(jsonResponse);

            JsonNode regionaisNode = null;

            if (root.has("regionais")) {
                regionaisNode = root.get("regionais");
            }
            else if (root.has("data")) {
                regionaisNode = root.get("data");
            }
            else if (root.isArray()) {
                regionaisNode = root;
            }

            if (regionaisNode == null || !regionaisNode.isArray()) {
                log.error("Formato de resposta inesperado: {}", jsonResponse);
                throw new RuntimeException("Formato de resposta inválido da API. Esperado array de regionais.");
            }

            if (regionaisNode.size() == 0) {
                log.warn("API retornou array vazio de regionais");
                return new SyncResult(0, 0, 0);
            }

            Set<Integer> idsFromApi = new HashSet<>();
            int novos = 0;
            int atualizados = 0;

            for (JsonNode node : regionaisNode) {
                if (!node.has("id") || !node.has("nome")) {
                    log.warn("Regional sem id ou nome, ignorando: {}", node);
                    continue;
                }

                Integer id = node.get("id").asInt();
                String nome = node.get("nome").asText();

                idsFromApi.add(id);

                Regional existing = regionalRepository.findById(id).orElse(null);

                if (existing == null) {
                    Regional novo = Regional.builder()
                            .id(id)
                            .nome(nome)
                            .ativo(true)
                            .build();
                    regionalRepository.save(novo);
                    novos++;
                    log.info("Nova regional inserida: ID={}, Nome={}", id, nome);

                } else if (!existing.getNome().equals(nome)) {
                    existing.setNome(nome);
                    existing.setAtivo(true);
                    regionalRepository.save(existing);
                    atualizados++;
                    log.info("Regional atualizada: ID={}, Nome={}", id, nome);

                } else if (!existing.getAtivo()) {
                    existing.setAtivo(true);
                    regionalRepository.save(existing);
                    atualizados++;
                    log.info("Regional reativada: ID={}, Nome={}", id, nome);
                }
            }

            List<Regional> todasAtivas = regionalRepository.findByAtivoTrue();
            int inativados = 0;

            for (Regional regional : todasAtivas) {
                if (!idsFromApi.contains(regional.getId())) {
                    regional.setAtivo(false);
                    regionalRepository.save(regional);
                    inativados++;
                    log.info("Regional inativada (ausente na API): ID={}, Nome={}",
                            regional.getId(), regional.getNome());
                }
            }

            SyncResult result = new SyncResult(novos, atualizados, inativados);
            log.info("Sincronização concluída: {}", result);

            return result;

        } catch (Exception e) {
            log.error("Erro ao sincronizar com API externa", e);
            throw new RuntimeException("Erro ao sincronizar: " + e.getMessage(), e);
        }
    }

    private RegionalResponse toResponse(Regional regional) {
        return RegionalResponse.builder()
                .id(regional.getId())
                .nome(regional.getNome())
                .ativo(regional.getAtivo())
                .createdAt(regional.getCreatedAt())
                .updatedAt(regional.getUpdatedAt())
                .build();
    }

    public static class SyncResult {
        public final int novos;
        public final int atualizados;
        public final int inativados;

        public SyncResult(int novos, int atualizados, int inativados) {
            this.novos = novos;
            this.atualizados = atualizados;
            this.inativados = inativados;
        }

        @Override
        public String toString() {
            return String.format("Novos: %d, Atualizados: %d, Inativados: %d",
                    novos, atualizados, inativados);
        }
    }
}
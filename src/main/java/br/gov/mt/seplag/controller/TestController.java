package br.gov.mt.seplag.controller;

import br.gov.mt.seplag.entity.Artist;
import br.gov.mt.seplag.entity.User;
import br.gov.mt.seplag.repository.ArtistRepository;
import br.gov.mt.seplag.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final UserRepository userRepository;
    private final ArtistRepository artistRepository;

    @GetMapping("/users")
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    /**
     * Retorna artistas em formato simples (sem relacionamentos)
     */
    @GetMapping("/artists")
    public List<Map<String, Object>> getArtists() {
        List<Artist> artists = artistRepository.findAll();

        // Converte para Map para evitar lazy loading
        return artists.stream()
                .map(artist -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", artist.getId());
                    map.put("name", artist.getName());
                    map.put("isBand", artist.getIsBand());
                    map.put("createdAt", artist.getCreatedAt());
                    map.put("updatedAt", artist.getUpdatedAt());
                    // NÃO inclui albums para evitar lazy loading
                    return map;
                })
                .toList();
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("database", "H2");
        status.put("status", "OK");
        status.put("usersCount", userRepository.count());
        status.put("artistsCount", artistRepository.count());
        return status;
    }
}
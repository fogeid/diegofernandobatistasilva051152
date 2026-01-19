package br.gov.mt.seplag.security;

import br.gov.mt.seplag.entity.User;
import br.gov.mt.seplag.repository.RefreshTokenRepository;
import br.gov.mt.seplag.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Slf4j
class SecurityIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private RefreshTokenRepository refreshTokenRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ObjectMapper objectMapper;

    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "password123";

    @BeforeEach
    void setUp() {
        // ordem importa por FK
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        User user = User.builder()
                .username(USERNAME)
                .password(passwordEncoder.encode(PASSWORD))
                .build();

        userRepository.saveAndFlush(user);
    }

    private String loginAndGetAccessToken(String username, String password) throws Exception {
        Map<String, String> loginRequest = Map.of(
                "username", username,
                "password", password
        );

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.expiresIn").exists())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());

        // seu DTO usa accessToken (não "token")
        String accessToken = json.get("accessToken").asText();
        assertThat(accessToken).isNotBlank();

        return accessToken;
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    @Test
    @DisplayName("Deve permitir login com credenciais válidas")
    void shouldAllowLoginWithValidCredentials() throws Exception {
        loginAndGetAccessToken(USERNAME, PASSWORD);
    }

    @Test
    @DisplayName("Deve bloquear login com credenciais inválidas")
    void shouldBlockLoginWithInvalidCredentials() throws Exception {
        Map<String, String> loginRequest = Map.of(
                "username", USERNAME,
                "password", "wrongpassword"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve bloquear acesso a endpoint protegido sem token")
    void shouldBlockProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/artists"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve permitir acesso a endpoint protegido com token válido")
    void shouldAllowProtectedEndpointWithValidToken() throws Exception {
        String token = loginAndGetAccessToken(USERNAME, PASSWORD);

        mockMvc.perform(get("/api/v1/artists")
                        .header("Authorization", bearer(token)))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("Deve bloquear acesso sem prefixo Bearer")
    void shouldBlockAccessWithoutBearerPrefix() throws Exception {
        String token = loginAndGetAccessToken(USERNAME, PASSWORD);

        mockMvc.perform(get("/api/v1/artists")
                        .header("Authorization", token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Swagger deve responder (index liberado; swagger-ui.html pode estar protegido)")
    void shouldAllowSwaggerWithoutAuth() throws Exception {
        // No seu log, /swagger-ui/index.html retornou 200
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().is2xxSuccessful());

        // No seu log, /swagger-ui.html retornou 401 (está protegido no SecurityConfig)
        // Se você decidir liberar, aí sim esse teste pode virar 3xx/2xx.
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("API Docs devem responder (sem auth, se estiver liberado)")
    void shouldAllowApiDocsWithoutAuth() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("CORS preflight (OPTIONS) deve responder (pelo menos não 401/5xx)")
    void shouldAllowOptionsWithoutAuth() throws Exception {
        mockMvc.perform(options("/api/v1/artists")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().is2xxSuccessful());
    }
}

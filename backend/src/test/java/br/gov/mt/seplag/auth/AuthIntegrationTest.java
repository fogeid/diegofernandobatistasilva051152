package br.gov.mt.seplag.auth;

import br.gov.mt.seplag.dto.LoginRequest;
import br.gov.mt.seplag.dto.RefreshTokenRequest;
import br.gov.mt.seplag.entity.User;
import br.gov.mt.seplag.repository.RefreshTokenRepository;
import br.gov.mt.seplag.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "password123";

    @BeforeEach
    void setup() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        userRepository.saveAndFlush(User.builder()
                .username(USERNAME)
                .password(passwordEncoder.encode(PASSWORD))
                .build());
    }

    private JsonNode doLogin(String username, String password) throws Exception {
        LoginRequest req = LoginRequest.builder()
                .username(username)
                .password(password)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.expiresIn").exists())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private JsonNode doRefreshExpectOk(String refreshToken) throws Exception {
        RefreshTokenRequest req = RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.expiresIn").exists())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private void doRefreshExpectUnauthorized(String refreshToken) throws Exception {
        RefreshTokenRequest req = RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .build();

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    private void doLogout(String refreshToken) throws Exception {
        RefreshTokenRequest req = RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .build();

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNoContent());
    }

    private static void waitNextSecond() {
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Login válido retorna tokenType=Bearer, accessToken e refreshToken")
    void login_validCredentials_returnsTokens() throws Exception {
        JsonNode json = doLogin(USERNAME, PASSWORD);

        assertThat(json.get("tokenType").asText()).isEqualTo("Bearer");
        assertThat(json.get("accessToken").asText()).isNotBlank();
        assertThat(json.get("refreshToken").asText()).isNotBlank();
        assertThat(json.get("expiresIn").asLong()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Login inválido retorna 401")
    void login_invalidCredentials_returns401() throws Exception {
        LoginRequest req = LoginRequest.builder()
                .username(USERNAME)
                .password("wrong")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Refresh rotaciona: gera novo refresh e invalida o antigo")
    void refresh_rotation_invalidatesOldRefreshToken() throws Exception {
        JsonNode login = doLogin(USERNAME, PASSWORD);
        String refresh1 = login.get("refreshToken").asText();
        String access1 = login.get("accessToken").asText();

        waitNextSecond();

        JsonNode refreshed = doRefreshExpectOk(refresh1);
        String refresh2 = refreshed.get("refreshToken").asText();
        String access2 = refreshed.get("accessToken").asText();

        assertThat(refresh2).isNotBlank();
        assertThat(access2).isNotBlank();

        assertThat(refresh2).isNotEqualTo(refresh1);
        assertThat(access2).isNotEqualTo(access1);

        doRefreshExpectUnauthorized(refresh1);
    }

    @Test
    @DisplayName("Logout revoga refresh token e impede refresh")
    void logout_revokesRefreshToken() throws Exception {
        JsonNode login = doLogin(USERNAME, PASSWORD);
        String refresh = login.get("refreshToken").asText();

        doLogout(refresh);

        doRefreshExpectUnauthorized(refresh);
    }

    @Test
    @DisplayName("Refresh com token vazio retorna 400")
    void refresh_blankToken_returns400() throws Exception {
        RefreshTokenRequest req = RefreshTokenRequest.builder()
                .refreshToken("")
                .build();

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}

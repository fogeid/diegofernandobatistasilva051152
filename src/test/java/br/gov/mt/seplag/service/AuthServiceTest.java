package br.gov.mt.seplag.service;

import br.gov.mt.seplag.dto.LoginRequest;
import br.gov.mt.seplag.dto.LoginResponse;
import br.gov.mt.seplag.dto.RefreshTokenRequest;
import br.gov.mt.seplag.entity.RefreshToken;
import br.gov.mt.seplag.entity.User;
import br.gov.mt.seplag.exception.BadRequestException;
import br.gov.mt.seplag.exception.UnauthorizedException;
import br.gov.mt.seplag.repository.RefreshTokenRepository;
import br.gov.mt.seplag.repository.UserRepository;
import br.gov.mt.seplag.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private AuthService authService;

    private User user;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // Como o AuthService usa @Value, em teste unitário (Mockito puro) isso vem null.
        ReflectionTestUtils.setField(authService, "accessExpirationMs", 300000L);
        ReflectionTestUtils.setField(authService, "refreshExpirationMs", 86400000L); // 1 dia

        user = User.builder()
                .id(1L)
                .username("admin")
                .password("$2a$10$encodedPassword")
                .createdAt(LocalDateTime.now())
                .build();

        loginRequest = LoginRequest.builder()
                .username("admin")
                .password("admin123")
                .build();
    }

    @Test
    @DisplayName("Deve fazer login com sucesso")
    void shouldLoginSuccessfully() {
        String accessToken = "access-token-123";
        String refreshToken = "refresh-token-456";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn(accessToken);
        when(jwtService.generateRefreshToken(user)).thenReturn(refreshToken);

        // persistRefreshToken() salva no refreshTokenRepository
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        LoginResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getAccessToken()).isEqualTo(accessToken);
        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(response.getExpiresIn()).isEqualTo(300000L);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername("admin");
        verify(jwtService).generateToken(user);
        verify(jwtService).generateRefreshToken(user);

        // garante que persistiu o refresh token em hash
        verify(refreshTokenRepository).save(argThat(rt ->
                rt.getUser() == user
                        && rt.getTokenHash() != null
                        && !rt.getTokenHash().isBlank()
                        && rt.getRevokedAt() == null
                        && rt.getExpiresAt() != null
        ));
    }

    @Test
    @DisplayName("Deve lançar exceção com credenciais inválidas")
    void shouldThrowExceptionWithInvalidCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtService, never()).generateToken(any());
        verify(jwtService, never()).generateRefreshToken(any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar UsernameNotFound quando usuário não existe")
    void shouldThrowWhenUserNotFound() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Usuário não encontrado");

        verify(jwtService, never()).generateToken(any());
        verify(jwtService, never()).generateRefreshToken(any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve renovar token com sucesso (rotation)")
    void shouldRefreshTokenSuccessfully() {
        String oldRefreshToken = "old-refresh-token";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";

        RefreshTokenRequest refreshRequest = RefreshTokenRequest.builder()
                .refreshToken(oldRefreshToken)
                .build();

        // Hash do refresh antigo (mesma lógica do AuthService)
        String oldHash = sha256ForTest(oldRefreshToken);

        RefreshToken stored = RefreshToken.builder()
                .id(10L)
                .user(user)
                .tokenHash(oldHash)
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .revokedAt(null)
                .build();

        when(jwtService.extractUsername(oldRefreshToken)).thenReturn("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(jwtService.validateToken(oldRefreshToken, user)).thenReturn(true);

        when(refreshTokenRepository.findByTokenHash(oldHash)).thenReturn(Optional.of(stored));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        when(jwtService.generateToken(user)).thenReturn(newAccessToken);
        when(jwtService.generateRefreshToken(user)).thenReturn(newRefreshToken);

        LoginResponse response = authService.refreshToken(refreshRequest);

        assertThat(response).isNotNull();
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getAccessToken()).isEqualTo(newAccessToken);
        assertThat(response.getRefreshToken()).isEqualTo(newRefreshToken);
        assertThat(response.getExpiresIn()).isEqualTo(300000L);

        // rotation: revoga o antigo + salva novo
        verify(refreshTokenRepository).findByTokenHash(oldHash);

        // 1) salvou o old com revokedAt != null
        verify(refreshTokenRepository).save(argThat(rt ->
                rt.getTokenHash().equals(oldHash) && rt.getRevokedAt() != null
        ));

        // 2) salvou o novo refresh
        verify(refreshTokenRepository).save(argThat(rt ->
                rt.getUser() == user
                        && rt.getTokenHash().equals(sha256ForTest(newRefreshToken))
                        && rt.getRevokedAt() == null
                        && rt.getExpiresAt() != null
        ));
    }

    @Test
    @DisplayName("Deve lançar exceção com refresh token inválido (JWT inválido/expirado)")
    void shouldThrowExceptionWithInvalidRefreshToken_JwtInvalid() {
        String invalidToken = "invalid-token";
        RefreshTokenRequest refreshRequest = RefreshTokenRequest.builder()
                .refreshToken(invalidToken)
                .build();

        when(jwtService.extractUsername(invalidToken)).thenReturn("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(jwtService.validateToken(invalidToken, user)).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken(refreshRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Refresh token inválido ou expirado");

        verify(refreshTokenRepository, never()).findByTokenHash(anyString());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando refresh token não existe no banco (allowlist)")
    void shouldThrowWhenRefreshNotInDb() {
        String oldRefreshToken = "old-refresh-token";
        RefreshTokenRequest refreshRequest = RefreshTokenRequest.builder()
                .refreshToken(oldRefreshToken)
                .build();

        String oldHash = sha256ForTest(oldRefreshToken);

        when(jwtService.extractUsername(oldRefreshToken)).thenReturn("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(jwtService.validateToken(oldRefreshToken, user)).thenReturn(true);

        when(refreshTokenRepository.findByTokenHash(oldHash)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshToken(refreshRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Refresh token inválido ou revogado");

        verify(refreshTokenRepository).findByTokenHash(oldHash);
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando refresh token está revogado")
    void shouldThrowWhenRefreshRevoked() {
        String oldRefreshToken = "old-refresh-token";
        RefreshTokenRequest refreshRequest = RefreshTokenRequest.builder()
                .refreshToken(oldRefreshToken)
                .build();

        String oldHash = sha256ForTest(oldRefreshToken);

        RefreshToken stored = RefreshToken.builder()
                .id(10L)
                .user(user)
                .tokenHash(oldHash)
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .revokedAt(LocalDateTime.now().minusMinutes(1))
                .build();

        when(jwtService.extractUsername(oldRefreshToken)).thenReturn("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(jwtService.validateToken(oldRefreshToken, user)).thenReturn(true);

        when(refreshTokenRepository.findByTokenHash(oldHash)).thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> authService.refreshToken(refreshRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Refresh token inválido ou revogado");

        verify(refreshTokenRepository).findByTokenHash(oldHash);
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando refresh token está expirado no banco")
    void shouldThrowWhenRefreshExpiredInDb() {
        String oldRefreshToken = "old-refresh-token";
        RefreshTokenRequest refreshRequest = RefreshTokenRequest.builder()
                .refreshToken(oldRefreshToken)
                .build();

        String oldHash = sha256ForTest(oldRefreshToken);

        RefreshToken stored = RefreshToken.builder()
                .id(10L)
                .user(user)
                .tokenHash(oldHash)
                .expiresAt(LocalDateTime.now().minusSeconds(1))
                .revokedAt(null)
                .build();

        when(jwtService.extractUsername(oldRefreshToken)).thenReturn("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(jwtService.validateToken(oldRefreshToken, user)).thenReturn(true);

        when(refreshTokenRepository.findByTokenHash(oldHash)).thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> authService.refreshToken(refreshRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Refresh token expirado");

        verify(refreshTokenRepository).findByTokenHash(oldHash);
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve registrar novo usuário")
    void shouldRegisterNewUser() {
        String username = "newuser";
        String password = "password123";
        String encodedPassword = "$2a$10$encoded";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(2L);
            return u;
        });

        User result = authService.registerUser(username, password);

        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getPassword()).isEqualTo(encodedPassword);
        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao registrar username existente")
    void shouldThrowExceptionWhenUsernameExists() {
        when(userRepository.existsByUsername("admin")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerUser("admin", "pass"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Username já existe");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve fazer logout revogando refresh token quando existir e estiver ativo")
    void shouldLogoutRevokingRefreshToken() {
        String refreshToken = "refresh-token-logout";
        String hash = sha256ForTest(refreshToken);

        RefreshToken stored = RefreshToken.builder()
                .id(99L)
                .user(user)
                .tokenHash(hash)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .revokedAt(null)
                .build();

        when(refreshTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(stored));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.logout(refreshToken);

        verify(refreshTokenRepository).findByTokenHash(hash);
        verify(refreshTokenRepository).save(argThat(rt ->
                rt.getTokenHash().equals(hash) && rt.getRevokedAt() != null
        ));
    }

    @Test
    @DisplayName("Logout não deve salvar se refresh token já estiver revogado")
    void shouldLogoutNotSavingIfAlreadyRevoked() {
        String refreshToken = "refresh-token-logout";
        String hash = sha256ForTest(refreshToken);

        RefreshToken stored = RefreshToken.builder()
                .id(99L)
                .user(user)
                .tokenHash(hash)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .revokedAt(LocalDateTime.now().minusMinutes(1))
                .build();

        when(refreshTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(stored));

        authService.logout(refreshToken);

        verify(refreshTokenRepository).findByTokenHash(hash);
        verify(refreshTokenRepository, never()).save(any());
    }

    private static String sha256ForTest(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}

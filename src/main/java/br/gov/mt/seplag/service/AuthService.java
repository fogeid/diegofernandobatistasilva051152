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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.expiration}")
    private Long accessExpirationMs;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpirationMs;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        persistRefreshToken(user, refreshToken);

        return LoginResponse.builder()
                .tokenType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(accessExpirationMs)
                .build();

    }

    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        final String oldRefreshToken = request.getRefreshToken();

        final String username = jwtService.extractUsername(oldRefreshToken);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        if (!jwtService.validateToken(oldRefreshToken, user)) {
            throw new UnauthorizedException("Refresh token inválido ou expirado");
        }

        String oldHash = sha256(oldRefreshToken);

        RefreshToken stored = refreshTokenRepository.findByTokenHash(oldHash)
                .orElseThrow(() -> new UnauthorizedException("Refresh token inválido ou revogado"));

        if (stored.getRevokedAt() != null) {
            throw new UnauthorizedException("Refresh token inválido ou revogado");
        }

        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Refresh token expirado");
        }

        stored.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(stored);

        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        persistRefreshToken(user, newRefreshToken);

        return LoginResponse.builder()
                .tokenType("Bearer")
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(accessExpirationMs)
                .build();

    }

    @Transactional
    public void logout(String refreshToken) {
        String hash = sha256(refreshToken);

        refreshTokenRepository.findByTokenHash(hash).ifPresent(stored -> {
            if (stored.getRevokedAt() == null) {
                stored.setRevokedAt(LocalDateTime.now());
                refreshTokenRepository.save(stored);
            }
        });
    }

    @Transactional
    public User registerUser(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException("Username já existe");
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .build();

        return userRepository.save(user);
    }

    private void persistRefreshToken(User user, String refreshToken) {
        RefreshToken entity = RefreshToken.builder()
                .user(user)
                .tokenHash(sha256(refreshToken))
                .expiresAt(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000))
                .revokedAt(null)
                .build();

        refreshTokenRepository.save(entity);
    }

    private String sha256(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao gerar hash SHA-256", e);
        }
    }
}

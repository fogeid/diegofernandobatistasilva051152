package br.gov.mt.seplag.service;

import br.gov.mt.seplag.dto.LoginRequest;
import br.gov.mt.seplag.dto.LoginResponse;
import br.gov.mt.seplag.dto.RefreshTokenRequest;
import br.gov.mt.seplag.entity.User;
import br.gov.mt.seplag.exception.BadRequestException;
import br.gov.mt.seplag.exception.UnauthorizedException;
import br.gov.mt.seplag.repository.UserRepository;
import br.gov.mt.seplag.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        // 1. Autentica o usuário (valida username e password)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // 2. Se chegou aqui, autenticação foi bem-sucedida
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        // 3. Gera tokens JWT
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // 4. Retorna resposta com tokens
        return LoginResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(300000L) // 5 minutos em milissegundos
                .build();
    }

    public LoginResponse refreshToken(RefreshTokenRequest request) {
        // 1. Extrai username do refresh token
        String username = jwtService.extractUsername(request.getRefreshToken());

        // 2. Carrega o usuário
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        // 3. Valida o refresh token
        if (!jwtService.validateToken(request.getRefreshToken(), user)) {
            throw new UnauthorizedException("Refresh token inválido ou expirado");
        }

        // 4. Gera novo access token
        String newAccessToken = jwtService.generateToken(user);

        // 5. Retorna resposta (mantém o mesmo refresh token)
        return LoginResponse.builder()
                .token(newAccessToken)
                .refreshToken(request.getRefreshToken())
                .expiresIn(300000L)
                .build();
    }

    public User registerUser(String username, String password) {
        // Verifica se username já existe
        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException("Username já existe");
        }

        // Cria novo usuário com senha criptografada
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .build();

        return userRepository.save(user);
    }
}
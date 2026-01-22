package br.gov.mt.seplag.controller;

import br.gov.mt.seplag.dto.LoginRequest;
import br.gov.mt.seplag.dto.LoginResponse;
import br.gov.mt.seplag.dto.RefreshTokenRequest;
import br.gov.mt.seplag.entity.User;
import br.gov.mt.seplag.service.AuthService;
import io.swagger.v3.oas.annotations.OpenAPI31;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints de login e gerenciamento de tokens")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Autentica usuário e retorna tokens JWT")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping( "/register")
    @Operation(summary = "Register", description = "Registra um usuario novo para autencicação")
    public ResponseEntity<User> registerUser(@Valid @RequestBody LoginRequest request) {
        User user = authService.registerUser(request.getUsername(), request.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh Token", description = "Renova o access token usando refresh token")
    public ResponseEntity<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Revoga o refresh token informado")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
}

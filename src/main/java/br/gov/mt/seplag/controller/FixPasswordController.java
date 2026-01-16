package br.gov.mt.seplag.controller;

import br.gov.mt.seplag.entity.User;
import br.gov.mt.seplag.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class FixPasswordController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/fix-admin-now")
    public Map<String, Object> fixAdminNow() {
        // 1. Gera hash AGORA com o PasswordEncoder da aplicação
        String newHash = passwordEncoder.encode("admin123");

        // 2. Busca o admin
        User admin = userRepository.findByUsername("admin").orElseThrow();

        // 3. Atualiza a senha
        admin.setPassword(newHash);
        userRepository.save(admin);

        // 4. Verifica se funciona
        boolean matches = passwordEncoder.matches("admin123", newHash);

        return Map.of(
                "success", true,
                "message", "✅ Senha atualizada com hash NOVO gerado agora!",
                "newHash", newHash,
                "validates", matches,
                "testLogin", "Tente login agora: admin/admin123"
        );
    }
}
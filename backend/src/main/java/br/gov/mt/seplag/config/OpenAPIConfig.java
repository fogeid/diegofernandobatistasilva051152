package br.gov.mt.seplag.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Projeto para o Processo Seletivo Seplag 2026")
                        .version("1.0.0")
                        .description("""
                                API REST para gerenciamento de artistas, álbuns e capas.
                                
                                **Funcionalidades principais:**
                                - Autenticação JWT com refresh token
                                - CRUD de artistas e álbuns
                                - Upload de capas para MinIO (S3-compatible)
                                - Sincronização com API externa de regionais
                                - Paginação e filtros avançados
                                
                                **Como usar:**
                                1. Faça login em `/api/v1/auth/login` (credenciais: admin/admin123)
                                2. Copie o token retornado
                                3. Clique no botão "Authorize" no topo da página
                                4. Cole o token no formato: Bearer {seu-token}
                                5. Teste os endpoints protegidos!
                                """)
                        .contact(new Contact()
                                .name("Diego Fernando Batista Silva")
                                .email("batista.diego@protonmail.com")
                                .url("https://fogeid.github.io"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT"))
                )

                // Servidores
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Servidor de Desenvolvimento"),
                        new Server()
                                .url("https://api-seplag.example.com")
                                .description("Servidor de Produção (exemplo)")
                ))

                // Configuração de segurança JWT
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Insira o token JWT obtido no endpoint /api/v1/auth/login")
                        )
                );
    }
}
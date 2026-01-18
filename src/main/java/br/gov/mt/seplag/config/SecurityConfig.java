package br.gov.mt.seplag.config;

import br.gov.mt.seplag.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {})
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos
                        .requestMatchers(
                                // Autenticação
                                "/api/v1/auth/**",

                                //WebSocket
                                "/ws/**",
                                "/ws",
                                "/ws/info",

                                // Static resources
                                "/websocket-test.html",
                                "/static/**",
                                "/*.html",

                                // Testes (REMOVER EM PRODUÇÃO)
                                "/api/test/**",

                                // H2 Console (apenas desenvolvimento)
                                "/h2-console/**",

                                // Actuator
                                "/actuator/**",
                                "/actuator/health/**",

                                // Swagger/OpenAPI
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",

                                // Error page
                                "/error"
                        ).permitAll()

                        // Todas as outras requisições requerem autenticação
                        .anyRequest().authenticated()
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authenticationProvider(authenticationProvider())

                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                .headers(headers -> headers
                        .frameOptions(frame -> frame.disable())
                );

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
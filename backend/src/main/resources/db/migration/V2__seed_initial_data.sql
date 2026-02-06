-- V2__seed_initial_data.sql
-- Dados iniciais para PostgreSQL

-- Usuário admin (senha: admin123)
-- Hash BCrypt: $2a$10$VBQNckEMXfo9nsrHBQlcfeBrSSk1lXQ.C6SPEqQK78wBDvYkzCFqS
INSERT INTO users (username, password, created_at)
VALUES ('admin', '$2a$10$VBQNckEMXfo9nsrHBQlcfeBrSSk1lXQ.C6SPEqQK78wBDvYkzCFqS', CURRENT_TIMESTAMP);

-- Regionais (serão sincronizadas via API, mas podemos adicionar algumas iniciais)
INSERT INTO regionais (id, nome, ativo, created_at, updated_at) VALUES
                                                                    (1, 'Cuiabá', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                    (2, 'Rondonópolis', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                    (3, 'Sinop', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                    (4, 'Tangará da Serra', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                    (5, 'Cáceres', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                    (6, 'Barra do Garças', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                    (7, 'Alta Floresta', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
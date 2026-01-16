-- V2__seed_initial_data.sql
-- Popular dados iniciais: usuário admin e dados de exemplo dos artistas/álbuns

-- ========================================
-- Usuário Admin (senha: admin123)
-- ========================================
-- Hash BCrypt gerado para a senha "admin123"
-- Você pode gerar novos hashes em: https://bcrypt-generator.com/
INSERT INTO users (username, password) VALUES
    ('admin', '$2a$10$N9qo8uLOickgx2ZrVzY6j.5bZjqC5RnIvGgYqd4Yh8qG6qKqxF.6i');

-- ========================================
-- Artistas Conforme Especificação
-- ========================================
INSERT INTO artists (name, is_band) VALUES
                                        ('Serj Tankian', FALSE),      -- ID: 1
                                        ('Mike Shinoda', FALSE),       -- ID: 2
                                        ('Michel Teló', FALSE),        -- ID: 3
                                        ('Guns N'' Roses', TRUE);      -- ID: 4 (aspas simples escapadas com '')

-- ========================================
-- Álbuns Conforme Especificação
-- ========================================

-- Álbuns de Serj Tankian
INSERT INTO albums (title, release_year) VALUES
                                             ('Harakiri', 2012),                    -- ID: 1
                                             ('Black Blooms', NULL),                -- ID: 2
                                             ('The Rough Dog', NULL);               -- ID: 3

-- Álbuns de Mike Shinoda
INSERT INTO albums (title, release_year) VALUES
                                             ('The Rising Tied', 2005),             -- ID: 4
                                             ('Post Traumatic', 2018),              -- ID: 5
                                             ('Post Traumatic EP', 2018),           -- ID: 6
                                             ('Where''d You Go', NULL);             -- ID: 7

-- Álbuns de Michel Teló
INSERT INTO albums (title, release_year) VALUES
                                             ('Bem Sertanejo', 2011),                                    -- ID: 8
                                             ('Bem Sertanejo - O Show (Ao Vivo)', 2012),                -- ID: 9
                                             ('Bem Sertanejo - (1ª Temporada) - EP', 2011);             -- ID: 10

-- Álbuns de Guns N' Roses
INSERT INTO albums (title, release_year) VALUES
                                             ('Use Your Illusion I', 1991),         -- ID: 11
                                             ('Use Your Illusion II', 1991),        -- ID: 12
                                             ('Greatest Hits', 2004);               -- ID: 13

-- ========================================
-- Relacionamentos Artist <-> Album
-- ========================================

-- Serj Tankian (ID: 1) -> Seus álbuns
INSERT INTO artist_albums (artist_id, album_id) VALUES
                                                    (1, 1),  -- Serj Tankian -> Harakiri
                                                    (1, 2),  -- Serj Tankian -> Black Blooms
                                                    (1, 3);  -- Serj Tankian -> The Rough Dog

-- Mike Shinoda (ID: 2) -> Seus álbuns
INSERT INTO artist_albums (artist_id, album_id) VALUES
                                                    (2, 4),  -- Mike Shinoda -> The Rising Tied
                                                    (2, 5),  -- Mike Shinoda -> Post Traumatic
                                                    (2, 6),  -- Mike Shinoda -> Post Traumatic EP
                                                    (2, 7);  -- Mike Shinoda -> Where'd You Go

-- Michel Teló (ID: 3) -> Seus álbuns
INSERT INTO artist_albums (artist_id, album_id) VALUES
                                                    (3, 8),   -- Michel Teló -> Bem Sertanejo
                                                    (3, 9),   -- Michel Teló -> Bem Sertanejo - O Show (Ao Vivo)
                                                    (3, 10);  -- Michel Teló -> Bem Sertanejo - (1ª Temporada) - EP

-- Guns N' Roses (ID: 4) -> Seus álbuns
INSERT INTO artist_albums (artist_id, album_id) VALUES
                                                    (4, 11),  -- Guns N' Roses -> Use Your Illusion I
                                                    (4, 12),  -- Guns N' Roses -> Use Your Illusion II
                                                    (4, 13);  -- Guns N' Roses -> Greatest Hits

-- ========================================
-- Verificação: Contagem de Registros
-- ========================================
-- Descomente as linhas abaixo para verificar após execução

-- SELECT 'Users:' as tabela, COUNT(*) as total FROM users
-- UNION ALL
-- SELECT 'Artists:', COUNT(*) FROM artists
-- UNION ALL
-- SELECT 'Albums:', COUNT(*) FROM albums
-- UNION ALL
-- SELECT 'Relationships:', COUNT(*) FROM artist_albums;
-- V2__seed_initial_data.sql
-- Dados iniciais para PostgreSQL

-- Usuário admin (senha: admin123)
-- Hash BCrypt: $2a$10$VBQNckEMXfo9nsrHBQlcfeBrSSk1lXQ.C6SPEqQK78wBDvYkzCFqS
INSERT INTO users (username, password, created_at)
VALUES ('admin', '$2a$10$VBQNckEMXfo9nsrHBQlcfeBrSSk1lXQ.C6SPEqQK78wBDvYkzCFqS', CURRENT_TIMESTAMP);

-- Artistas
INSERT INTO artists (name, is_band, created_at, updated_at) VALUES
                                                                ('Serj Tankian', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                ('Mike Shinoda', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                ('Michel Teló', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                ('Guns N'' Roses', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Álbuns
INSERT INTO albums (title, release_year, created_at, updated_at) VALUES
                                                                     ('Harakiri', 2012, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                     ('Black Blooms', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                     ('Perplex Cities', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                     ('Elect the Dead', 2007, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                     ('Post Traumatic', 2018, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                     ('Dropped Frames, Vol. 1', 2020, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                     ('Dropped Frames, Vol. 2', 2020, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                     ('Dropped Frames, Vol. 3', 2021, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                     ('Bem Sertanejo', 2013, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                     ('Bem Sertanejo - O Show (Ao Vivo)', 2014, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                     ('Use Your Illusion I', 1991, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                     ('Use Your Illusion II', 1991, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                     ('Greatest Hits', 2004, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Relacionamentos artista-álbum
INSERT INTO artist_albums (artist_id, album_id) VALUES
-- Serj Tankian (ID: 1)
(1, 1),  -- Harakiri
(1, 2),  -- Black Blooms
(1, 3),  -- Perplex Cities
(1, 4),  -- Elect the Dead

-- Mike Shinoda (ID: 2)
(2, 5),  -- Post Traumatic
(2, 6),  -- Dropped Frames, Vol. 1
(2, 7),  -- Dropped Frames, Vol. 2
(2, 8),  -- Dropped Frames, Vol. 3

-- Michel Teló (ID: 3)
(3, 9),  -- Bem Sertanejo
(3, 10), -- Bem Sertanejo - O Show

-- Guns N' Roses (ID: 4)
(4, 11), -- Use Your Illusion I
(4, 12), -- Use Your Illusion II
(4, 13); -- Greatest Hits

-- Regionais (serão sincronizadas via API, mas podemos adicionar algumas iniciais)
INSERT INTO regionais (id, nome, ativo, created_at, updated_at) VALUES
                                                                    (1, 'Cuiabá', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                    (2, 'Rondonópolis', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                    (3, 'Sinop', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                    (4, 'Tangará da Serra', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                    (5, 'Cáceres', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                    (6, 'Barra do Garças', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                    (7, 'Alta Floresta', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
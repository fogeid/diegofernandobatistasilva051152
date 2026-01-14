-- V1__create_initial_tables.sql
-- Migration inicial: Cria todas as tabelas do sistema (H2 Database)

-- ========================================
-- Tabela de Usuários (Autenticação)
-- ========================================
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- Tabela de Artistas
-- ========================================
CREATE TABLE artists (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         name VARCHAR(200) NOT NULL,
                         is_band BOOLEAN DEFAULT FALSE,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- Tabela de Álbuns
-- ========================================
CREATE TABLE albums (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        title VARCHAR(200) NOT NULL,
                        release_year INTEGER,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- Tabela de Relacionamento N:N
-- Artist <-> Album
-- ========================================
CREATE TABLE artist_albums (
                               artist_id BIGINT NOT NULL,
                               album_id BIGINT NOT NULL,
                               PRIMARY KEY (artist_id, album_id),
                               FOREIGN KEY (artist_id) REFERENCES artists(id) ON DELETE CASCADE,
                               FOREIGN KEY (album_id) REFERENCES albums(id) ON DELETE CASCADE
);

-- ========================================
-- Tabela de Capas de Álbuns
-- ========================================
CREATE TABLE album_covers (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              album_id BIGINT NOT NULL,
                              file_name VARCHAR(255) NOT NULL,
                              minio_key VARCHAR(500) NOT NULL,
                              content_type VARCHAR(100),
                              file_size BIGINT,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              FOREIGN KEY (album_id) REFERENCES albums(id) ON DELETE CASCADE
);

-- ========================================
-- Tabela de Regionais
-- ========================================
CREATE TABLE regionais (
                           id INTEGER PRIMARY KEY,
                           nome VARCHAR(200) NOT NULL,
                           ativo BOOLEAN DEFAULT TRUE,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- Índices para Melhor Performance
-- ========================================

-- Busca por nome de artista
CREATE INDEX idx_artists_name ON artists(name);

-- Busca por título de álbum
CREATE INDEX idx_albums_title ON albums(title);

-- Joins na tabela N:N
CREATE INDEX idx_artist_albums_artist_id ON artist_albums(artist_id);
CREATE INDEX idx_artist_albums_album_id ON artist_albums(album_id);

-- Busca de capas por álbum
CREATE INDEX idx_album_covers_album_id ON album_covers(album_id);

-- Filtro de regionais ativas
CREATE INDEX idx_regionais_ativo ON regionais(ativo);
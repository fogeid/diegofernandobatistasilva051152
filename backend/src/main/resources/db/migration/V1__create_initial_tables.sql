-- V1__create_initial_schema.sql
-- Migration para PostgreSQL com BIGSERIAL

-- Tabela de usuários
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de artistas
CREATE TABLE artists (
                         id BIGSERIAL PRIMARY KEY,
                         name VARCHAR(200) NOT NULL,
                         is_band BOOLEAN NOT NULL DEFAULT FALSE,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de álbuns
CREATE TABLE albums (
                        id BIGSERIAL PRIMARY KEY,
                        title VARCHAR(200) NOT NULL,
                        release_year INTEGER,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de relacionamento (muitos para muitos)
CREATE TABLE artist_albums (
                               artist_id BIGINT NOT NULL,
                               album_id BIGINT NOT NULL,
                               PRIMARY KEY (artist_id, album_id),
                               FOREIGN KEY (artist_id) REFERENCES artists(id) ON DELETE CASCADE,
                               FOREIGN KEY (album_id) REFERENCES albums(id) ON DELETE CASCADE
);

-- Tabela de capas de álbuns
CREATE TABLE album_covers (
                              id BIGSERIAL PRIMARY KEY,
                              album_id BIGINT NOT NULL,
                              file_name VARCHAR(255) NOT NULL,
                              content_type VARCHAR(100) NOT NULL,
                              file_size BIGINT NOT NULL,
                              minio_key VARCHAR(500) NOT NULL,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              FOREIGN KEY (album_id) REFERENCES albums(id) ON DELETE CASCADE
);

-- Tabela de regionais
CREATE TABLE regionais (
                           id INTEGER PRIMARY KEY,
                           nome VARCHAR(200) NOT NULL,
                           ativo BOOLEAN NOT NULL DEFAULT TRUE,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para performance
CREATE INDEX idx_artists_name ON artists(name);
CREATE INDEX idx_albums_title ON albums(title);
CREATE INDEX idx_albums_year ON albums(release_year);
CREATE INDEX idx_artist_albums_artist ON artist_albums(artist_id);
CREATE INDEX idx_artist_albums_album ON artist_albums(album_id);
CREATE INDEX idx_album_covers_album ON album_covers(album_id);
CREATE INDEX idx_regionais_ativo ON regionais(ativo);
N° Inscrição : 16452

Perfil: Backend

Nome: DIEGO FERNANDO BATISTA SILVA

Email: batista.diego@protonmail.com

CPF: 051..-96


# SELETIVO SEPLAG 2026 - Engenheiro da Computação - Sênior - PROJETO BACKEND JAVA

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.1-brightgreen?style=for-the-badge&logo=spring)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=for-the-badge&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker)
![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)

API REST completa para gerenciamento de álbuns musicais, artistas, capas e regionais. Desenvolvida com Spring Boot 4.0.1, PostgreSQL, MinIO e Docker para o Processo Seletivo Seplag 2026.

---

## 📋 Índice

- [Sobre o Projeto](#-sobre-o-projeto)
- [Funcionalidades](#-funcionalidades)
- [Tecnologias](#-tecnologias)
- [Pré-requisitos](#-pré-requisitos)
- [Instalação e Execução](#-instalação-e-execução)
    - [Docker](#opção-1-docker-recomendado)
- [Endpoints da API](#-endpoints-da-api)
- [Autenticação](#-autenticação)
- [Exemplos de Uso](#-exemplos-de-uso)
- [Testes](#-testes)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Monitoramento](#-monitoramento)
- [Troubleshooting](#-troubleshooting)
- [Licença](#-licença)
- [Autor](#-autor)

---

## 🎯 Sobre o Projeto

A **Seplag API** é uma aplicação REST completa que permite gerenciar álbuns musicais, artistas, capas de álbuns e regionais administrativas. O projeto foi desenvolvido seguindo as melhores práticas de desenvolvimento, incluindo:

- ✅ Arquitetura em camadas (Controller, Service, Repository)
- ✅ Autenticação JWT com refresh tokens
- ✅ Upload de arquivos para MinIO (S3-compatible)
- ✅ Sincronização com API externa de regionais
- ✅ WebSocket para notificações em tempo real
- ✅ Rate limiting (proteção contra abuso)
- ✅ Migrations automáticas com Flyway
- ✅ Documentação interativa com Swagger
- ✅ Containerização completa com Docker
- ✅ Cobertura de testes > 80%

---

## ✨ Funcionalidades

### 🎤 Gestão de Artistas
- CRUD completo de artistas e bandas
- Busca por nome (case-insensitive)
- Filtros: bandas, artistas solo
- Relacionamento N:N com álbuns

### 💿 Gestão de Álbuns
- CRUD completo de álbuns
- Busca por título e ano
- Paginação de álbuns por tipo (bandas/solo)
- Relacionamento com múltiplos artistas

### 🖼️ Upload de Capas
- Upload de imagens para MinIO (S3)
- Suporte a múltiplas capas por álbum
- Validação de tipo e tamanho
- URLs de download automáticas

### 🗺️ Regionais
- CRUD completo de regionais
- Sincronização automática com API externa
- Controle de status ativo/inativo
- Histórico de alterações

### 🔐 Segurança
- Autenticação JWT
- Refresh tokens
- Rate limiting (100 req/min autenticado, 20 req/min anônimo)
- CORS configurável
- BCrypt para senhas

### 📡 Notificações
- WebSocket para updates em tempo real
- Notificações de criação/edição/exclusão
- Múltiplos canais (albums, artists, covers, regionais)

---

## 🚀 Tecnologias

### Backend
- **Java 21** - Linguagem principal
- **Spring Boot 4.0.1** - Framework
- **Spring Security** - Autenticação e autorização
- **Spring Data JPA** - Persistência
- **Spring WebSocket** - Notificações em tempo real
- **Spring WebFlux** - Cliente HTTP assíncrono

### Banco de Dados
- **PostgreSQL 16** - Banco de dados principal (produção)
- **H2 Database** - Banco de dados em memória (desenvolvimento/testes)
- **Flyway** - Migrations

### Storage
- **MinIO** - Armazenamento S3-compatible para imagens

### Segurança
- **JWT (JSON Web Tokens)** - Autenticação stateless
- **BCrypt** - Hash de senhas
- **Bucket4j** - Rate limiting

### Documentação
- **Swagger/OpenAPI 3** - Documentação interativa da API
- **Spring Boot Actuator** - Monitoramento

### DevOps
- **Docker** - Containerização
- **Docker Compose** - Orquestração de containers
- **Maven** - Build e gerenciamento de dependências

### Testes
- **JUnit 5** - Framework de testes
- **Mockito** - Mocks
- **AssertJ** - Assertions fluentes
- **Spring Boot Test** - Testes de integração
- **JaCoCo** - Cobertura de código

---

## 📦 Pré-requisitos

### Opção 1: Docker (Recomendado)
- [Docker](https://docs.docker.com/get-docker/) 20.10+
- [Docker Compose](https://docs.docker.com/compose/install/) 2.0+

### Opção 2: Desenvolvimento Local
- [Java 21](https://adoptium.net/)
- [Maven 3.9+](https://maven.apache.org/download.cgi)
- [PostgreSQL 16](https://www.postgresql.org/download/) (opcional, pode usar H2)
- [MinIO](https://min.io/download) (opcional)

---

## 🔧 Instalação e Execução

### Opção 1: Docker (Recomendado)

Esta é a forma **mais rápida e fácil** de rodar o projeto!

#### 1. Clone o Repositório

```bash
git clone https://github.com/fogeid/seplag-music-api.git
cd seplag-music-api
```

#### 2. Suba a Stack Completa

```bash
docker-compose up -d
```

#### 3. Aguarde a Inicialização

```bash
# Ver logs em tempo real
docker-compose logs -f app

# Aguarde até ver: "Started Application in X seconds"
```

#### 4. Acesse a Aplicação

```
🌐 API:           http://localhost:8080
📚 Swagger:       http://localhost:8080/swagger-ui.html
💾 PostgreSQL:    localhost:5432 (seplag/seplag123)
📦 MinIO Console: http://localhost:9001 (seplag/seplag123)
```

#### 5. Teste a API

```bash
# Health check
curl http://localhost:8080/actuator/health

# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

#### Comandos Úteis

```bash
# Ver status dos containers
docker-compose ps

# Ver logs
docker-compose logs -f

# Parar containers
docker-compose down

# Parar e remover volumes (⚠️ apaga dados)
docker-compose down -v

# Rebuild
docker-compose build --no-cache
docker-compose up -d
```

## 📡 Endpoints da API

### 🔐 Autenticação

#### Login
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**Resposta:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "expiresIn": 300000
}
```

#### Refresh Token
```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "seu-refresh-token"
}
```

---

### 🎤 Artistas

| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| GET | `/api/v1/artists` | Listar todos | ✅ |
| GET | `/api/v1/artists/{id}` | Buscar por ID | ✅ |
| GET | `/api/v1/artists/search?name=Queen` | Buscar por nome | ✅ |
| GET | `/api/v1/artists/bands` | Listar bandas | ✅ |
| GET | `/api/v1/artists/solo` | Listar artistas solo | ✅ |
| POST | `/api/v1/artists` | Criar artista | ✅ |
| PUT | `/api/v1/artists/{id}` | Atualizar artista | ✅ |
| DELETE | `/api/v1/artists/{id}` | Deletar artista | ✅ |

#### Exemplo: Criar Artista
```http
POST /api/v1/artists
Authorization: Bearer {seu-token}
Content-Type: application/json

{
  "name": "Queen",
  "isBand": true
}
```

---

### 💿 Álbuns

| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| GET | `/api/v1/albums` | Listar todos | ✅ |
| GET | `/api/v1/albums/{id}` | Buscar por ID | ✅ |
| GET | `/api/v1/albums/search?title=Bohemian` | Buscar por título | ✅ |
| GET | `/api/v1/albums/year/{year}` | Buscar por ano | ✅ |
| GET | `/api/v1/albums/bands?page=0&size=10` | Álbuns de bandas | ✅ |
| GET | `/api/v1/albums/solo?page=0&size=10` | Álbuns solo | ✅ |
| POST | `/api/v1/albums` | Criar álbum | ✅ |
| PUT | `/api/v1/albums/{id}` | Atualizar álbum | ✅ |
| DELETE | `/api/v1/albums/{id}` | Deletar álbum | ✅ |

#### Exemplo: Criar Álbum
```http
POST /api/v1/albums
Authorization: Bearer {seu-token}
Content-Type: application/json

{
  "title": "A Night at the Opera",
  "releaseYear": 1975,
  "artistIds": [1, 2]
}
```

---

### 🖼️ Capas de Álbuns

| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| POST | `/api/v1/albums/{id}/covers` | Upload de capa | ✅ |
| GET | `/api/v1/albums/{id}/covers` | Listar capas | ✅ |
| DELETE | `/api/v1/albums/{albumId}/covers/{coverId}` | Deletar capa | ✅ |

#### Exemplo: Upload de Capa
```bash
curl -X POST http://localhost:8080/api/v1/albums/1/covers \
  -H "Authorization: Bearer {seu-token}" \
  -F "file=@cover.jpg"
```

---

### 🗺️ Regionais

| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| GET | `/api/v1/regionais` | Listar todas | ✅ |
| GET | `/api/v1/regionais/{id}` | Buscar por ID | ✅ |
| GET | `/api/v1/regionais/active` | Listar ativas | ✅ |
| GET | `/api/v1/regionais/inactive` | Listar inativas | ✅ |
| POST | `/api/v1/regionais` | Criar regional | ✅ |
| PUT | `/api/v1/regionais/{id}` | Atualizar regional | ✅ |
| DELETE | `/api/v1/regionais/{id}` | Deletar regional | ✅ |
| PATCH | `/api/v1/regionais/{id}/activate` | Ativar regional | ✅ |
| PATCH | `/api/v1/regionais/{id}/inactivate` | Inativar regional | ✅ |
| POST | `/api/v1/regionais/sync` | Sincronizar com API | ✅ |

---

### 📚 Documentação Completa

Acesse o **Swagger** para ver todos os endpoints, modelos e testar a API:

```
http://localhost:8080/swagger-ui.html
```

---

## 🔐 Autenticação

A API usa **JWT (JSON Web Tokens)** para autenticação.

### Como Usar

1. **Faça Login** para obter o token:
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

2. **Use o Token** nas requisições:
```bash
curl http://localhost:8080/api/v1/artists \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

3. **Renove o Token** quando expirar:
```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"seu-refresh-token"}'
```

### Credenciais Padrão

| Serviço | Username | Password |
|---------|----------|----------|
| **API** | `admin` | `admin123` |
| PostgreSQL | `seplag` | `seplag123` |
| MinIO | `seplag` | `seplag123` |

⚠️ **Importante:** Altere as senhas em produção!

---

## 💡 Exemplos de Uso

### Fluxo Completo: Criar Álbum com Capa

#### 1. Login
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  | jq -r '.token')
```

#### 2. Criar Artista
```bash
ARTIST_ID=$(curl -s -X POST http://localhost:8080/api/v1/artists \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Pink Floyd","isBand":true}' \
  | jq -r '.id')
```

#### 3. Criar Álbum
```bash
ALBUM_ID=$(curl -s -X POST http://localhost:8080/api/v1/albums \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"The Dark Side of the Moon\",\"releaseYear\":1973,\"artistIds\":[$ARTIST_ID]}" \
  | jq -r '.id')
```

#### 4. Upload da Capa
```bash
curl -X POST http://localhost:8080/api/v1/albums/$ALBUM_ID/covers \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@dark-side.jpg"
```

#### 5. Buscar Álbum Completo
```bash
curl http://localhost:8080/api/v1/albums/$ALBUM_ID \
  -H "Authorization: Bearer $TOKEN" | jq
```

---

## 🧪 Testes

### Rodar Todos os Testes

```bash
mvn test
```

### Testes Específicos

```bash
# Testes de um serviço
mvn test -Dtest=AlbumServiceTest

# Testes de um método
mvn test -Dtest=AlbumServiceTest#shouldInsertAlbumSuccessfully
```

### Relatório de Cobertura

```bash
# Gerar relatório
mvn clean test jacoco:report

# Abrir relatório
open target/site/jacoco/index.html
```

### Cobertura Atual

- **Services:** ~85%
- **Repositories:** ~75%
- **Security:** ~80%
- **Total:** ~80%

---

## 📂 Estrutura do Projeto

```
seplag-music-api/
├── src/
│   ├── main/
│   │   ├── java/br/gov/mt/seplag/
│   │   │   ├── config/              # Configurações (Security, CORS, Swagger, etc)
│   │   │   ├── controller/          # Controllers REST
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── entity/              # Entidades JPA
│   │   │   ├── exception/           # Exception handlers
│   │   │   ├── repository/          # Repositories JPA
│   │   │   ├── security/            # JWT, Authentication
│   │   │   └── service/             # Lógica de negócio
│   │   └── resources/
│   │       ├── application.properties          # Config desenvolvimento
│   │       ├── application-prod.properties     # Config produção
│   │       ├── application-test.properties     # Config testes
│   │       └── db/migration/                   # Flyway migrations
│   │           ├── V1__create_initial_schema.sql
│   │           └── V2__seed_initial_data.sql
│   └── test/
│       └── java/br/gov/mt/seplag/
│           ├── service/              # Testes unitários (Services)
│           ├── repository/           # Testes de integração (Repositories)
│           └── security/             # Testes de segurança
├── docker/
│   └── postgres/
│       └── init.sql                 # Script de inicialização PostgreSQL
├── scripts/
│   └── docker-manage.sh             # Script de gerenciamento Docker
├── docker-compose.yml               # Orquestração de containers
├── Dockerfile                       # Build da aplicação
├── .dockerignore                    # Arquivos ignorados no build
├── .gitignore                       # Arquivos ignorados pelo Git
├── pom.xml                          # Dependências Maven
└── README.md                        # Este arquivo
```

---

## ⚙️ Variáveis de Ambiente

### Desenvolvimento (H2)

Já configurado em `application.properties`. Basta rodar:
```bash
mvn spring-boot:run
```

## 📊 Monitoramento

### Spring Boot Actuator

A API expõe endpoints de monitoramento:

```bash
# Health check
curl http://localhost:8080/actuator/health

# Informações da aplicação
curl http://localhost:8080/actuator/info

# Métricas
curl http://localhost:8080/actuator/metrics
```

### Logs

```bash
# Docker
docker logs backend -f

# Local
tail -f logs/spring-boot-application.log
```

---

## 🐛 Troubleshooting

### Porta 8080 já está em uso

```bash
# Descobrir processo
lsof -ti:8080

# Matar processo
lsof -ti:8080 | xargs kill -9

# Ou mudar porta
export SERVER_PORT=8081
```

### Docker: Container não inicia

```bash
# Ver logs detalhados
docker-compose logs app

# Rebuild sem cache
docker-compose build --no-cache
docker-compose up -d
```

### Erro de conexão com PostgreSQL

```bash
# Verificar se PostgreSQL está rodando
docker ps | grep postgres

# Ver logs do PostgreSQL
docker logs database

# Restart do PostgreSQL
docker-compose restart database
```

### MinIO: Bucket não existe

```bash
# Acessar MinIO Console
open http://localhost:9001

# Criar bucket "albums" manualmente
# Ou reiniciar o container minio-init
docker-compose restart minio-init
```

### Flyway: Erro de migration

```bash
# Limpar banco e rodar migrations novamente
docker-compose down -v
docker-compose up -d
```

### Erro 401 Unauthorized

```bash
# Verificar se o token está válido
# Verificar se o header Authorization está correto:
# Authorization: Bearer {seu-token}

# Gerar novo token
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### Padrões de Commit

```
feat: nova funcionalidade
fix: correção de bug
docs: documentação
style: formatação
refactor: refatoração
test: testes
chore: manutenção
```

---

## 📝 Licença

Este projeto foi desenvolvido para o **Processo Seletivo Seplag 2026**.

MIT License - veja o arquivo [LICENSE](LICENSE) para detalhes.

---

## 👤 Autor

**Diego Batista**

- GitHub: [@fogeid](https://github.com/fogeid)
- Docker Hub: [fogeid/seplag-music-api](https://hub.docker.com/r/fogeid/seplag-music-api)
- LinkedIn: [DBatista](https://linkedin.com/in/dbatista)

---

## 🔗 Links Úteis

- 📚 [Documentação Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- 🐳 [Docker Hub - Imagem do Projeto](https://hub.docker.com/r/fogeid/seplag-api)
- 🌐 [Swagger/OpenAPI](http://localhost:8080/swagger-ui.html)
- 📦 [MinIO Docs](https://docs.min.io/)
- 🐘 [PostgreSQL Docs](https://www.postgresql.org/docs/)

---

## ⭐ Agradecimentos

Obrigado por conferir este projeto! Se ele foi útil, considere dar uma ⭐ no GitHub!

---

<div align="center">

**Desenvolvido com ❤️ para o Processo Seletivo Seplag 2026**

[![GitHub](https://img.shields.io/badge/GitHub-fogeid-181717?style=for-the-badge&logo=github)](https://github.com/fogeid)
[![Docker Hub](https://img.shields.io/badge/Docker%20Hub-fogeid-2496ED?style=for-the-badge&logo=docker)](https://hub.docker.com/r/fogeid/seplag-music-api)

</div>
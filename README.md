# SELETIVO SEPLAG 2026 – Projeto Full Stack (Backend + Frontend)

## 📌 Identificação
- **Inscrição:** 16452
- **Perfil:** FullStack
- **Nome:** Diego Fernando Batista Silva
- **E-mail:** batista.diego@protonmail.com
- **CPF: 051..-96

---

## 🧩 Visão Geral do Projeto

Este projeto consiste em uma **plataforma full stack para gestão de álbuns musicais**, desenvolvida para o **Processo Seletivo SEPLAG 2026**, contemplando:

- Backend robusto em **Java + Spring Boot**
- Frontend moderno em **React + Vite + TypeScript**
- Arquitetura desacoplada
- Autenticação segura com JWT
- Upload de arquivos via MinIO
- WebSocket para notificações em tempo real
- Cobertura de testes, qualidade e análise estática com **SonarQube**
- Orquestração completa via **Docker Compose**

---

## 🏗️ Arquitetura Geral

```
[ React + Vite ]  --->  [ Nginx ]
        |                    |
        v                    v
[ API Spring Boot ] ---> [ PostgreSQL ]
        |
        +--> [ MinIO (S3) ]
        |
        +--> [ WebSocket / STOMP ]
```

---

# 🔙 Backend

## Tecnologias Principais

- **Java 21**
- **Spring Boot 3.2.x**
- Spring Security + JWT
- Spring Data JPA (Hibernate)
- Spring WebSocket (STOMP)
- Spring WebFlux (client HTTP)
- Flyway (migrations)
- PostgreSQL 16
- H2 (testes)
- MinIO (S3-compatible)
- Bucket4j (Rate Limit)
- Swagger / OpenAPI
- Actuator
- Docker / Docker Compose
- Maven
- JaCoCo (coverage)
- **SonarQube (qualidade de código)**

---

## Funcionalidades Backend

- CRUD de Artistas, Álbuns, Capas e Regionais
- Autenticação JWT + Refresh Token
- Rate limiting por perfil
- Upload e download de imagens
- Presigned URLs
- Notificações via WebSocket
- Sincronização com API externa
- Testes unitários e de integração
- Cobertura de código superior a 80%

---

## Qualidade e SonarQube

O projeto possui integração completa com **SonarQube**, incluindo:

- Análise estática de código
- Métricas de qualidade
- Dívida técnica
- Bugs e vulnerabilidades
- Cobertura via JaCoCo (XML)

### Executar análise local:

```bash
mvn clean verify sonar:sonar \
  -Dsonar.projectKey=projeto-seplag \
  -Dsonar.host.url=http://localhost:9002 \
  -Dsonar.login=SEU_TOKEN
```

---

# 🎨 Frontend

## Tecnologias Utilizadas

### Base
- **React 18**
- **TypeScript**
- **Vite 5**
- **React Router DOM**

### Estado e Dados
- **Zustand** – gerenciamento de estado global
- **TanStack React Query** – cache e sincronização de dados
- **Axios** – comunicação HTTP

### Formulários e Validação
- **React Hook Form**
- **Zod**
- @hookform/resolvers

### UI / UX
- **Tailwind CSS**
- **Lucide Icons**
- clsx + tailwind-merge
- react-hot-toast (notificações)

### Autenticação
- JWT Decode
- Controle de sessão no frontend

### WebSocket
- SockJS Client
- STOMP.js

### Testes
- **Vitest**
- Testing Library
- JSDOM
- Coverage via Vitest

### Qualidade
- ESLint
- Prettier

---

## Scripts Frontend

```bash
npm run dev            # ambiente de desenvolvimento
npm run build          # build de produção
npm run preview        # preview do build
npm run test           # testes
npm run test:coverage  # cobertura
npm run lint           # lint
npm run format         # formatar código
```

---

## Funcionalidades Frontend

- Login e refresh automático de token
- Proteção de rotas
- CRUD completo de artistas, álbuns e regionais
- Upload de capas
- Listagens paginadas
- Feedback visual com toast
- Atualizações em tempo real via WebSocket
- UX responsiva e moderna

---

# 🐳 Docker & Orquestração

O projeto sobe **toda a stack com um único comando**:

```bash
docker-compose up -d
```

### Serviços:
- Backend (Spring Boot)
- Frontend (Nginx + React build)
- PostgreSQL
- MinIO + init
- SonarQube
- SonarDB (Postgres)

---

## URLs Locais

| Serviço | URL | Acessos
|------|-----|--------|
| Frontend | http://localhost | user: admin / password: admin123
| API | http://localhost/actuator/health |
| Swagger | http://localhost/swagger-ui |
| MinIO Console | http://localhost:9001 | user: seplag / password: seplag123
| SonarQube | http://localhost:9002 | user: admin / password: admin

# 🧪 Testes

## Backend
```bash
mvn test
mvn jacoco:report
```

## Frontend
```bash
npm run test
npm run test:coverage
```

---

# 📂 Estrutura Resumida

```
backend/
 ├── src/main/java
 ├── src/test/java
 └── pom.xml

frontend/
 ├── src/
 ├── vite.config.ts
 └── package.json

docker-compose.yml
nginx.conf
README.md
```

---

# 📜 Licença

Projeto desenvolvido exclusivamente para o **Processo Seletivo SEPLAG 2026**.

---

# 👤 Autor

**Diego Fernando Batista Silva**
- GitHub: https://github.com/fogeid
- Docker Hub: https://hub.docker.com/r/fogeid

---

⭐ Obrigado pela avaliação!

#!/bin/bash
# publish-dockerhub.sh - Publicar BACKEND + FRONTEND no Docker Hub
# Uso: ./publish-dockerhub.sh
set -euo pipefail

# Cores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}"
cat << "EOF"
╔═══════════════════════════════════════════════════╗
║                                                   ║
║   🐳 Publicar no Docker Hub (backend + frontend)  ║
║                                                   ║
╚═══════════════════════════════════════════════════╝
EOF
echo -e "${NC}"

# =========================
# Configurações
# =========================
DOCKER_USERNAME="fogeid"
BACKEND_IMAGE_NAME="seplag-api"
FRONTEND_IMAGE_NAME="seplag-frontend"
VERSION="1.1.8"

BACKEND_FULL="$DOCKER_USERNAME/$BACKEND_IMAGE_NAME"
FRONTEND_FULL="$DOCKER_USERNAME/$FRONTEND_IMAGE_NAME"

# Caminhos (rodar na RAIZ do repo)
BACKEND_DIR="backend"
FRONTEND_DIR="frontend"

BACKEND_DOCKERFILE="$BACKEND_DIR/Dockerfile"
FRONTEND_DOCKERFILE="$FRONTEND_DIR/Dockerfile"

echo -e "${BLUE}📦 Configuração:${NC}"
echo "  Docker Hub Username:  $DOCKER_USERNAME"
echo "  Backend image:        $BACKEND_FULL:$VERSION"
echo "  Frontend image:       $FRONTEND_FULL:$VERSION"
echo "  Backend dir:          $BACKEND_DIR"
echo "  Frontend dir:         $FRONTEND_DIR"
echo ""

# Checagens básicas
if [ ! -f "$BACKEND_DOCKERFILE" ]; then
  echo -e "${RED}❌ Dockerfile do BACKEND não encontrado em: $BACKEND_DOCKERFILE${NC}"
  exit 1
fi

if [ ! -f "$FRONTEND_DOCKERFILE" ]; then
  echo -e "${RED}❌ Dockerfile do FRONTEND não encontrado em: $FRONTEND_DOCKERFILE${NC}"
  exit 1
fi

# Confirmar
read -p "Continuar com a publicação das duas imagens? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
  echo -e "${YELLOW}⚠️  Cancelado pelo usuário${NC}"
  exit 0
fi

# Login
echo ""
echo -e "${BLUE}🔐 Fazendo login no Docker Hub como $DOCKER_USERNAME...${NC}"
docker login -u "$DOCKER_USERNAME"
echo -e "${GREEN}✅ Login realizado com sucesso!${NC}"

# =========================
# Build BACKEND
# =========================
echo ""
echo -e "${BLUE}🔨 Buildando BACKEND: $BACKEND_FULL:$VERSION...${NC}"
docker build \
  -t "$BACKEND_FULL:$VERSION" \
  -f "$BACKEND_DOCKERFILE" \
  "$BACKEND_DIR"

echo -e "${GREEN}✅ Build do BACKEND concluído!${NC}"

echo -e "${BLUE}🏷️  Tag BACKEND latest...${NC}"
docker tag "$BACKEND_FULL:$VERSION" "$BACKEND_FULL:latest"

# =========================
# Build FRONTEND
# =========================
echo ""
echo -e "${BLUE}🔨 Buildando FRONTEND: $FRONTEND_FULL:$VERSION...${NC}"
docker build \
  -t "$FRONTEND_FULL:$VERSION" \
  -f "$FRONTEND_DOCKERFILE" \
  "$FRONTEND_DIR"

echo -e "${GREEN}✅ Build do FRONTEND concluído!${NC}"

echo -e "${BLUE}🏷️  Tag FRONTEND latest...${NC}"
docker tag "$FRONTEND_FULL:$VERSION" "$FRONTEND_FULL:latest"

# Listar imagens
echo ""
echo -e "${BLUE}📋 Imagens criadas:${NC}"
docker images | egrep "$BACKEND_IMAGE_NAME|$FRONTEND_IMAGE_NAME" || true

# =========================
# Push BACKEND
# =========================
echo ""
echo -e "${BLUE}📤 Enviando BACKEND para Docker Hub...${NC}"
docker push "$BACKEND_FULL:$VERSION"
docker push "$BACKEND_FULL:latest"
echo -e "${GREEN}✅ BACKEND enviado!${NC}"

# =========================
# Push FRONTEND
# =========================
echo ""
echo -e "${BLUE}📤 Enviando FRONTEND para Docker Hub...${NC}"
docker push "$FRONTEND_FULL:$VERSION"
docker push "$FRONTEND_FULL:latest"
echo -e "${GREEN}✅ FRONTEND enviado!${NC}"

# Limpeza opcional
echo ""
read -p "Deseja limpar imagens antigas locais? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
  docker image prune -f
  echo -e "${GREEN}✅ Imagens antigas removidas!${NC}"
fi

# Sucesso
echo ""
echo -e "${GREEN}"
cat << "EOF"
╔═══════════════════════════════════════════════════╗
║                                                   ║
║      ✅ BACKEND + FRONTEND Publicados! ✅         ║
║                                                   ║
╚═══════════════════════════════════════════════════╝
EOF
echo -e "${NC}"

echo -e "${BLUE}🐳 Backend:${NC}"
echo "  docker pull $BACKEND_FULL:latest"
echo "  docker pull $BACKEND_FULL:$VERSION"
echo ""
echo -e "${BLUE}🐳 Frontend:${NC}"
echo "  docker pull $FRONTEND_FULL:latest"
echo "  docker pull $FRONTEND_FULL:$VERSION"
echo ""

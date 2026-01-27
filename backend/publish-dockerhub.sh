#!/bin/bash

# publish-dockerhub.sh - Publicar no Docker Hub
# Autor: fogeid
# Uso: ./publish-dockerhub.sh

set -e

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
║     🐳 Publicar no Docker Hub - fogeid 🐳       ║
║                                                   ║
╚═══════════════════════════════════════════════════╝
EOF
echo -e "${NC}"

# Configurações
DOCKER_USERNAME="fogeid"
IMAGE_NAME="seplag-api"
VERSION="1.0.1"
FULL_IMAGE_NAME="$DOCKER_USERNAME/$IMAGE_NAME"

echo -e "${BLUE}📦 Configuração:${NC}"
echo "  Docker Hub Username: $DOCKER_USERNAME"
echo "  Nome da Imagem: $IMAGE_NAME"
echo "  Versão: $VERSION"
echo "  Imagem Completa: $FULL_IMAGE_NAME:$VERSION"
echo ""

# Confirmar
read -p "Continuar com a publicação? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}⚠️  Cancelado pelo usuário${NC}"
    exit 0
fi

# Login no Docker Hub
echo ""
echo -e "${BLUE}🔐 Fazendo login no Docker Hub como $DOCKER_USERNAME...${NC}"
docker login -u $DOCKER_USERNAME

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Falha no login!${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Login realizado com sucesso!${NC}"

# Verificar se Dockerfile existe
if [ ! -f "Dockerfile" ]; then
    echo -e "${RED}❌ Dockerfile não encontrado!${NC}"
    exit 1
fi

# Build da imagem
echo ""
echo -e "${BLUE}🔨 Buildando imagem $FULL_IMAGE_NAME:$VERSION...${NC}"
docker build -t $FULL_IMAGE_NAME:$VERSION .

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Falha no build!${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Build concluído!${NC}"

# Tag como latest
echo ""
echo -e "${BLUE}🏷️  Criando tag latest...${NC}"
docker tag $FULL_IMAGE_NAME:$VERSION $FULL_IMAGE_NAME:latest

# Listar imagens
echo ""
echo -e "${BLUE}📋 Imagens criadas:${NC}"
docker images | grep $IMAGE_NAME

# Push para Docker Hub
echo ""
echo -e "${BLUE}📤 Enviando para Docker Hub...${NC}"
echo "  Enviando versão $VERSION..."
docker push $FULL_IMAGE_NAME:$VERSION

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Falha no push da versão $VERSION!${NC}"
    exit 1
fi

echo ""
echo "  Enviando versão latest..."
docker push $FULL_IMAGE_NAME:latest

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Falha no push da versão latest!${NC}"
    exit 1
fi

# Limpar imagens locais antigas (opcional)
echo ""
read -p "Deseja limpar imagens antigas locais? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    docker image prune -f
    echo -e "${GREEN}✅ Imagens antigas removidas!${NC}"
fi

# Sucesso!
echo ""
echo -e "${GREEN}"
cat << "EOF"
╔═══════════════════════════════════════════════════╗
║                                                   ║
║        ✅ Imagem Publicada com Sucesso! ✅        ║
║                                                   ║
╚═══════════════════════════════════════════════════╝
EOF
echo -e "${NC}"

echo -e "${BLUE}🐳 Sua imagem Docker está disponível em:${NC}"
echo "  https://hub.docker.com/r/$DOCKER_USERNAME/$IMAGE_NAME"
echo ""
echo -e "${BLUE}📥 Para outras pessoas usarem:${NC}"
echo "  docker pull $FULL_IMAGE_NAME:latest"
echo "  docker pull $FULL_IMAGE_NAME:$VERSION"
echo ""
echo -e "${BLUE}🚀 Para rodar localmente:${NC}"
echo "  docker run -p 8080:8080 $FULL_IMAGE_NAME:latest"
echo ""
echo -e "${BLUE}📦 Com docker-compose:${NC}"
echo "  services:"
echo "    backend:"
echo "      image: $FULL_IMAGE_NAME:latest"
echo ""
echo -e "${GREEN}✅ Pronto para compartilhar!${NC}"
echo ""
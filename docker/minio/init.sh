#!/bin/sh

# docker/minio/init.sh
# Script de inicialização do MinIO - Cria bucket 'albums' automaticamente

set -e

echo "🚀 Aguardando MinIO iniciar..."
sleep 10

echo "📦 Configurando MinIO..."

# Configura o alias do MinIO
mc alias set myminio http://minio:9000 minioadmin minioadmin

# Cria o bucket 'albums' se não existir
if mc ls myminio/albums >/dev/null 2>&1; then
    echo "✅ Bucket 'albums' já existe"
else
    echo "📦 Criando bucket 'albums'..."
    mc mb myminio/albums
    echo "✅ Bucket 'albums' criado com sucesso"
fi

# Define política pública de leitura (opcional - ajuste conforme necessidade)
echo "🔐 Configurando política de acesso..."
mc anonymous set download myminio/albums

echo "✅ MinIO configurado com sucesso!"
echo ""
echo "📊 Informações do bucket:"
mc ls myminio/

exit 0
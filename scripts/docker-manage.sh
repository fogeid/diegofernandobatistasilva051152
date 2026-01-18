#!/bin/bash

# scripts/docker-manage.sh
# Script para gerenciar os containers Docker

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

function print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

function print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

function print_error() {
    echo -e "${RED}❌ $1${NC}"
}

function print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

function build() {
    print_info "Building Docker images..."
    docker-compose build --no-cache
    print_success "Build completed!"
}

function start() {
    print_info "Starting services..."
    docker-compose up -d
    print_success "Services started!"
    print_info "Waiting for services to be healthy..."
    sleep 10
    docker-compose ps
}

function stop() {
    print_info "Stopping services..."
    docker-compose down
    print_success "Services stopped!"
}

function restart() {
    stop
    start
}

function logs() {
    SERVICE=${1:-app}
    print_info "Showing logs for $SERVICE..."
    docker-compose logs -f $SERVICE
}

function status() {
    print_info "Services status:"
    docker-compose ps
}

function clean() {
    print_warning "This will remove all containers, volumes, and images!"
    read -p "Are you sure? (y/N) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        docker-compose down -v
        docker system prune -f
        print_success "Cleanup completed!"
    else
        print_info "Cleanup cancelled."
    fi
}

function shell() {
    SERVICE=${1:-app}
    print_info "Opening shell in $SERVICE..."
    docker-compose exec $SERVICE sh
}

function db_shell() {
    print_info "Opening PostgreSQL shell..."
    docker-compose exec postgres psql -U seplag -d musicdb
}

function test_api() {
    print_info "Testing API..."
    echo ""

    print_info "1. Health Check:"
    curl -s http://localhost:8080/actuator/health | jq .
    echo ""

    print_info "2. Login:"
    TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
        -H "Content-Type: application/json" \
        -d '{"username":"admin","password":"admin123"}' \
        | jq -r .token)

    if [ -n "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
        print_success "Login successful! Token: ${TOKEN:0:20}..."
        echo ""

        print_info "3. List Artists:"
        curl -s -H "Authorization: Bearer $TOKEN" \
            http://localhost:8080/api/v1/artists | jq .
        echo ""

        print_success "API is working!"
    else
        print_error "Login failed!"
    fi
}

function show_urls() {
    echo ""
    print_info "Service URLs:"
    echo "  🌐 API:            http://localhost:8080"
    echo "  📚 Swagger:        http://localhost:8080/swagger-ui.html"
    echo "  💾 PostgreSQL:     localhost:5432 (user: seplag, db: musicdb)"
    echo "  📦 MinIO Console:  http://localhost:9001 (admin/minioadmin)"
    echo "  📊 MinIO API:      http://localhost:9000"
    echo ""
}

function help() {
    echo "Docker Management Script"
    echo ""
    echo "Usage: ./docker-manage.sh [command]"
    echo ""
    echo "Commands:"
    echo "  build       - Build Docker images"
    echo "  start       - Start all services"
    echo "  stop        - Stop all services"
    echo "  restart     - Restart all services"
    echo "  logs [svc]  - Show logs (default: app)"
    echo "  status      - Show services status"
    echo "  clean       - Remove all containers and volumes"
    echo "  shell [svc] - Open shell in service (default: app)"
    echo "  db-shell    - Open PostgreSQL shell"
    echo "  test        - Test API endpoints"
    echo "  urls        - Show service URLs"
    echo "  help        - Show this help"
    echo ""
    echo "Examples:"
    echo "  ./docker-manage.sh build"
    echo "  ./docker-manage.sh start"
    echo "  ./docker-manage.sh logs app"
    echo "  ./docker-manage.sh test"
}

# Main
case "${1}" in
    build)
        build
        ;;
    start)
        start
        show_urls
        ;;
    stop)
        stop
        ;;
    restart)
        restart
        show_urls
        ;;
    logs)
        logs $2
        ;;
    status)
        status
        ;;
    clean)
        clean
        ;;
    shell)
        shell $2
        ;;
    db-shell)
        db_shell
        ;;
    test)
        test_api
        ;;
    urls)
        show_urls
        ;;
    help|*)
        help
        ;;
esac
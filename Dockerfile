# Dockerfile Multi-Stage para Spring Boot 4.0.1

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Define encoding UTF-8
ENV MAVEN_OPTS="-Dfile.encoding=UTF-8"

# Copia apenas pom.xml primeiro (cache de dependências)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código fonte
COPY src ./src

# Compila a aplicação (pula testes para build mais rápido)
RUN mvn clean package -DskipTests -Dproject.build.sourceEncoding=UTF-8

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Cria usuário não-root para segurança
RUN groupadd -r spring && useradd -r -g spring spring

# Copia o JAR da stage de build
COPY --from=build /app/target/*.jar app.jar

# Muda ownership
RUN chown spring:spring app.jar

# Usa usuário não-root
USER spring:spring

# Expõe a porta da aplicação
EXPOSE 8080

# Variáveis de ambiente padrão
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xms512m -Xmx1024m -Dfile.encoding=UTF-8"

# Comando de inicialização
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
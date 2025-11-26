# Etapa 1: build com Maven + JDK 21
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copia pom.xml e resolve dependências
COPY pom.xml .
RUN mvn -q -e -B dependency:go-offline || true

# Copia o código-fonte e empacota
COPY src ./src
RUN mvn -q -e -B clean package -DskipTests

# Etapa 2: runtime (imagem menor, só com JRE)
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copia o JAR gerado na etapa de build
COPY --from=build /app/target/*.jar app.jar

# Porta exposta pela aplicação (ajuste se sua app usar outra)
EXPOSE 8080

# Configura o entrypoint
ENTRYPOINT ["java","-jar","/app/app.jar"]
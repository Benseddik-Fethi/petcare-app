# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copie le pom et télécharge les deps en cache
COPY pom.xml .
RUN mvn -q -e -B dependency:go-offline

# Copie le reste du code et build
COPY . .
RUN mvn -q -e -B package -DskipTests

# ---- Run stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Koyeb attend que ton app écoute sur le port 8080
ENV PORT=8080
EXPOSE 8080

# Copie le jar généré
COPY --from=build /app/target/*.jar app.jar

# Lancement
ENTRYPOINT ["java","-jar","app.jar"]

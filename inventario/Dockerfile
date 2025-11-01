# Usamos una imagen de Maven con Java 17 para compilar
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# Copiamos solo el pom.xml y descargamos dependencias
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiamos el resto del código fuente y compilamos el .jar
COPY src ./src
RUN mvn package -DskipTests

# Usamos una imagen ligera, solo con Java 17, para ejecutar
FROM eclipse-temurin:17-jre-focal
WORKDIR /app

# Exponemos el puerto que definimos en application.properties
EXPOSE 8082

# Copiamos solo el .jar compilado desde la etapa 'build'
COPY --from=build /app/target/*.jar app.jar

# Comando para iniciar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
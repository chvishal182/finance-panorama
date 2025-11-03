# --- Dockerfile for authentication_service ---

# 1. BASE IMAGE:
FROM openjdk:21-jdk-slim

# 2. WORKING DIRECTORY
WORKDIR /app

# 3. COPY JAR FILE
COPY target/userservice-0.0.1-SNAPSHOT.jar app.jar

# 4. EXPOSE PORT
EXPOSE 9898

# 5. SPRING PROFILE:
ENV SPRING_PROFILES_ACTIVE=prod

# 6. ENVIRONMENT VARIABLES: 
ENV DB_HOST=192.168.56.101
ENV DB_USER=springuser         
ENV DB_PASSWORD=password       

ENV KAFKA_BOOTSTRAP_SERVERS=192.168.56.101:9092

ENV REDIS_HOST=192.168.56.101
ENV REDIS_PORT=6379
ENV REDIS_PASSWORD=redis_gold123 

# 7. ENTRYPOINT: Define the command to run when the container starts.
ENTRYPOINT ["java", "-jar", "app.jar"]
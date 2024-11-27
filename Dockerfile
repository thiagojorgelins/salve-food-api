FROM maven:3.8.4-openjdk-17-slim

COPY . .
RUN mvn clean package -DskipTests

ENTRYPOINT ["java", "-jar", "target/comunicaa-api-0.0.1-SNAPSHOT.jar"]

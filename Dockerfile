FROM eclipse-temurin:21

WORKDIR /app

RUN apt-get update && apt-get install -y maven && apt-get clean

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

EXPOSE 8080

CMD ["mvn", "spring-boot:run"]

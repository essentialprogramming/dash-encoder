#stage build
FROM maven:3.6.3-jdk-8 AS builder

WORKDIR /app

COPY . .

RUN mvn clean install -Dmaven.test.skip=true

#stage deploy
FROM openjdk:8-jre
COPY --from=builder /app/essentialprogramming-api/target/uber-essentialprogramming-api-1.0.0-SNAPSHOT.jar /app/uber-essentialprogramming-api-1.0.0-SNAPSHOT.jar

EXPOSE 8082

CMD java -jar ./app/uber-essentialprogramming-api-1.0.0-SNAPSHOT.jar
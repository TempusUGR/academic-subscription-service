FROM amazoncorretto:21-alpine-jdk
RUN apk add --no-cache curl lftp openssh-client zsh

WORKDIR /app
EXPOSE 8084
COPY ./target/academic-subscription-service-0.0.1-SNAPSHOT.jar academic-subscription-service.jar
COPY .env .env

ENTRYPOINT ["java", "-jar", "academic-subscription-service.jar"]

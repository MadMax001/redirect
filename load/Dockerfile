FROM gradle:8.14.3-jdk21-alpine AS build

ENV GRADLE_USER_HOME=/home/.gradle
ENV HOME=/home/app

RUN mkdir -p "$HOME"
RUN mkdir -p "$GRADLE_USER_HOME"

WORKDIR $HOME

COPY build.gradle.kts settings.gradle.kts ./
COPY gradlew ./
COPY gradle ./gradle/
COPY src ./src

RUN ./gradlew build --no-daemon -x test

FROM openjdk:26-ea-21-jdk-slim

ENV HOME=/home/app

RUN mkdir -p "$HOME"

WORKDIR $HOME

COPY --from=build $HOME/build/libs/*SNAPSHOT.jar $HOME/app.jar

EXPOSE ${SERVICE_PORT}

CMD ["sh", "-c", "java -jar $HOME/app.jar --server.port=${SERVICE_PORT}"]

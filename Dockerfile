FROM eclipse-temurin:25-jre

ENV HOME=/home/app

RUN mkdir -p "$HOME"

WORKDIR $HOME

COPY build/libs/*SNAPSHOT.jar $HOME/app.jar

EXPOSE 8080

CMD ["sh", "-c", "java -jar $HOME/app.jar --server.port=8080"]

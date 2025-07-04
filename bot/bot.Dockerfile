FROM openjdk:23

WORKDIR /app

COPY . /app
COPY target/bot-1.0.jar ./bot.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "bot.jar"]

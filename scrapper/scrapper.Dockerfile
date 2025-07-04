FROM openjdk:23

WORKDIR /app

COPY . /app
COPY target/scrapper-1.0.jar ./scrapper.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "scrapper.jar"]

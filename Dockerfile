FROM openjdk:8-alpine

COPY target/uberjar/copom.jar /copom/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/copom/app.jar"]

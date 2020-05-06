FROM openjdk:8-alpine

COPY target/uberjar/close-spec-test.jar /close-spec-test/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/close-spec-test/app.jar"]

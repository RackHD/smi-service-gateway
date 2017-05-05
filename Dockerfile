FROM openjdk:8-jre

COPY build/libs/service-gateway-zuul*.jar /app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
EXPOSE 46020
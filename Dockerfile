FROM gradle:8.4-jdk17 AS build
COPY --chown=gradle:gradle . /src
WORKDIR /src
RUN gradle buildFatJar --no-daemon

FROM amazoncorretto:17
EXPOSE 8080:8080
RUN mkdir /app
COPY --from=build /src/build/libs/*.jar /app/bobr-kurwa.jar
ENTRYPOINT ["java","-jar","/app/bobr-kurwa.jar"]

ARG PYTHON_VERSION=3.10.12

FROM gradle:8.4-jdk17 AS build
COPY --chown=gradle:gradle . /src
WORKDIR /src
RUN gradle buildFatJar --no-daemon

FROM amazoncorretto:17-alpine
EXPOSE 8080:8080
RUN mkdir /app

RUN apk add --update --no-cache python3 py3-pip python3-dev build-base gfortran
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
RUN --mount=type=bind,source=ml/requirements.txt,target=requirements.txt \
    python -m pip install --no-cache -r requirements.txt

COPY --from=build /src/build/libs/*.jar /app/bobr-kurwa.jar

ENTRYPOINT ["java","-jar","/app/bobr-kurwa.jar"]

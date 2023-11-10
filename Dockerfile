ARG PYTHON_VERSION=3.10.12

FROM gradle:8.4-jdk17 AS build
COPY --chown=gradle:gradle . /src
WORKDIR /src
RUN gradle buildFatJar --no-daemon

FROM amazoncorretto:17

EXPOSE 8080:8080
RUN mkdir /app
COPY --from=build /src/build/libs/*.jar /app/bobr-kurwa.jar

FROM python:${PYTHON_VERSION}-slim
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

RUN --mount=type=cache,target=/root/.cache/pip \
    --mount=type=bind,source=ml/requirements.txt,target=requirements.txt \
  python -m pip install -r requirements.txt

ENTRYPOINT ["java","-jar","/app/bobr-kurwa.jar"]

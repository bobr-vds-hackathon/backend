ARG PYTHON_VERSION=3.10.12

FROM gradle:8.4-jdk17 AS build
COPY --chown=gradle:gradle . /src
WORKDIR /src
RUN gradle buildFatJar --no-daemon

FROM ubuntu:jammy
EXPOSE 8080:8080
RUN mkdir /app

RUN apt update && DEBIAN_FRONTEND=noninteractive TZ=Europe/Moscow apt install -y wget gpg tzdata

RUN wget -O corretto.key "https://apt.corretto.aws/corretto.key" && \
    cat corretto.key | gpg --dearmor -o /usr/share/keyrings/corretto-keyring.gpg && \
    echo "deb [signed-by=/usr/share/keyrings/corretto-keyring.gpg] https://apt.corretto.aws stable main" >> /etc/apt/sources.list.d/corretto.list

RUN apt update && apt install -y java-17-amazon-corretto-jdk

RUN apt install -y python3.10-full python-is-python3 python3-pip libgl1-mesa-glx

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
RUN python --version

RUN --mount=type=cache,target=/root/.cache/pip \
    pip install --upgrade pip

RUN --mount=type=cache,target=/root/.cache/pip \
    --mount=type=bind,source=ml/requirements.txt,target=requirements.txt \
    python -m pip install -r requirements.txt

RUN apt install -y libglib2.0-0

RUN mkdir /app/input /app/output

COPY --from=build /src/build/libs/*.jar /app/bobr-kurwa.jar

ENTRYPOINT ["java","-jar","/app/bobr-kurwa.jar"]

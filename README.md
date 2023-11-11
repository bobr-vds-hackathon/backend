# Бобровый бэкенд

## Требования и запуск

Для деплоя используется Docker Compose

Для запуска нужен лишь установленные Docker, Git и _небольшие_ навыки работы с командной строкой

```shell
git clone git@github.com:bobr-vds-hackathon/backend
docker compose up --build
```

Сервер будет доступен на порте `8080`

## Стек

1. **Kotlin**
2. **Ktor**
3. Koin
4. Koin Annotations
3. WebSocket
4. Docker
5. Caddy (reverse-proxy)
6. SLF4J (logger)

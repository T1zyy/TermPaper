# LinkTalk

LinkTalk — это приложение для поиска собеседников по интересам, отправки заявок в чат и общения в реальном времени. Я делал проект как аналог сервиса знакомств, но с акцентом на подбор людей именно для общения, нетворкинга и совместных интересов.

## Что умеет приложение

- Регистрация и вход через Spring Security.
- Профиль пользователя с редактированием данных, интересов и целей общения.
- Каталог рекомендаций с умными фильтрами:
  - возраст,
  - город,
  - язык,
  - пол,
  - интересы,
  - цели,
  - поиск по тексту,
  - показ только общих интересов.
- Ранжирование рекомендаций по совпадениям интересов.
  - Полное совпадение интереса даёт больший вес.
  - Совпадение по кластеру интересов тоже учитывается, но слабее.
- Отправка заявки на общение.
- Принятие или отклонение входящих заявок.
- Блокировка пользователей.
- Отложенные пользователи, чтобы вернуться к ним позже.
- Список диалогов и история сообщений.
- Realtime-чат через WebSocket.
- Двуязычный интерфейс: русский и английский.

## Технологии

### Backend

- Java 21
- Spring Boot 3.3
- Spring Web
- Spring Validation
- Spring Data JPA
- Spring Security
- Spring WebSocket
- PostgreSQL

### Frontend

- React 18
- TypeScript
- Vite
- Mantine UI
- i18next

## Архитектура

Проект разделён на два приложения:

- `src/main/java/com/linktalk` — backend на Spring Boot.
- `frontend` — отдельный React/Vite-клиент.

На сервере есть следующие основные слои:

- `controller` — REST API и обработка запросов.
- `service` — бизнес-логика.
- `repo` — JPA-репозитории.
- `model` — сущности базы данных.
- `dto` — объекты для обмена данными с фронтом.
- `websocket` — обработка realtime-сообщений.
- `config` — безопасность, сиды и конфигурация WebSocket.

## Основные API

### Аутентификация

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET /api/auth/me`
- `PUT /api/auth/me`

### Рекомендации

- `GET /api/recommendations`
- `POST /api/recommendations/{userId}/view`

### Заявки в чат

- `POST /api/requests`
- `GET /api/requests/incoming`
- `GET /api/requests/outgoing`
- `POST /api/requests/{requestId}/accept`
- `POST /api/requests/{requestId}/decline`

### Чаты

- `GET /api/chats`
- `GET /api/chats/{conversationId}/messages`
- WebSocket: `/ws/chats?chatId={conversationId}`

### Дополнительно

- `GET /api/interests`
- `GET /api/goals`
- `GET /api/blocks`
- `POST /api/blocks/{userId}`
- `DELETE /api/blocks/{userId}`
- `GET /api/postponed`
- `POST /api/postponed/{userId}`
- `DELETE /api/postponed/{userId}`

## Как запустить

### 1. Поднять PostgreSQL

В проекте есть `docker-compose.yml` с базой:

```bash
docker compose up -d postgres
```

База поднимется на `localhost:5432` с параметрами:

- database: `linktalk`
- user: `linktalk`
- password: `linktalk`

### 2. Запустить backend

```bash
./gradlew bootRun
```

Backend стартует на `http://localhost:8080`.

### 3. Запустить frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend по умолчанию работает на `http://localhost:5173` и проксирует запросы `/api` и `/ws` на backend.

## Демо-данные

При первом запуске приложение заполняет:

- список интересов;
- список целей общения;
- набор тестовых пользователей.

Для демо-аккаунтов используется пароль:

```text
password123
```

## Ограничения и поведение

- При регистрации нужно выбрать минимум 3 интереса.
- Если пользователь уже заблокирован или находится в отложенных, он не показывается в рекомендациях.
- Рекомендованные пользователи не повторяются в течение окна просмотра, которое настраивается через `linktalk.recommendations.view-cooldown-hours`.
- Чат доступен только участникам соответствующего диалога.
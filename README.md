# Banking Card Management API

Spring Boot API для управления банковскими картами с JWT-аутентификацией, ролями `ADMIN` и `USER`, миграциями Liquibase и PostgreSQL в качестве основной базы данных.

## Возможности

- Регистрация и вход по JWT: `/api/v1/auth/sign-up`, `/api/v1/auth/sign-in`
- Ролевой доступ через Spring Security:
  - `ADMIN`: управление пользователями и всеми картами
  - `USER`: просмотр своих карт, баланс, запрос блокировки, переводы между своими картами
- Номер карты хранится в зашифрованном виде
- Во внешнем API номер карты отдается только в маске `**** **** **** 1234`
- Поиск, фильтрация по статусу и пагинация для списков карт
- Liquibase-миграции: `src/main/resources/db/migration/db.changelog-master.yaml`
- OpenAPI-файл: `docs/openapi.yaml`
- Swagger UI: `http://localhost:8090/swagger-ui/index.html`

## Запуск локально

Перед запуском нужен PostgreSQL, например:

- база: `banking`
- пользователь: `banking`
- пароль: `banking`

После этого можно запускать приложение:

```bash
./mvnw spring-boot:run
```


## Запуск dev-среды в Docker

```bash
docker compose up --build
```

Приложение будет доступно на `http://localhost:8090`, PostgreSQL на `localhost:8085`.

## Подключение к БД

- Host: `localhost`
- Port: `8085`
- Database: `banking`
- User: `banking`
- Password: `banking`
- JDBC URL: `jdbc:postgresql://localhost:8085/banking`

## Авторизация в Swagger

- Swagger UI: `http://localhost:8090/swagger-ui/index.html`
- Для получения JWT выполните `POST /api/v1/auth/sign-in`
- Пример body:

```json
{
  "username": "admin",
  "password": "admin123"
}
```

- В ответе придет JSON с полем `token`
- Нажмите `Authorize` в Swagger и вставьте значение `token`
- Заголовок `Bearer` Swagger подставляет сам
- Bootstrap admin по умолчанию:
  - `username: admin`
  - `password: admin123`

## Основные API

- `POST /api/v1/auth/sign-up` - регистрация пользователя с ролью `USER`
- `POST /api/v1/auth/sign-in` - получение JWT
- `GET /api/v1/admin/users` - список пользователей, только `ADMIN`
- `POST /api/v1/admin/users` - создание пользователя с ролью `ADMIN` или `USER`
- `GET /api/v1/admin/cards` - все карты, только `ADMIN`
- `POST /api/v1/admin/cards` - создать карту пользователю
- `PATCH /api/v1/admin/cards/{id}/block` - заблокировать карту
- `PATCH /api/v1/admin/cards/{id}/activate` - активировать карту
- `DELETE /api/v1/admin/cards/{id}` - удалить карту
- `GET /api/v1/cards` - свои карты, поддерживает `search`, `status`, `page`, `size`, `sort`
- `GET /api/v1/cards/{id}/balance` - баланс своей карты
- `POST /api/v1/cards/{id}/block-request` - запрос блокировки своей карты
- `POST /api/v1/transfers` - перевод между своими картами

Для защищенных запросов передавайте заголовок:

```http
Authorization: Bearer <jwt>
```

## Примеры запросов

- `POST /api/v1/auth/sign-up`

```json
{
  "username": "user1",
  "email": "user1@example.com",
  "password": "secret123"
}
```

- `POST /api/v1/auth/sign-in`

```json
{
  "username": "admin",
  "password": "admin123"
}
```

- `GET /api/v1/admin/users`
  query params: `page=0&size=20&sort=id,desc`

- `POST /api/v1/admin/users`

```json
{
  "username": "manager1",
  "email": "manager1@example.com",
  "password": "secret123",
  "role": "ROLE_ADMIN"
}
```

- `GET /api/v1/admin/cards`
  query params: `search=4000&status=ACTIVE&page=0&size=20&sort=id,desc`

- `POST /api/v1/admin/cards`

```json
{
  "ownerId": 1,
  "cardNumber": "4000001234567890",
  "expirationDate": "2030-12-31",
  "balance": 1500.00
}
```

- `PATCH /api/v1/admin/cards/{id}/block`
  body не нужен

- `PATCH /api/v1/admin/cards/{id}/activate`
  body не нужен

- `DELETE /api/v1/admin/cards/{id}`
  body не нужен

- `GET /api/v1/cards`
  query params: `search=1234&status=ACTIVE&page=0&size=10&sort=id,desc`

- `GET /api/v1/cards/{id}/balance`
  body не нужен

- `POST /api/v1/cards/{id}/block-request`
  body не нужен

- `POST /api/v1/transfers`

```json
{
  "fromCardId": 1,
  "toCardId": 2,
  "amount": 100.00
}
```

- `role`: `ROLE_ADMIN` или `ROLE_USER`
- `status`: `ACTIVE`, `BLOCKED`, `EXPIRED`

## Тесты

```bash
./mvnw test
```

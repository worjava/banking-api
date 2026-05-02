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
- `POST /api/v1/auth/refresh` - обновление access token по refresh token
- `POST /api/v1/auth/logout` - отзыв refresh token
- `GET /api/v1/admin/users` - список пользователей, только `ADMIN`
- `POST /api/v1/admin/users` - создание пользователя с ролью `ADMIN` или `USER`
- `GET /api/v1/admin/cards` - все карты в системе, только `ADMIN`
- `POST /api/v1/admin/cards` - создать карту пользователю
- `PATCH /api/v1/admin/cards/{id}/block` - заблокировать карту
- `PATCH /api/v1/admin/cards/{id}/activate` - активировать карту
- `DELETE /api/v1/admin/cards/{id}` - удалить карту
- `GET /api/v1/cards` - только свои карты текущего пользователя, поддерживает `search`, `status`, `page`, `size`, `sort`
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

### Сценарий: администратор создал карту пользователю

Если карта создана через `POST /api/v1/admin/cards` для пользователя с `ownerId = 2`, то администратор должен смотреть ее через `GET /api/v1/admin/cards`.

`GET /api/v1/cards` под токеном администратора не покажет эту карту, потому что этот endpoint возвращает только карты текущего пользователя, от имени которого выпущен JWT.

Корректный запрос для этого случая:

```bash
curl -X GET "http://localhost:8090/api/v1/admin/cards?page=0&size=10&sort=id,desc" \
  -H "accept: */*" \
  -H "Authorization: Bearer <jwt>"
```

Если нужно отфильтровать карты конкретного пользователя, можно использовать поиск по логину владельца или по последним 4 цифрам:

```bash
curl -X GET "http://localhost:8090/api/v1/admin/cards?search=user2&page=0&size=10&sort=id,desc" \
  -H "accept: */*" \
  -H "Authorization: Bearer <jwt>"
```

Для `sort` нужно передавать существующее поле сущности, например `id,desc`, `createdAt,desc`, `balance,asc`, `status,asc`.
Значение `sort=string` некорректно, потому что поля `string` у сущности `Card` нет.

## JWT production-friendly flow

`POST /api/v1/auth/sign-up` и `POST /api/v1/auth/sign-in` теперь возвращают:

```json
{
  "token": "<access-token>",
  "accessToken": "<access-token>",
  "refreshToken": "<refresh-token>",
  "tokenType": "Bearer",
  "accessTokenExpiresAt": "2026-05-02T12:15:00Z",
  "refreshTokenExpiresAt": "2026-05-09T12:00:00Z"
}
```

`accessToken` используется в заголовке `Authorization: Bearer <access-token>`.
`refreshToken` используется только для обновления токенов и logout.

Пример refresh:

```bash
curl -X POST "http://localhost:8090/api/v1/auth/refresh" \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"<refresh-token>\"}"
```

Пример logout:

```bash
curl -X POST "http://localhost:8090/api/v1/auth/logout" \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"<refresh-token>\"}"
```

Основные JWT-настройки через переменные окружения:

- `JWT_SIGNING_KEY`
- `JWT_REFRESH_SIGNING_KEY`
- `JWT_ACCESS_EXPIRATION`
- `JWT_REFRESH_EXPIRATION`
- `JWT_ISSUER`
- `JWT_AUDIENCE`

## Тесты

```bash
./mvnw test
```

# URL Shortener

A modern URL shortening service with analytics, built with Next.js 15 and Spring Boot.

![URL Shortener](https://www.svgrepo.com/svg/461845/link-alt?height=400&width=800)

## Features

- 🔗 URL shortening with custom short codes
- 📊 Detailed analytics for each shortened URL
- 👤 User authentication and authorization
- 📱 Responsive design with dark mode support
- 🔒 Secure JWT-based authentication
- 🐳 Docker support for easy deployment

## Tech Stack

### Frontend
- Next.js 15.2.1
- TypeScript
- Tailwind CSS
- shadcn/ui components
- Chart.js for analytics visualization

### Backend
- Spring Boot
- Spring Security with JWT
- JPA/Hibernate
- PostgreSQL

## Project Structure
```
url-shortener/
├── frontend/               # Next.js application
│   ├── app/                # Source code
│   ├── public/             # Static assets
│   ├── Dockerfile          # Frontend container definition
│   └── ...                 # Other configuration files
│
├── backend/                # Spring Boot application
│   ├── src/                # Source code
│   ├── Dockerfile          # Backend container definition
│   └── ...                 # Other configuration files
│
├── docker-compose.yml      # Container orchestration
├── .gitignore              # Git ignore file
└── README.md               # This file
```


## Getting Started

### Prerequisites

- Node.js 20+ and npm
- Java 17+
- Maven
- PostgreSQL
- Docker and Docker Compose

### Environment Variables
```
      - SPRING_TEST_PORT
      - APP_CONTEXT_PATH
      - LOCAL_HOST
      - SPRING_PORT
      - KEY_ALPHABETS
      - KEY_LENGTH
      - LOCAL_POSTGRES_BASE_URL
      - URL_SHORTENER_DB
      - DEV_POSTGRES_USER
      - DEV_POSTGRES_PASSWORD
      - DEV_POSTGRES_PORT
      - JWT_SECRET_KEY
      - JWT_EXPIRATION_TIME
      - REDIS_PASSWORD
```

### Running with Docker

1. Build and start the containers:

```shellscript
docker-compose up -d
```

The backend will be available at [http://localhost:8080](http://localhost:8080)

The frontend will be available at [http://localhost:3000](http://localhost:3000)

2. Access the application at [http://localhost:3000](http://localhost:3000)


## API Endpoints

### Authentication

#### Register a new user

```plaintext
POST /auth/register
```

Request body:

```json
{
  "username": "username",
  "email": "user@example.com",
  "password": "Password123!"
}
```

Response:

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "username": "username"
}
```

#### Login

```plaintext
POST /auth/login
```

Request body:

```json
{
  "username": "username",
  "password": "Password123!"
}
```

Response:

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "username": "username"
}
```

#### Refresh Token

```plaintext
POST /auth/refresh?refreshToken={refreshToken}
```

Response:

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "username": "username"
}
```

#### Logout

```plaintext
POST /auth/logout
```

Headers:

```plaintext
Authorization: Bearer {accessToken}
```

Response: 204 No Content

### URL Management

#### Create a shortened URL

```plaintext
POST /urls/shorten
```

Headers:

```plaintext
Authorization: Bearer {accessToken}
```

Request body:

```json
{
  "longUrl": "https://example.com/very/long/url/that/needs/shortening"
}
```

Response:

```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "shortCode": "abc123",
  "shortUrl": "http://localhost:8080/abc123",
  "longUrl": "https://example.com/very/long/url/that/needs/shortening",
  "createdAt": "2023-01-01T12:00:00Z",
  "clicks": 0
}
```

#### Get all user URLs

```plaintext
GET /urls
```

Headers:

```plaintext
Authorization: Bearer {accessToken}
```

Response:

```json
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "shortCode": "abc123",
    "shortUrl": "http://localhost:8080/abc123",
    "longUrl": "https://example.com/very/long/url/that/needs/shortening",
    "createdAt": "2023-01-01T12:00:00Z",
    "clicks": 5
  },
  {
    "id": "223e4567-e89b-12d3-a456-426614174000",
    "shortCode": "def456",
    "shortUrl": "http://localhost:8080/def456",
    "longUrl": "https://example.com/another/long/url",
    "createdAt": "2023-01-02T12:00:00Z",
    "clicks": 10
  }
]
```

#### Get URL details

```plaintext
GET /urls/{shortCode}
```

Headers:

```plaintext
Authorization: Bearer {accessToken}
```

Response:

```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "shortCode": "abc123",
  "shortUrl": "http://localhost:8080/abc123",
  "longUrl": "https://example.com/very/long/url/that/needs/shortening",
  "createdAt": "2023-01-01T12:00:00Z",
  "clicks": 5
}
```

#### Delete URL

```plaintext
DELETE /urls/{shortCode}
```

Headers:

```plaintext
Authorization: Bearer {accessToken}
```

Response: 204 No Content

### Analytics

#### Get URL analytics

```plaintext
GET /urls/analytics/{shortCode}
```

Headers:

```plaintext
Authorization: Bearer {accessToken}
```

Response:

```json
{
  "urlId": "123e4567-e89b-12d3-a456-426614174000",
  "shortCode": "abc123",
  "shortUrl": "http://localhost:8080/abc123",
  "longUrl": "https://example.com/very/long/url/that/needs/shortening",
  "totalClicks": 5,
  "clicksByDay": {
    "2023-01-01": 2,
    "2023-01-02": 3
  },
  "referrerCounts": {
    "google.com": 3,
    "twitter.com": 1,
    "direct": 1
  },
  "browserCounts": {
    "Chrome": 3,
    "Firefox": 1,
    "Safari": 1
  }
}
```

### Redirection

#### Redirect to original URL

```plaintext
GET /{shortCode}
```

This endpoint redirects to the original URL and records analytics data.

## Frontend Pages

- `/` - Home page
- `/login` - Login page
- `/register` - Registration page
- `/dashboard` - User dashboard with URL management
- `/dashboard/analytics/{shortCode}` - Analytics for a specific URL


## Development

### Frontend

- Build: `npm run build`
- Lint: `npm run lint`
- Type check: `npm run type-check`


### Backend

- Build: `./mvnw clean package`
- Run tests: `./mvnw test`


## Docker

The project includes Docker configuration for both frontend and backend:

- `frontend/Dockerfile` - Next.js frontend
- `backend/Dockerfile` - Spring Boot backend
- `docker-compose.yml` - Orchestrates the entire application


## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request


## License

This project is licensed under the Apache License - see the LICENSE file for details.
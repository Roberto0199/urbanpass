# 🚌 UrbanPass — Transit Card Validation System

A professional backend REST API simulating a public transit card validation system, 
inspired by real-world transit systems like Transmetro in Guatemala City.

## 🛠️ Tech Stack

- **Java 17**
- **Spring Boot 3**
- **MySQL 8**
- **JPA / Hibernate**
- **Lombok**

## ✨ Features

- 👤 User registration and management
- 💳 Card issuance per user
- 💰 Balance recharge system
- 🚪 Turnstile validation with automatic fare deduction
- 🔒 Card blocking / unblocking
- 📊 Full transaction history with pagination
- 🔐 Pessimistic locking for concurrency control
- 📝 Failed transaction audit trail

## 🗄️ Database Schema
```
users ──< cards ──< transactions
              └──────────────────< stations
```

## 🚀 Getting Started

### Prerequisites
- Java 17+
- MySQL 8+
- Maven

### Setup

**1. Clone the repository**
```bash
git clone https://github.com/Roberto0199/urbanpass.git
cd urbanpass
```

**2. Create the database**
```sql
CREATE DATABASE urbanpass_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

**3. Configure credentials**
```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```
Edit `application.properties` with your MySQL credentials.

**4. Run the application**
```bash
./mvnw spring-boot:run
```
The API will be available at `http://localhost:8080`

## 🌐 API Endpoints

### Users
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/users` | Create user |
| GET | `/api/users/{id}` | Get user |
| GET | `/api/users` | List all users |
| POST | `/api/users/{id}/cards` | Issue card |
| GET | `/api/users/{id}/cards` | Get user cards |

### Cards
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/cards/{id}/recharge` | Recharge balance |
| POST | `/api/cards/{id}/validate` | 🚪 Turnstile validation |
| GET | `/api/cards/{id}/history` | Transaction history |
| PATCH | `/api/cards/{id}/block` | Block card |
| PATCH | `/api/cards/{id}/unblock` | Unblock card |

### Stations
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/stations` | List stations |

## 🔐 Concurrency Control

The system uses **Pessimistic Locking** to prevent double charges 
when the same card is validated simultaneously at multiple turnstiles.

Failed transactions are saved in a separate transaction 
(`REQUIRES_NEW`) to ensure audit trail integrity even when 
the main transaction rolls back.

## 📊 Transaction Types

| Type | Description |
|------|-------------|
| `RECHARGE` | Balance top-up |
| `VALIDATION` ✅ | Successful turnstile pass |
| `VALIDATION` ❌ | Failed attempt (blocked card / insufficient balance) |

## 👨‍💻 Author

Roberto — Backend Developer
Guatemala 🇬🇹
```

Cuando lo pegues hacé clic en **Commit new file** abajo con el mensaje:
```
docs: add professional README

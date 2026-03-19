# UrbanPass — Sistema de Validación de Tarjetas de Transporte

API REST backend que simula un sistema de tarjetas de transporte público,
inspirado en sistemas reales como Transmetro en Ciudad de Guatemala.

![CI](https://github.com/Roberto0199/urbanpass/actions/workflows/ci.yml/badge.svg)

---

##  Tech Stack

- Java 17
- Spring Boot 3.2.5
- Spring Security + JWT
- MySQL 8
- JPA / Hibernate
- Lombok
- Swagger / OpenAPI
- Docker + Docker Compose

---

## Funcionalidades

- Registro y autenticación de usuarios con JWT
- Roles de seguridad — ADMIN y USER
- Emisión de tarjetas por usuario
- Recarga de saldo
- Validación en torniquete con descuento automático de tarifa
- Bloqueo y desbloqueo de tarjetas (solo ADMIN)
- Historial de transacciones con paginación
- Auditoría de transacciones fallidas
- Control de concurrencia con bloqueo pesimista
- Documentación interactiva con Swagger UI

---

## Correr el proyecto con Docker

### Prerequisitos
- Docker Desktop instalado

### Pasos

**1. Clonar el repositorio**
```bash
git clone https://github.com/Roberto0199/urbanpass.git
cd urbanpass
```

**2. Crear el archivo de configuración**
```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

**3. Levantar con Docker Compose**
```bash
docker-compose up --build
```

La API estará disponible en: `http://localhost:8080`

---

## Documentación — Swagger UI

Una vez corriendo, entra a:
```
http://localhost:8080/swagger-ui/index.html
```

---

## Credenciales por defecto

Al iniciar la aplicación se crea automáticamente un usuario administrador:

| Campo | Valor |
|-------|-------|
| Email | admin@urbanpass.gt |
| Password | Admin1234! |
| Rol | ADMIN |

>  Cambia la contraseña en producción.

---

## Endpoints

### Autenticación — públicos
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/auth/register` | Registrar usuario |
| POST | `/api/auth/login` | Iniciar sesión |

### Usuarios
| Método | Endpoint | Descripción | Rol |
|--------|----------|-------------|-----|
| POST | `/api/users` | Crear usuario | ADMIN |
| GET | `/api/users` | Listar usuarios | ADMIN |
| GET | `/api/users/{id}` | Ver usuario | USER |
| POST | `/api/users/{id}/cards` | Emitir tarjeta | ADMIN |
| GET | `/api/users/{id}/cards` | Ver tarjetas | USER |

### Tarjetas
| Método | Endpoint | Descripción | Rol |
|--------|----------|-------------|-----|
| POST | `/api/cards/{id}/recharge` | Recargar saldo | USER |
| POST | `/api/cards/{id}/validate` | Validar torniquete | USER |
| GET | `/api/cards/{id}/history` | Historial | USER |
| PATCH | `/api/cards/{id}/block` | Bloquear tarjeta | ADMIN |
| PATCH | `/api/cards/{id}/unblock` | Desbloquear tarjeta | ADMIN |

### Estaciones
| Método | Endpoint | Descripción | Rol |
|--------|----------|-------------|-----|
| GET | `/api/stations` | Listar estaciones | USER |

---

##  Esquema de base de datos
```
users ──< cards ──< transactions
               └──────────────── stations
```

---

##  Seguridad

- Autenticación stateless con JWT
- Roles ADMIN y USER con `@PreAuthorize`
- Bloqueo pesimista para evitar cobros dobles
- Contraseñas encriptadas con BCrypt

---

##  Tests

- Tests unitarios del servicio con Mockito
- Tests de controladores con MockMvc y `@WebMvcTest`
- Cobertura de casos exitosos, errores de validación y control de acceso

---

## Autor
**Roberto** — Backend Developer
Guatemala 🇬🇹

[![GitHub](https://img.shields.io/badge/GitHub-Roberto0199-black?logo=github)](https://github.com/Roberto0199)
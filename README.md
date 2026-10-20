# E-Commerce 

Single-store e-commerce platform.

## Stack
- Backend: Spring Boot 3.x, Java 17+
- Database: PostgreSQL
- Auth: Spring Security + JWT
- Frontend: React

## Roles
- **Admin** (1 per store) — manage everything, oversee all sellers/products/orders
- **Seller** (many) — manage own products only (isolated)
- **Customer** (many) — browse, add to cart, place orders, write reviews

## Features

### Auth
- Email + password registration/login
- JWT tokens, stateless sessions
- Role-based access (ADMIN, SELLER, CUSTOMER)

### Products
- CRUD by Seller
- Fields: name, description, price, stock, weight, category, seller_id
- Flat category list
- Physical goods only (shipping required)

### Cart
- Add/remove/update quantity
- Per-user cart, persists across sessions
- Price snapshot at add-time

### Orders
- Checkout converts cart to order
- COD (Cash on Delivery)
- Status flow: `PENDING → CONFIRMED → SHIPPED → DELIVERED` (+ `CANCELLED`)
- Order contains: items (with price snapshot), shipping address, total, status, timestamps
- Seller sees orders containing their products; Admin sees all

### Reviews
- Rating (1-5) + text
- One review per product per user
- Only users who purchased the product can review

## Database Schema (Core Entities)

- `users` (id, email, password_hash, role, created_at)
- `products` (id, seller_id, name, description, price, stock, weight, category, created_at)
- `carts` (id, user_id)
- `cart_items` (id, cart_id, product_id, quantity)
- `orders` (id, user_id, status, shipping_address, total, created_at, updated_at)
- `order_items` (id, order_id, product_id, quantity, price_snapshot)
- `reviews` (id, user_id, product_id, order_id, rating, comment, created_at)

The schema is created and updated by Hibernate (`ddl-auto`), so there are no migration files.

## Running with Docker

Prerequisite: Docker Desktop, or Docker Engine with Compose v2.

```bash
docker compose up --build
```

This starts three containers:

| Service | URL | Notes |
|---|---|---|
| frontend | http://localhost:3000 | nginx serving the React build; reverse-proxies `/api` to the backend |
| backend | http://localhost:8085 | Spring Boot API |
| postgres | localhost:5434 | `admin` / `secretpassword` / `ecommerce_db`, persisted in the `ecommerce_postgres_data` volume |

Open http://localhost:3000. 

### Creating an admin

The database ships **empty** — there is no seed data — and `ADMIN` accounts **cannot be created
through registration**. `POST /api/auth/register` accepts only `SELLER` and `CUSTOMER`; requesting
`ADMIN` returns `403 Forbidden`, and unknown roles return `400 Bad Request`. Role names are matched
case-insensitively. Administrators are provisioned directly in the database.

The simplest route is to register an ordinary account with the password you want — so the
application does the BCrypt hashing for you — and then promote it:

```bash
docker compose exec postgres psql -U admin -d ecommerce_db \
  -c "UPDATE users SET role = 'ADMIN' WHERE email = 'you@store.com';"
```

On Render, run the same statement from the database's **psql shell** (Dashboard → your Postgres
instance → Shell).

```sql
INSERT INTO users (email, password_hash, role, created_at)
VALUES ('admin@store.com', '<bcrypt-hash>', 'ADMIN', NOW());
```

Sellers and customers register normally through the UI.

## Configuration

Every backend setting is read from an environment variable with a default that matches local
development, so nothing needs to be configured just to run the stack.

| Variable | Default | Purpose |
|---|---|---|
| `DATABASE_URL` | *(unset)* | `postgresql://user:pass@host:port/db`. When set it takes precedence over `DB_*`. Injected by Render; translated to a JDBC URL by `DataSourceConfig` |
| `DB_URL` | `jdbc:postgresql://localhost:5434/ecommerce_db` | JDBC URL |
| `DB_USERNAME` | `admin` | Database user |
| `DB_PASSWORD` | `secretpassword` | Database password |
| `JWT_SECRET` | dev value in `application.yml` | Base64-encoded, at least 256 bits |
| `JWT_EXPIRATION` | `86400000` | Token lifetime in ms |
| `JPA_DDL_AUTO` | `update` | Hibernate schema mode |
| `PORT` | `8085` | HTTP port; Render injects this automatically |
| `BACKEND_URL` | `http://backend:8085` | Frontend only: origin nginx proxies `/api` to. **No trailing slash** |

Copy `.env.example` to `.env` to override compose values.

## Local development without Docker

```bash
docker compose up postgres -d          # database only, on localhost:5434
cd backend && mvn spring-boot:run      # API on :8085
cd frontend && npm ci && npm run dev   # UI on :3000, proxies /api to :8085
```
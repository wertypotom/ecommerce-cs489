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

export type Role = 'ADMIN' | 'SELLER' | 'CUSTOMER';

export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';

export interface User {
  id: number;
  email: string;
  role: Role;
  fullName?: string;
  createdAt?: string;
}

export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  stock: number;
  weight: number;
  category: string;
  userId: number;
  createdAt: string;
}

export interface CartItem {
  id: number;
  productId: number;
  productName: string;
  quantity: number;
  priceSnapshot: number;
  subtotal: number;
}

export interface Cart {
  id: number;
  userId: number;
  items: CartItem[];
  total: number;
}

export interface OrderItem {
  id: number;
  productId: number;
  productName: string;
  quantity: number;
  priceSnapshot: number;
  subtotal: number;
}

export interface Order {
  id: number;
  status: OrderStatus;
  shippingAddress: string;
  total: number;
  userId: number;
  items: OrderItem[];
  createdAt: string;
  updatedAt: string;
}

export interface Review {
  id: number;
  userId: number;
  userEmail: string;
  productId: number;
  productName: string;
  orderId: number;
  rating: number;
  comment: string;
  createdAt: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  role: Role;
}

export interface ProductRequest {
  name: string;
  description: string;
  price: number;
  stock: number;
  weight: number;
  category: string;
}

export interface CartItemRequest {
  productId: number;
  quantity: number;
}

export interface CheckoutRequest {
  shippingAddress: string;
}

export interface ReviewRequest {
  productId: number;
  orderId: number;
  rating: number;
  comment: string;
}

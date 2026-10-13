import apiClient from './client';
import type { Cart, CartItem, CartItemRequest } from '../types';

export const cartApi = {
  getByUserId: async (userId: number): Promise<Cart> => {
    const response = await apiClient.get<Cart>(`/carts/user/${userId}`);
    return response.data;
  },

  getById: async (id: number): Promise<Cart> => {
    const response = await apiClient.get<Cart>(`/carts/${id}`);
    return response.data;
  },

  addItem: async (cartId: number, data: CartItemRequest): Promise<CartItem> => {
    const response = await apiClient.post<CartItem>(`/carts/${cartId}/items`, data);
    return response.data;
  },

  updateItem: async (cartId: number, itemId: number, data: CartItemRequest): Promise<CartItem> => {
    const response = await apiClient.put<CartItem>(`/carts/${cartId}/items/${itemId}`, data);
    return response.data;
  },

  removeItem: async (cartId: number, itemId: number): Promise<void> => {
    await apiClient.delete(`/carts/${cartId}/items/${itemId}`);
  },

  clear: async (cartId: number): Promise<void> => {
    await apiClient.delete(`/carts/${cartId}`);
  },
};

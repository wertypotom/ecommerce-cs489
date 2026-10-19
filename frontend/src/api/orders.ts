import apiClient from './client';
import type { CheckoutRequest, Order, OrderStatus } from '../types';

export const orderApi = {
  getAll: async (): Promise<Order[]> => {
    const response = await apiClient.get<Order[]>('/orders');
    return response.data;
  },

  getById: async (id: number): Promise<Order> => {
    const response = await apiClient.get<Order>(`/orders/${id}`);
    return response.data;
  },

  getByUser: async (userId: number): Promise<Order[]> => {
    const response = await apiClient.get<Order[]>(`/orders/user/${userId}`);
    return response.data;
  },

  checkout: async (data: CheckoutRequest): Promise<Order> => {
    const response = await apiClient.post<Order>('/orders/checkout', data);
    return response.data;
  },

  updateStatus: async (id: number, status: OrderStatus): Promise<Order> => {
    const response = await apiClient.put<Order>(`/orders/${id}/status`, null, { params: { status } });
    return response.data;
  },

  cancel: async (id: number): Promise<void> => {
    await apiClient.delete(`/orders/${id}`);
  },
};

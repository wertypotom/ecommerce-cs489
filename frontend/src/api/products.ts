import apiClient from './client';
import type { Product, ProductRequest } from '../types';

export const productApi = {
  getAll: async (params?: { category?: string; minPrice?: number; maxPrice?: number }): Promise<Product[]> => {
    const response = await apiClient.get<Product[]>('/products', { params });
    return response.data;
  },

  getById: async (id: number): Promise<Product> => {
    const response = await apiClient.get<Product>(`/products/${id}`);
    return response.data;
  },

  getBySeller: async (sellerId: number): Promise<Product[]> => {
    const response = await apiClient.get<Product[]>(`/products/seller/${sellerId}`);
    return response.data;
  },

  create: async (data: ProductRequest): Promise<Product> => {
    const response = await apiClient.post<Product>('/products', data);
    return response.data;
  },

  update: async (id: number, data: ProductRequest): Promise<Product> => {
    const response = await apiClient.put<Product>(`/products/${id}`, data);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete(`/products/${id}`);
  },
};

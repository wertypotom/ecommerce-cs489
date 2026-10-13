import apiClient from './client';
import type { Review, ReviewRequest } from '../types';

export const reviewApi = {
  getAll: async (): Promise<Review[]> => {
    const response = await apiClient.get<Review[]>('/reviews');
    return response.data;
  },

  getById: async (id: number): Promise<Review> => {
    const response = await apiClient.get<Review>(`/reviews/${id}`);
    return response.data;
  },

  getByProduct: async (productId: number): Promise<Review[]> => {
    const response = await apiClient.get<Review[]>(`/reviews/product/${productId}`);
    return response.data;
  },

  getByUser: async (userId: number): Promise<Review[]> => {
    const response = await apiClient.get<Review[]>(`/reviews/user/${userId}`);
    return response.data;
  },

  create: async (data: ReviewRequest): Promise<Review> => {
    const response = await apiClient.post<Review>('/reviews', data);
    return response.data;
  },

  update: async (id: number, data: ReviewRequest): Promise<Review> => {
    const response = await apiClient.put<Review>(`/reviews/${id}`, data);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete(`/reviews/${id}`);
  },
};

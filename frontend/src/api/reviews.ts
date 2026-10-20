import apiClient from './client';
import type { Review, ReviewRequest } from '../types';

export const reviewApi = {
  getByProduct: async (productId: number): Promise<Review[]> => {
    const response = await apiClient.get<Review[]>(`/reviews/product/${productId}`);
    return response.data;
  },

  create: async (data: ReviewRequest): Promise<Review> => {
    const response = await apiClient.post<Review>('/reviews', data);
    return response.data;
  },
};

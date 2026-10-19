import { createContext, useContext, useState, useEffect, useCallback, type ReactNode } from 'react';
import { cartApi } from '../api';
import { useAuth } from './AuthContext';
import type { Cart } from '../types';

interface CartContextType {
  cart: Cart | null;
  isLoading: boolean;
  refreshCart: () => Promise<void>;
  addToCart: (productId: number, quantity: number) => Promise<void>;
  updateQuantity: (itemId: number, quantity: number) => Promise<void>;
  removeItem: (itemId: number) => Promise<void>;
  clearCart: () => Promise<void>;
  itemCount: number;
}

const CartContext = createContext<CartContextType | undefined>(undefined);

export function CartProvider({ children }: { children: ReactNode }) {
  const [cart, setCart] = useState<Cart | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const { user, isAuthenticated } = useAuth();

  const refreshCart = useCallback(async () => {
    if (!isAuthenticated || !user) {
      setCart(null);
      return;
    }
    setIsLoading(true);
    try {
      const cartData = await cartApi.getByUserId(user.id);
      setCart(cartData);
    } catch {
      setCart(null);
    } finally {
      setIsLoading(false);
    }
  }, [isAuthenticated, user]);

  useEffect(() => {
    refreshCart();
  }, [refreshCart]);

  const addToCart = async (productId: number, quantity: number) => {
    if (!isAuthenticated || !user) return;
    let currentCart = cart;
    if (!currentCart) {
      try {
        currentCart = await cartApi.getByUserId(user.id);
        setCart(currentCart);
      } catch {
        return;
      }
    }
    await cartApi.addItem(currentCart.id, { productId, quantity });
    await refreshCart();
  };

  const updateQuantity = async (itemId: number, quantity: number) => {
    if (!cart) return;
    const item = cart.items.find(i => i.id === itemId);
    if (!item) return;
    await cartApi.updateItem(cart.id, itemId, { productId: item.productId, quantity });
    await refreshCart();
  };

  const removeItem = async (itemId: number) => {
    if (!cart) return;
    await cartApi.removeItem(cart.id, itemId);
    await refreshCart();
  };

  const clearCart = async () => {
    if (!cart) return;
    await cartApi.clear(cart.id);
    await refreshCart();
  };

  const itemCount = cart?.items.reduce((sum, item) => sum + item.quantity, 0) ?? 0;

  return (
    <CartContext.Provider value={{ cart, isLoading, refreshCart, addToCart, updateQuantity, removeItem, clearCart, itemCount }}>
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  const context = useContext(CartContext);
  if (context === undefined) {
    throw new Error('useCart must be used within a CartProvider');
  }
  return context;
}

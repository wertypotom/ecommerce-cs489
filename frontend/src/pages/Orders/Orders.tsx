import { useState, useEffect } from 'react';
import { orderApi } from '../../api';
import { useAuth } from '../../contexts/AuthContext';
import { Loading } from '../../components/Loading';
import type { Order, OrderStatus } from '../../types';
import styles from './Orders.module.css';

const STATUS_COLORS: Record<OrderStatus, string> = {
  PENDING: '#f5a623',
  CONFIRMED: '#0366d6',
  SHIPPED: '#6f42c1',
  DELIVERED: '#22863a',
  CANCELLED: '#e63946',
};

export function Orders() {
  const { user } = useAuth();
  const [orders, setOrders] = useState<Order[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    loadOrders();
  }, [user]);

  const loadOrders = async () => {
    if (!user) return;
    setIsLoading(true);
    try {
      const data = await orderApi.getByUser(user.id);
      setOrders(data);
    } catch {
      setError('Failed to load orders');
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading) return <Loading />;

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>My Orders</h1>

      {error && <p className={styles.error}>{error}</p>}

      {orders.length === 0 ? (
        <div className={styles.empty}>
          <p>You haven't placed any orders yet</p>
        </div>
      ) : (
        <div className={styles.orders}>
          {orders.map(order => (
            <div key={order.id} className={styles.order}>
              <div className={styles.orderHeader}>
                <div>
                  <span className={styles.orderId}>Order #{order.id}</span>
                  <span className={styles.date}>
                    {new Date(order.createdAt).toLocaleDateString()}
                  </span>
                </div>
                <span
                  className={styles.status}
                  style={{ background: STATUS_COLORS[order.status] }}
                >
                  {order.status}
                </span>
              </div>

              <div className={styles.items}>
                {order.items.map(item => (
                  <div key={item.id} className={styles.item}>
                    <span className={styles.itemName}>{item.productName}</span>
                    <span className={styles.itemQty}>x{item.quantity}</span>
                    <span className={styles.itemPrice}>${item.subtotal.toFixed(2)}</span>
                  </div>
                ))}
              </div>

              <div className={styles.orderFooter}>
                <div className={styles.address}>
                  <span className={styles.label}>Shipping to:</span>
                  <span>{order.shippingAddress}</span>
                </div>
                <div className={styles.total}>
                  <span>Total:</span>
                  <span>${order.total.toFixed(2)}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

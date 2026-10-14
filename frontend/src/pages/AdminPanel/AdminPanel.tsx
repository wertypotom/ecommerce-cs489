import { useState, useEffect } from 'react';
import { orderApi, productApi, userApi } from '../../api';
import { Loading } from '../../components/Loading';
import type { Order, Product, OrderStatus, User } from '../../types';
import styles from './AdminPanel.module.css';

export function AdminPanel() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<'orders' | 'products'>('orders');

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setIsLoading(true);
    try {
      const [ordersData, productsData, usersData] = await Promise.all([
        orderApi.getAll(),
        productApi.getAll(),
        userApi.getAll(),
      ]);
      setOrders(ordersData);
      setProducts(productsData);
      setUsers(usersData);
    } catch {
      // handle error
    } finally {
      setIsLoading(false);
    }
  };

  const handleStatusUpdate = async (orderId: number, status: OrderStatus) => {
    try {
      await orderApi.updateStatus(orderId, status);
      loadData();
    } catch {
      // handle error
    }
  };

  const handleDeleteProduct = async (productId: number) => {
    if (!confirm('Are you sure you want to delete this product?')) return;
    try {
      await productApi.delete(productId);
      loadData();
    } catch {
      // handle error
    }
  };

  const getUserInfo = (userId: number) => {
    return users.find(u => u.id === userId);
  };

  if (isLoading) return <Loading />;

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>Admin Panel</h1>

      <div className={styles.stats}>
        <div className={styles.stat}>
          <span className={styles.statValue}>{products.length}</span>
          <span className={styles.statLabel}>Products</span>
        </div>
        <div className={styles.stat}>
          <span className={styles.statValue}>{orders.length}</span>
          <span className={styles.statLabel}>Orders</span>
        </div>
        <div className={styles.stat}>
          <span className={styles.statValue}>
            {orders.filter(o => o.status === 'PENDING').length}
          </span>
          <span className={styles.statLabel}>Pending Orders</span>
        </div>
      </div>

      <div className={styles.tabs}>
        <button
          className={`${styles.tab} ${activeTab === 'orders' ? styles.activeTab : ''}`}
          onClick={() => setActiveTab('orders')}
        >
          Orders
        </button>
        <button
          className={`${styles.tab} ${activeTab === 'products' ? styles.activeTab : ''}`}
          onClick={() => setActiveTab('products')}
        >
          Products
        </button>
      </div>

      {activeTab === 'orders' && (
        <div className={styles.section}>
          <h2>All Orders</h2>
          {orders.length === 0 ? (
            <p className={styles.empty}>No orders yet</p>
          ) : (
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Customer</th>
                  <th>Total</th>
                  <th>Status</th>
                  <th>Date</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {orders.map(order => {
                  const customer = getUserInfo(order.userId);
                  return (
                  <tr key={order.id}>
                    <td>#{order.id}</td>
                    <td>
                      <div>{customer?.fullName || 'N/A'}</div>
                      <div style={{ fontSize: '0.875em', color: '#666' }}>
                        {customer?.email || 'N/A'}
                      </div>
                    </td>
                    <td>${order.total.toFixed(2)}</td>
                    <td>
                      <span className={`${styles.status} ${styles[order.status.toLowerCase()]}`}>
                        {order.status}
                      </span>
                    </td>
                    <td>{new Date(order.createdAt).toLocaleDateString()}</td>
                    <td>
                      <select
                        value={order.status}
                        onChange={(e) => handleStatusUpdate(order.id, e.target.value as OrderStatus)}
                        className={styles.select}
                      >
                        <option value="PENDING">Pending</option>
                        <option value="CONFIRMED">Confirmed</option>
                        <option value="SHIPPED">Shipped</option>
                        <option value="DELIVERED">Delivered</option>
                        <option value="CANCELLED">Cancelled</option>
                      </select>
                    </td>
                  </tr>
                  );
                })}
              </tbody>
            </table>
          )}
        </div>
      )}

      {activeTab === 'products' && (
        <div className={styles.section}>
          <h2>All Products</h2>
          {products.length === 0 ? (
            <p className={styles.empty}>No products yet</p>
          ) : (
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Name</th>
                  <th>Category</th>
                  <th>Price</th>
                  <th>Stock</th>
                  <th>Seller</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {products.map(product => {
                  const seller = getUserInfo(product.userId);
                  return (
                  <tr key={product.id}>
                    <td>#{product.id}</td>
                    <td>{product.name}</td>
                    <td>{product.category}</td>
                    <td>${product.price.toFixed(2)}</td>
                    <td>{product.stock}</td>
                    <td>
                      <div style={{ fontSize: '0.875em', color: '#666' }}>
                        {seller?.email || 'N/A'}
                      </div>
                    </td>
                    <td>
                      <button
                        onClick={() => handleDeleteProduct(product.id)}
                        className={styles.deleteBtn}
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                  );
                })}
              </tbody>
            </table>
          )}
        </div>
      )}
    </div>
  );
}

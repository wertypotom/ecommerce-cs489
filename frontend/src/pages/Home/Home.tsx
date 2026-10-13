import { Link } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import styles from './Home.module.css';

export function Home() {
  const { isAuthenticated, user } = useAuth();

  return (
    <div className={styles.container}>
      <section className={styles.hero}>
        <h1>Welcome to E-Commerce</h1>
        <p>Discover amazing products at great prices</p>
        <Link to="/products" className={styles.cta}>
          Shop Now
        </Link>
      </section>

      {isAuthenticated && (
        <section className={styles.quickLinks}>
          <h2>Quick Links</h2>
          <div className={styles.links}>
            <Link to="/products" className={styles.link}>
              <span className={styles.icon}>🛍️</span>
              <span>Browse Products</span>
            </Link>
            {user?.role === 'CUSTOMER' && (
              <>
                <Link to="/cart" className={styles.link}>
                  <span className={styles.icon}>🛒</span>
                  <span>My Cart</span>
                </Link>
                <Link to="/orders" className={styles.link}>
                  <span className={styles.icon}>📦</span>
                  <span>My Orders</span>
                </Link>
              </>
            )}
            {user?.role === 'SELLER' && (
              <Link to="/seller" className={styles.link}>
                <span className={styles.icon}>📊</span>
                <span>Seller Dashboard</span>
              </Link>
            )}
            {user?.role === 'ADMIN' && (
              <Link to="/admin" className={styles.link}>
                <span className={styles.icon}>⚙️</span>
                <span>Admin Panel</span>
              </Link>
            )}
          </div>
        </section>
      )}
    </div>
  );
}

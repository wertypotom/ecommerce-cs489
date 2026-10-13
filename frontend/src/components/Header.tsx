import { Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { useCart } from '../contexts/CartContext';
import styles from './Header.module.css';

export function Header() {
  const { user, isAuthenticated, logout } = useAuth();
  const { itemCount } = useCart();

  return (
    <header className={styles.header}>
      <div className={styles.container}>
        <Link to="/" className={styles.logo}>E-Commerce</Link>
        <nav className={styles.nav}>
          <Link to="/products">Products</Link>
          {isAuthenticated && user?.role === 'CUSTOMER' && (
            <>
              <Link to="/cart" className={styles.cartLink}>
                Cart {itemCount > 0 && <span className={styles.badge}>{itemCount}</span>}
              </Link>
              <Link to="/orders">Orders</Link>
            </>
          )}
          {isAuthenticated && user?.role === 'SELLER' && (
            <Link to="/seller">Dashboard</Link>
          )}
          {isAuthenticated && user?.role === 'ADMIN' && (
            <Link to="/admin">Admin</Link>
          )}
        </nav>
        <div className={styles.auth}>
          {isAuthenticated ? (
            <>
              <span className={styles.userEmail}>{user?.email}</span>
              <button onClick={logout} className={styles.logoutBtn}>Logout</button>
            </>
          ) : (
            <>
              <Link to="/login" className={styles.loginBtn}>Login</Link>
              <Link to="/register" className={styles.registerBtn}>Register</Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
}

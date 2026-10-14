import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { orderApi } from '../../api';
import { useAuth } from '../../contexts/AuthContext';
import { useCart } from '../../contexts/CartContext';
import styles from './Checkout.module.css';

export function Checkout() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { cart, clearCart } = useCart();
  const [shippingAddress, setShippingAddress] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  if (!cart || cart.items.length === 0) {
    navigate('/cart');
    return null;
  }

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!user) return;

    setIsLoading(true);
    setError('');

    try {
      await orderApi.checkout({
        shippingAddress,
        total: cart.total,
        userId: user.id,
      });
      await clearCart();
      navigate('/orders');
    } catch {
      setError('Failed to place order. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>Checkout</h1>

      <div className={styles.layout}>
        <form onSubmit={handleSubmit} className={styles.form}>
          {error && <p className={styles.error}>{error}</p>}

          <div className={styles.section}>
            <h2>Shipping Address</h2>
            <div className={styles.field}>
              <label htmlFor="address">Full Address</label>
              <textarea
                id="address"
                value={shippingAddress}
                onChange={(e) => setShippingAddress(e.target.value)}
                placeholder="Enter your complete shipping address"
                required
                rows={4}
              />
            </div>
          </div>

          <div className={styles.section}>
            <h2>Payment</h2>
            <p className={styles.cod}>Cash on Delivery (COD)</p>
          </div>

          <button type="submit" className={styles.submitBtn} disabled={isLoading}>
            {isLoading ? 'Placing Order...' : 'Place Order'}
          </button>
        </form>

        <div className={styles.summary}>
          <h2>Order Summary</h2>
          <div className={styles.items}>
            {cart.items.map(item => (
              <div key={item.id} className={styles.item}>
                <span className={styles.itemName}>{item.productName}</span>
                <span className={styles.itemQty}>x{item.quantity}</span>
                <span className={styles.itemPrice}>${item.subtotal.toFixed(2)}</span>
              </div>
            ))}
          </div>
          <div className={styles.total}>
            <span>Total</span>
            <span>${cart.total.toFixed(2)}</span>
          </div>
        </div>
      </div>
    </div>
  );
}

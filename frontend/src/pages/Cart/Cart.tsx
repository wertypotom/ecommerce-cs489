import { Link } from 'react-router-dom';
import { useCart } from '../../contexts/CartContext';
import { Loading } from '../../components/Loading';
import styles from './Cart.module.css';

export function Cart() {
  const { cart, isLoading, updateQuantity, removeItem } = useCart();

  if (isLoading) return <Loading />;

  if (!cart || cart.items.length === 0) {
    return (
      <div className={styles.container}>
        <div className={styles.empty}>
          <h2>Your cart is empty</h2>
          <Link to="/products" className={styles.continueBtn}>Continue Shopping</Link>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>Shopping Cart</h1>

      <div className={styles.layout}>
        <div className={styles.items}>
          {cart.items.map(item => (
            <div key={item.id} className={styles.item}>
              <div className={styles.itemImage}>
                <span className={styles.category}>{item.productName}</span>
              </div>
              <div className={styles.itemInfo}>
                <Link to={`/products/${item.productId}`} className={styles.itemName}>
                  {item.productName}
                </Link>
                <span className={styles.itemPrice}>${item.priceSnapshot.toFixed(2)}</span>
              </div>
              <div className={styles.quantity}>
                <button onClick={() => updateQuantity(item.id, item.quantity - 1)} disabled={item.quantity <= 1}>-</button>
                <span>{item.quantity}</span>
                <button onClick={() => updateQuantity(item.id, item.quantity + 1)}>+</button>
              </div>
              <div className={styles.subtotal}>
                ${item.subtotal.toFixed(2)}
              </div>
              <button
                className={styles.removeBtn}
                onClick={() => removeItem(item.id)}
              >
                Remove
              </button>
            </div>
          ))}
        </div>

        <div className={styles.summary}>
          <h2>Order Summary</h2>
          <div className={styles.summaryRow}>
            <span>Subtotal</span>
            <span>${cart.total.toFixed(2)}</span>
          </div>
          <div className={styles.summaryRow}>
            <span>Shipping</span>
            <span>Free</span>
          </div>
          <div className={styles.total}>
            <span>Total</span>
            <span>${cart.total.toFixed(2)}</span>
          </div>
          <Link to="/checkout" className={styles.checkoutBtn}>
            Proceed to Checkout
          </Link>
          <Link to="/products" className={styles.continueLink}>
            Continue Shopping
          </Link>
        </div>
      </div>
    </div>
  );
}

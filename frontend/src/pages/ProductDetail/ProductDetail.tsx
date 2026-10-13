import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { productApi, reviewApi } from '../../api';
import { useAuth } from '../../contexts/AuthContext';
import { useCart } from '../../contexts/CartContext';
import { Loading } from '../../components/Loading';
import type { Product, Review } from '../../types';
import styles from './ProductDetail.module.css';

export function ProductDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();
  const { addToCart } = useCart();
  const [product, setProduct] = useState<Product | null>(null);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [quantity, setQuantity] = useState(1);
  const [error, setError] = useState('');
  const [addingToCart, setAddingToCart] = useState(false);

  useEffect(() => {
    if (id) {
      loadProduct(parseInt(id));
    }
  }, [id]);

  const loadProduct = async (productId: number) => {
    setIsLoading(true);
    try {
      const [productData, reviewsData] = await Promise.all([
        productApi.getById(productId),
        reviewApi.getByProduct(productId),
      ]);
      setProduct(productData);
      setReviews(reviewsData);
    } catch {
      setError('Failed to load product');
    } finally {
      setIsLoading(false);
    }
  };

  const handleAddToCart = async () => {
    if (!product || !isAuthenticated) return;
    setAddingToCart(true);
    try {
      await addToCart(product.id, quantity);
      navigate('/cart');
    } catch {
      setError('Failed to add to cart');
    } finally {
      setAddingToCart(false);
    }
  };

  if (isLoading) return <Loading />;
  if (error) return <p className={styles.error}>{error}</p>;
  if (!product) return <p className={styles.error}>Product not found</p>;

  const isInStock = product.stock > 0;
  const isCustomer = user?.role === 'CUSTOMER';

  return (
    <div className={styles.container}>
      <div className={styles.product}>
        <div className={styles.image}>
          <span className={styles.category}>{product.category}</span>
        </div>
        <div className={styles.info}>
          <h1>{product.name}</h1>
          <p className={styles.description}>{product.description}</p>
          <div className={styles.meta}>
            <span className={styles.price}>${product.price.toFixed(2)}</span>
            <span className={`${styles.stock} ${isInStock ? styles.inStock : styles.outOfStock}`}>
              {isInStock ? `${product.stock} in stock` : 'Out of stock'}
            </span>
          </div>
          <div className={styles.details}>
            <span>Weight: {product.weight} kg</span>
          </div>

          {isCustomer && isAuthenticated && isInStock && (
            <div className={styles.actions}>
              <div className={styles.quantity}>
                <button onClick={() => setQuantity(Math.max(1, quantity - 1))}>-</button>
                <span>{quantity}</span>
                <button onClick={() => setQuantity(Math.min(product.stock, quantity + 1))}>+</button>
              </div>
              <button
                className={styles.addBtn}
                onClick={handleAddToCart}
                disabled={addingToCart}
              >
                {addingToCart ? 'Adding...' : 'Add to Cart'}
              </button>
            </div>
          )}
        </div>
      </div>

      <div className={styles.reviews}>
        <h2>Reviews ({reviews.length})</h2>
        {reviews.length === 0 ? (
          <p className={styles.noReviews}>No reviews yet</p>
        ) : (
          <div className={styles.reviewList}>
            {reviews.map(review => (
              <div key={review.id} className={styles.review}>
                <div className={styles.reviewHeader}>
                  <span className={styles.reviewer}>{review.userEmail}</span>
                  <span className={styles.rating}>{'★'.repeat(review.rating)}{'☆'.repeat(5 - review.rating)}</span>
                </div>
                <p className={styles.comment}>{review.comment}</p>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

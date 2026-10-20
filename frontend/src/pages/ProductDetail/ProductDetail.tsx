import { useState, useEffect, type FormEvent } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { isAxiosError } from 'axios';
import { orderApi, productApi, reviewApi } from '../../api';
import { useAuth } from '../../contexts/AuthContext';
import { useCart } from '../../contexts/CartContext';
import { Loading } from '../../components/Loading';
import type { Order, Product, Review } from '../../types';
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

  const [eligibleOrders, setEligibleOrders] = useState<Order[]>([]);
  const [showReviewForm, setShowReviewForm] = useState(false);
  const [selectedOrderId, setSelectedOrderId] = useState<number | null>(null);
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState('');
  const [reviewError, setReviewError] = useState('');
  const [submittingReview, setSubmittingReview] = useState(false);

  useEffect(() => {
    if (id) {
      loadProduct(parseInt(id));
    }
  }, [id]);

  const productId = product?.id;

  useEffect(() => {
    if (!isAuthenticated || !user || productId === undefined) {
      setEligibleOrders([]);
      return;
    }
    loadEligibleOrders(user.id, productId);
  }, [isAuthenticated, user, productId]);

  const loadProduct = async (targetId: number) => {
    setIsLoading(true);
    try {
      const [productData, reviewsData] = await Promise.all([
        productApi.getById(targetId),
        reviewApi.getByProduct(targetId),
      ]);
      setProduct(productData);
      setReviews(reviewsData);
    } catch {
      setError('Failed to load product');
    } finally {
      setIsLoading(false);
    }
  };

  const loadEligibleOrders = async (userId: number, targetProductId: number) => {
    try {
      const orders = await orderApi.getByUser(userId);
      const eligible = orders.filter(o =>
        o.items?.some(item => item.productId === targetProductId)
      );
      setEligibleOrders(eligible);
      setSelectedOrderId(eligible.length > 0 ? eligible[0].id : null);
    } catch {
      setEligibleOrders([]);
    }
  };

  const loadReviews = async (targetProductId: number) => {
    try {
      setReviews(await reviewApi.getByProduct(targetProductId));
    } catch {
      // keep existing list on failure
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

  const handleSubmitReview = async (e: FormEvent) => {
    e.preventDefault();
    if (!product || selectedOrderId === null) return;

    setSubmittingReview(true);
    setReviewError('');

    try {
      await reviewApi.create({
        productId: product.id,
        orderId: selectedOrderId,
        rating,
        comment,
      });
      setComment('');
      setRating(5);
      setShowReviewForm(false);
      await loadReviews(product.id);
    } catch (err) {
      const data = isAxiosError(err) ? err.response?.data : undefined;
      const fieldErrors = data?.details ? Object.values(data.details).join(', ') : undefined;
      setReviewError(data?.message || fieldErrors || 'Failed to submit review. Please try again.');
    } finally {
      setSubmittingReview(false);
    }
  };

  if (isLoading) return <Loading />;
  if (error) return <p className={styles.error}>{error}</p>;
  if (!product) return <p className={styles.error}>Product not found</p>;

  const isInStock = product.stock > 0;
  const isCustomer = user?.role === 'CUSTOMER';
  const hasReviewed = !!user && reviews.some(r => r.userId === user.id);
  const canReview = isAuthenticated && eligibleOrders.length > 0 && !hasReviewed;

  const averageRating = reviews.length > 0
    ? reviews.reduce((sum, r) => sum + r.rating, 0) / reviews.length
    : 0;

  const renderStars = (value: number) => {
    const filled = Math.round(value);
    return '★'.repeat(filled) + '☆'.repeat(5 - filled);
  };

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

          <div className={styles.ratingSummary}>
            <span
              className={styles.summaryStars}
              role="img"
              aria-label={reviews.length > 0 ? `Rated ${averageRating.toFixed(1)} out of 5` : 'Not rated yet'}
            >
              {renderStars(averageRating)}
            </span>
            <span className={styles.summaryValue}>
              {reviews.length > 0 ? averageRating.toFixed(1) : '—'}
            </span>
            <span className={styles.summaryCount}>
              {reviews.length} review{reviews.length === 1 ? '' : 's'}
            </span>
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
        <div className={styles.reviewsHeader}>
          <h2>Reviews ({reviews.length})</h2>
          {canReview && !showReviewForm && (
            <button
              type="button"
              className={styles.writeReviewBtn}
              onClick={() => {
                setReviewError('');
                setShowReviewForm(true);
              }}
            >
              Write a Review
            </button>
          )}
        </div>

        {!isAuthenticated && (
          <p className={styles.noReviews}>
            <Link to="/login">Sign in</Link> to review this product.
          </p>
        )}

        {isAuthenticated && hasReviewed && (
          <p className={styles.noReviews}>You have already reviewed this product.</p>
        )}

        {canReview && showReviewForm && (
          <form className={styles.reviewForm} onSubmit={handleSubmitReview}>
            <div className={styles.field}>
              <label>Rating</label>
              <div className={styles.starPicker}>
                {[1, 2, 3, 4, 5].map(star => (
                  <button
                    key={star}
                    type="button"
                    className={`${styles.star} ${star <= rating ? styles.starActive : ''}`}
                    onClick={() => setRating(star)}
                    aria-label={`${star} star${star > 1 ? 's' : ''}`}
                  >
                    ★
                  </button>
                ))}
              </div>
            </div>

            {eligibleOrders.length > 1 && (
              <div className={styles.field}>
                <label htmlFor="review-order">Purchase</label>
                <select
                  id="review-order"
                  value={selectedOrderId ?? ''}
                  onChange={(e) => setSelectedOrderId(parseInt(e.target.value))}
                >
                  {eligibleOrders.map(order => (
                    <option key={order.id} value={order.id}>
                      Order #{order.id} — {order.status}
                    </option>
                  ))}
                </select>
              </div>
            )}

            <div className={styles.field}>
              <label htmlFor="review-comment">Comment</label>
              <textarea
                id="review-comment"
                value={comment}
                onChange={(e) => setComment(e.target.value)}
                rows={3}
                placeholder="Share your experience with this product"
              />
            </div>

            {reviewError && <p className={styles.reviewError}>{reviewError}</p>}

            <div className={styles.reviewActions}>
              <button type="button" onClick={() => setShowReviewForm(false)}>
                Cancel
              </button>
              <button
                type="submit"
                className={styles.submitReviewBtn}
                disabled={submittingReview}
              >
                {submittingReview ? 'Submitting...' : 'Submit Review'}
              </button>
            </div>
          </form>
        )}

        {reviews.length === 0 ? (
          <p className={styles.noReviews}>No reviews yet</p>
        ) : (
          <div className={styles.reviewList}>
            {reviews.map(review => (
              <div key={review.id} className={styles.review}>
                <div className={styles.reviewHeader}>
                  <span className={styles.reviewer}>{review.userEmail}</span>
                  <span className={styles.rating}>{renderStars(review.rating)}</span>
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

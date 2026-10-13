import { Link } from 'react-router-dom';
import type { Product } from '../types';
import styles from './ProductCard.module.css';

interface ProductCardProps {
  product: Product;
}

export function ProductCard({ product }: ProductCardProps) {
  const isInStock = product.stock > 0;

  return (
    <Link to={`/products/${product.id}`} className={styles.card}>
      <div className={styles.imagePlaceholder}>
        <span className={styles.category}>{product.category}</span>
      </div>
      <div className={styles.content}>
        <h3 className={styles.name}>{product.name}</h3>
        <p className={styles.description}>{product.description}</p>
        <div className={styles.footer}>
          <span className={styles.price}>${product.price.toFixed(2)}</span>
          <span className={`${styles.stock} ${isInStock ? styles.inStock : styles.outOfStock}`}>
            {isInStock ? `In Stock (${product.stock})` : 'Out of Stock'}
          </span>
        </div>
      </div>
    </Link>
  );
}

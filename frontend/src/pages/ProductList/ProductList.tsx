import { useState, useEffect } from 'react';
import { productApi } from '../../api';
import { ProductCard } from '../../components/ProductCard';
import { Loading } from '../../components/Loading';
import type { Product } from '../../types';
import styles from './ProductList.module.css';

export function ProductList() {
  const [products, setProducts] = useState<Product[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [category, setCategory] = useState('');
  const [categories, setCategories] = useState<string[]>([]);

  useEffect(() => {
    loadProducts();
  }, [category]);

  const loadProducts = async () => {
    setIsLoading(true);
    try {
      const data = await productApi.getAll(category ? { category } : undefined);
      setProducts(data);
      const uniqueCategories = [...new Set(data.map(p => p.category))].filter(Boolean);
      setCategories(uniqueCategories);
    } catch {
      setError('Failed to load products');
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading) return <Loading />;

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h1>Products</h1>
        <div className={styles.filters}>
          <select
            value={category}
            onChange={(e) => setCategory(e.target.value)}
            className={styles.select}
          >
            <option value="">All Categories</option>
            {categories.map(cat => (
              <option key={cat} value={cat}>{cat}</option>
            ))}
          </select>
        </div>
      </div>

      {error && <p className={styles.error}>{error}</p>}

      {products.length === 0 ? (
        <p className={styles.empty}>No products found</p>
      ) : (
        <div className={styles.grid}>
          {products.map(product => (
            <ProductCard key={product.id} product={product} />
          ))}
        </div>
      )}
    </div>
  );
}

import { useState, useEffect } from 'react';
import { productApi } from '../../api';
import { useAuth } from '../../contexts/AuthContext';
import { Loading } from '../../components/Loading';
import type { Product, ProductRequest } from '../../types';
import styles from './SellerDashboard.module.css';
import { Link } from 'react-router-dom';

const defaultFormData: ProductRequest = {
  name: '',
  description: '',
  price: 0,
  stock: 0,
  weight: 0,
  category: '',
}

export function SellerDashboard() {
  const { user } = useAuth();
  const [products, setProducts] = useState<Product[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);
  const [formData, setFormData] = useState<ProductRequest>(defaultFormData);

  useEffect(() => {
    if (user) {
      loadProducts();
    }
  }, [user]);

  const loadProducts = async () => {
    if (!user) return;
    setIsLoading(true);
    try {
      const data = await productApi.getBySeller(user.id);
      setProducts(data);
    } catch (error) {
      console.log("Error in loadProducts 💥", (error as Error).message)
    } finally {
      setIsLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editingProduct) {
        await productApi.update(editingProduct.id, formData);
      } else {
        await productApi.create(formData);
      }
      setShowForm(false);
      setEditingProduct(null);
      setFormData(defaultFormData);
      loadProducts();
    } catch (error) {
      console.log("Error in handleSubmit 💥", (error as Error).message)
    }
  };

  const handleEdit = (product: Product) => {
    setEditingProduct(product);
    setFormData({
      name: product.name,
      description: product.description,
      price: product.price,
      stock: product.stock,
      weight: product.weight,
      category: product.category,
    });
    setShowForm(true);
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to delete this product?')) return;
    try {
      await productApi.delete(id);
      loadProducts();
    } catch (error) {
      console.log("Error in handleDelete 💥", (error as Error).message)
    }
  };

  const handleCancel = () => {
    setShowForm(false); 
    setEditingProduct(null); 
    setFormData(defaultFormData);
  }

  if (isLoading) return <Loading />;

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h1>My Products</h1>
        <button className={styles.addBtn} onClick={() => setShowForm(true)}>
          Add Product
        </button>
      </div>

      {showForm && (
        <div className={styles.modal}>
          <form onSubmit={handleSubmit} className={styles.form}>
            <h2>{editingProduct ? 'Edit Product' : 'New Product'}</h2>
            <div className={styles.field}>
              <label>Name</label>
              <input
                type="text"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                required
              />
            </div>
            <div className={styles.field}>
              <label>Description</label>
              <textarea
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                required
                rows={3}
              />
            </div>
            <div className={styles.row}>
              <div className={styles.field}>
                <label>Price</label>
                <input
                  type="number"
                  step="0.01"
                  value={formData.price}
                  onChange={(e) => setFormData({ ...formData, price: parseFloat(e.target.value) })}
                  required
                />
              </div>
              <div className={styles.field}>
                <label>Stock</label>
                <input
                  type="number"
                  value={formData.stock}
                  onChange={(e) => setFormData({ ...formData, stock: parseInt(e.target.value) })}
                  required
                />
              </div>
            </div>
            <div className={styles.row}>
              <div className={styles.field}>
                <label>Weight (kg)</label>
                <input
                  type="number"
                  step="0.1"
                  value={formData.weight}
                  onChange={(e) => setFormData({ ...formData, weight: parseFloat(e.target.value) })}
                  required
                />
              </div>
              <div className={styles.field}>
                <label>Category</label>
                <input
                  type="text"
                  value={formData.category}
                  onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                  required
                />
              </div>
            </div>
            <div className={styles.actions}>
              <button type="button" onClick={handleCancel}>
                Cancel
              </button>
              <button type="submit" className={styles.submitBtn}>
                {editingProduct ? 'Update' : 'Create'}
              </button>
            </div>
          </form>
        </div>
      )}

      {products.length === 0 ? (
        <div className={styles.empty}>
          <p>You haven't added any products yet</p>
        </div>
      ) : (
        <div className={styles.products}>
          {products.map(product => (
            <Link to={`/products/${product.id}`} key={product.id} className={styles.product}>
              <div className={styles.productInfo}>
                <h3>{product.name}</h3>
                <p className={styles.category}>{product.category}</p>
                <div className={styles.meta}>
                  <span>${product.price.toFixed(2)}</span>
                  <span>Stock: {product.stock}</span>
                </div>
              </div>
              <div className={styles.productActions}>
                <button onClick={() => handleEdit(product)}>Edit</button>
                <button onClick={() => handleDelete(product.id)} className={styles.deleteBtn}>
                  Delete
                </button>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}

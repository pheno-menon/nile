import { useState, useEffect, useCallback } from "react";
import API from "../api/axios";
import DashboardNavbar from "../components/DashboardNavbar";

interface Product {
  id: number;
  name: string;
  price: number;
  stockQuantity: number;
}

interface User {
  id: number;
  name: string;
  email: string;
  role: string;
}

type Tab = "products" | "users";

const emptyForm = { name: "", price: "", stock: "" };

export default function AdminDashboard() {
  const [tab, setTab] = useState<Tab>("products");

  // Products state
  const [products, setProducts] = useState<Product[]>([]);
  const [productForm, setProductForm] = useState(emptyForm);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);
  const [productLoading, setProductLoading] = useState(true);

  // Users state
  const [users, setUsers] = useState<User[]>([]);
  const [userLoading, setUserLoading] = useState(true);

  const [confirmModal, setConfirmModal] = useState<{
    message: string;
    onConfirm: () => void;
  } | null>(null);

  // Get products
  const fetchProducts = useCallback(() => {
    setProductLoading(true);
    API.get("/api/products")
      .then((res) => setProducts(res.data))
      .catch(() => alert("Failed to load products"))
      .finally(() => setProductLoading(false));
  }, []);

  // Get users
  const fetchUsers = useCallback(() => {
    setUserLoading(true);
    API.get("/api/admin/users")
      .then((res) => setUsers(res.data))
      .catch(() => alert("Failed to load users"))
      .finally(() => setUserLoading(false));
  }, []);

  useEffect(() => { fetchProducts(); }, [fetchProducts]);
  useEffect(() => { if (tab === "users") fetchUsers(); }, [tab, fetchUsers]);

  const handleProductSubmit = async () => {
    if (!productForm.name || !productForm.price || !productForm.stock) {
      alert("Please fill in all fields");
      return;
    }
    const payload = {
      name: productForm.name,
      price: parseFloat(productForm.price),
      stock: parseInt(productForm.stock),
    };
    try {
      if (editingProduct) {
        await API.put(`/api/admin/products/${editingProduct.id}`, payload);
      } else {
        await API.post("/api/admin/products", payload);
      }
      setProductForm(emptyForm);
      setEditingProduct(null);
      fetchProducts();
    } catch {
      alert("Failed to save product");
    }
  };

  const handleEditProduct = (product: Product) => {
    setEditingProduct(product);
    setProductForm({
      name: product.name,
      price: product.price.toString(),
      stock: product.stockQuantity.toString(),
    });
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleDeleteProduct = (id: number) => {
    setConfirmModal({
      message: "Delete this product? This cannot be undone.",
      onConfirm: async () => {
        try {
          await API.delete(`/api/admin/products/${id}`);
          fetchProducts();
        } catch {
          alert("Failed to delete product");
        } finally {
          setConfirmModal(null);
        }
      },
    });
  };

  const handleCancelEdit = () => {
    setEditingProduct(null);
    setProductForm(emptyForm);
  };

  const handleDeleteUser = (id: number, name: string) => {
    setConfirmModal({
      message: `Delete user "${name}"? This cannot be undone.`,
      onConfirm: async () => {
        try {
          await API.delete(`/api/admin/users/${id}`);
          fetchUsers();
        } catch {
          alert("Failed to delete user");
        } finally {
          setConfirmModal(null);
        }
      },
    });
  };

  const inputClass =
    "w-full border border-black dark:border-white px-3 py-2 bg-white dark:bg-black text-black dark:text-white outline-none focus:outline-black dark:focus:outline-white focus:outline-offset-2 transition-all duration-200";

  return (
    <div className="min-h-screen flex flex-col bg-white dark:bg-black text-black dark:text-white transition-colors duration-300">
      <DashboardNavbar />

      <div className="max-w-6xl mx-auto w-full px-6 py-10">
        <h1 className="text-3xl font-bold mb-8">Admin Dashboard</h1>

        {/* Tabs */}
        <div className="flex gap-0 mb-8 border-b border-black dark:border-white">
          {(["products", "users"] as Tab[]).map((t) => (
            <button
              key={t}
              onClick={() => setTab(t)}
              className={`px-6 py-2 capitalize text-sm font-medium border-b-2 transition-all duration-200 -mb-px ${
                tab === t
                  ? "border-black dark:border-white text-black dark:text-white"
                  : "border-transparent text-gray-400 hover:text-black dark:hover:text-white"
              }`}
            >
              {t}
            </button>
          ))}
        </div>

        {/* ── Products Tab ── */}
        {tab === "products" && (
          <div className="flex flex-col gap-10">
            {/* Form */}
            <div className="border border-black dark:border-white p-6">
              <h2 className="text-lg font-semibold mb-4">
                {editingProduct ? `Editing: ${editingProduct.name}` : "Add New Product"}
              </h2>
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-4">
                <input
                  placeholder="Product name"
                  value={productForm.name}
                  onChange={(e) => setProductForm({ ...productForm, name: e.target.value })}
                  className={inputClass}
                />
                <input
                  placeholder="Price (e.g. 19.99)"
                  type="number"
                  min="0"
                  step="0.01"
                  value={productForm.price}
                  onChange={(e) => setProductForm({ ...productForm, price: e.target.value })}
                  className={inputClass}
                />
                <input
                  placeholder="Stock quantity"
                  type="number"
                  min="0"
                  value={productForm.stock}
                  onChange={(e) => setProductForm({ ...productForm, stock: e.target.value })}
                  className={inputClass}
                />
              </div>
              <div className="flex gap-3">
                <button
                  onClick={handleProductSubmit}
                  className="border px-6 py-2 border-black dark:border-white
                    hover:bg-black hover:text-white dark:hover:bg-white dark:hover:text-black
                    transition-all duration-200 font-medium"
                >
                  {editingProduct ? "Save Changes" : "Add Product"}
                </button>
                {editingProduct && (
                  <button
                    onClick={handleCancelEdit}
                    className="border px-6 py-2 border-black dark:border-white
                      hover:bg-black hover:text-white dark:hover:bg-white dark:hover:text-black
                      transition-all duration-200"
                  >
                    Cancel
                  </button>
                )}
              </div>
            </div>

            {/* Product list */}
            {productLoading ? (
              <p className="text-gray-500 dark:text-gray-400">Loading products...</p>
            ) : products.length === 0 ? (
              <p className="text-gray-500 dark:text-gray-400">No products yet.</p>
            ) : (
              <div className="flex flex-col gap-3">
                {products.map((product) => (
                  <div
                    key={product.id}
                    className={`border p-4 flex items-center justify-between gap-4 transition-all duration-200 ${
                      editingProduct?.id === product.id
                        ? "border-indigo-400"
                        : "border-black dark:border-white"
                    }`}
                  >
                    <div className="flex-1">
                      <p className="font-semibold">{product.name}</p>
                      <p className="text-sm text-gray-500 dark:text-gray-400">
                        ${product.price.toFixed(2)} &bull; {product.stockQuantity} in stock
                      </p>
                    </div>
                    <div className="flex gap-2">
                      <button
                        onClick={() => handleEditProduct(product)}
                        className="border px-4 py-1 text-sm border-black dark:border-white
                          hover:bg-black hover:text-white dark:hover:bg-white dark:hover:text-black
                          transition-all duration-200"
                      >
                        Edit
                      </button>
                      <button
                        onClick={() => handleDeleteProduct(product.id)}
                        className="border px-4 py-1 text-sm border-red-500 text-red-500
                          hover:bg-red-500 hover:text-white
                          transition-all duration-200"
                      >
                        Delete
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* ── Users Tab ── */}
        {tab === "users" && (
          <div>
            {userLoading ? (
              <p className="text-gray-500 dark:text-gray-400">Loading users...</p>
            ) : users.length === 0 ? (
              <p className="text-gray-500 dark:text-gray-400">No users found.</p>
            ) : (
              <div className="flex flex-col gap-3">
                {users.map((user) => (
                  <div
                    key={user.id}
                    className="border border-black dark:border-white p-4 flex items-center justify-between gap-4"
                  >
                    <div className="flex-1">
                      <p className="font-semibold">{user.name}</p>
                      <p className="text-sm text-gray-500 dark:text-gray-400">{user.email}</p>
                    </div>
                    <span
                      className={`text-xs font-medium px-2 py-0.5 border ${
                        user.role === "ROLE_ADMIN"
                          ? "border-indigo-400 text-indigo-400"
                          : "border-gray-400 text-gray-400"
                      }`}
                    >
                      {user.role === "ROLE_ADMIN" ? "ADMIN" : "USER"}
                    </span>
                    {user.role !== "ROLE_ADMIN" && (
                      <button
                        onClick={() => handleDeleteUser(user.id, user.name)}
                        className="border px-4 py-1 text-sm border-red-500 text-red-500
                          hover:bg-red-500 hover:text-white
                          transition-all duration-200"
                      >
                        Delete
                      </button>
                    )}
                    {user.role === "ROLE_ADMIN" && (
                      <span className="text-xs text-gray-400 px-4 py-1">Protected</span>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </div>

      {/* Confirm Modal */}
      {confirmModal && (
        <div
          className="fixed inset-0 flex items-center justify-center bg-black/30 backdrop-blur-sm z-50"
          onClick={() => setConfirmModal(null)}
        >
          <div
            onClick={(e) => e.stopPropagation()}
            className="bg-white dark:bg-black border border-black dark:border-white p-8 max-w-sm w-full shadow-2xl"
          >
            <p className="text-base mb-6">{confirmModal.message}</p>
            <div className="flex gap-3 justify-end">
              <button
                onClick={() => setConfirmModal(null)}
                className="border px-5 py-2 border-black dark:border-white
                  hover:bg-black hover:text-white dark:hover:bg-white dark:hover:text-black
                  transition-all duration-200"
              >
                Cancel
              </button>
              <button
                onClick={confirmModal.onConfirm}
                className="border px-5 py-2 border-red-500 text-red-500
                  hover:bg-red-500 hover:text-white
                  transition-all duration-200"
              >
                Confirm
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
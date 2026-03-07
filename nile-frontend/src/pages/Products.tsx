import { useState, useEffect } from "react";
import API from "../api/axios";
import DashboardNavbar from "../components/DashboardNavbar";

interface Product {
  id: number;
  name: string;
  price: number;
  stockQuantity: number;
}

export default function Products() {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [feedback, setFeedback] = useState<{ id: number; msg: string } | null>(null);

  useEffect(() => {
    API.get("/api/products")
      .then((res) => setProducts(res.data))
      .catch(() => alert("Failed to load products"))
      .finally(() => setLoading(false));
  }, []);

  const handleAddToCart = async (productId: number) => {
    const user = JSON.parse(localStorage.getItem("user")!);
    try {
      await API.post("/api/cart/add", {
        userId: user.id,
        productId,
        quantity: 1,
      });
      setFeedback({ id: productId, msg: "Added!" });
      setTimeout(() => setFeedback(null), 1500);
    } catch {
      alert("Failed to add to cart");
    }
  };

  return (
    <div className="min-h-screen flex flex-col bg-white dark:bg-black text-black dark:text-white transition-colors duration-300">
      <DashboardNavbar />

      <div className="max-w-7xl mx-auto w-full px-6 py-10">
        <h1 className="text-3xl font-bold mb-8">Products</h1>

        {loading ? (
          <p className="text-gray-500 dark:text-gray-400">Loading products...</p>
        ) : products.length === 0 ? (
          <p className="text-gray-500 dark:text-gray-400">No products available.</p>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {products.map((product) => (
              <div
                key={product.id}
                className="border border-black dark:border-white p-6 flex flex-col gap-3 transition-all duration-200 hover:shadow-lg"
              >
                <h2 className="text-lg font-semibold">{product.name}</h2>
                <p className="text-2xl font-bold">${product.price.toFixed(2)}</p>
                <p className="text-sm text-gray-500 dark:text-gray-400">
                  {product.stockQuantity > 0
                    ? `${product.stockQuantity} in stock`
                    : "Out of stock"}
                </p>
                <button
                  onClick={() => handleAddToCart(product.id)}
                  disabled={product.stockQuantity === 0}
                  className="mt-auto border px-4 py-2
                    border-black dark:border-white
                    hover:bg-black hover:text-white
                    dark:hover:bg-white dark:hover:text-black
                    disabled:opacity-40 disabled:cursor-not-allowed
                    transition-all duration-200"
                >
                  {feedback?.id === product.id ? feedback.msg : "Add to Cart"}
                </button>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
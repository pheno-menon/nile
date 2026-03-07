import { useState, useEffect, useCallback } from "react";
import API from "../api/axios";
import DashboardNavbar from "../components/DashboardNavbar";
import { useNavigate } from "react-router-dom";

interface CartItem {
  id: number;
  product: {
    id: number;
    name: string;
    price: number;
  };
  quantity: number;
}

export default function Cart() {
  const [cartItems, setCartItems] = useState<CartItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [placingOrder, setPlacingOrder] = useState(false);
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem("user")!);

  const fetchCart = useCallback(() => {
    setLoading(true);
    API.get(`/api/cart/${user.id}`)
      .then((res) => setCartItems(res.data))
      .catch(() => alert("Failed to load cart"))
      .finally(() => setLoading(false));
  }, [user.id]);

  useEffect(() => {
    fetchCart();
  }, [fetchCart]);

  const handleRemove = async (cartItemId: number) => {
    try {
      await API.delete(`/api/cart/${cartItemId}`);
      setCartItems((prev) => prev.filter((item) => item.id !== cartItemId));
    } catch {
      alert("Failed to remove item");
    }
  };

  const handlePlaceOrder = async () => {
    setPlacingOrder(true);
    try {
      await API.post(`/api/orders/place/${user.id}`);
      setCartItems([]);
      navigate("/orders");
    } catch {
      alert("Failed to place order");
    } finally {
      setPlacingOrder(false);
    }
  };

  const total = cartItems.reduce(
    (sum, item) => sum + item.product.price * item.quantity,
    0
  );

  return (
    <div className="min-h-screen flex flex-col bg-white dark:bg-black text-black dark:text-white transition-colors duration-300">
      <DashboardNavbar />

      <div className="max-w-4xl mx-auto w-full px-6 py-10">
        <h1 className="text-3xl font-bold mb-8">Your Cart</h1>

        {loading ? (
          <p className="text-gray-500 dark:text-gray-400">Loading cart...</p>
        ) : cartItems.length === 0 ? (
          <div className="text-center py-20">
            <p className="text-gray-500 dark:text-gray-400 mb-6">Your cart is empty.</p>
            <button
              onClick={() => navigate("/products")}
              className="border px-6 py-2
                border-black dark:border-white
                hover:bg-black hover:text-white
                dark:hover:bg-white dark:hover:text-black
                transition-all duration-200"
            >
              Browse Products
            </button>
          </div>
        ) : (
          <>
            <div className="flex flex-col gap-4 mb-8">
              {cartItems.map((item) => (
                <div
                  key={item.id}
                  className="border border-black dark:border-white p-5 flex items-center justify-between gap-4"
                >
                  <div className="flex-1">
                    <p className="font-semibold text-lg">{item.product.name}</p>
                    <p className="text-sm text-gray-500 dark:text-gray-400">
                      Qty: {item.quantity} &times; ${item.product.price.toFixed(2)}
                    </p>
                  </div>
                  <p className="font-bold text-lg">
                    ${(item.product.price * item.quantity).toFixed(2)}
                  </p>
                  <button
                    onClick={() => handleRemove(item.id)}
                    className="border px-3 py-1 text-sm
                      border-black dark:border-white
                      hover:bg-black hover:text-white
                      dark:hover:bg-white dark:hover:text-black
                      transition-all duration-200"
                  >
                    Remove
                  </button>
                </div>
              ))}
            </div>

            {/* Summary */}
            <div className="border-t border-black dark:border-white pt-6 flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-500 dark:text-gray-400">Order Total</p>
                <p className="text-3xl font-bold">${total.toFixed(2)}</p>
              </div>
              <button
                onClick={handlePlaceOrder}
                disabled={placingOrder}
                className="border px-8 py-3 font-semibold
                  border-black dark:border-white
                  hover:bg-black hover:text-white
                  dark:hover:bg-white dark:hover:text-black
                  disabled:opacity-40 disabled:cursor-not-allowed
                  transition-all duration-200"
              >
                {placingOrder ? "Placing..." : "Place Order"}
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
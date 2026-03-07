import { useState, useEffect } from "react";
import API from "../api/axios";
import DashboardNavbar from "../components/DashboardNavbar";
import { useNavigate } from "react-router-dom";

interface OrderItem {
  productName: string;
  quantity: number;
  price: number;
}

interface Order {
  id: number;
  status: string;
  orderDate: string | number[];
  items: OrderItem[];
  totalAmount: number;
}

export default function Orders() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [expanded, setExpanded] = useState<number | null>(null);
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem("user")!);

  useEffect(() => {
    API.get(`/api/orders/user/${user.id}`)
      .then((res) => setOrders(res.data))
      .catch(() => alert("Failed to load orders"))
      .finally(() => setLoading(false));
  }, [user.id]);

  const toggleExpand = (id: number) => {
    setExpanded((prev) => (prev === id ? null : id));
  };

  const parseDate = (dateVal: string | number[]): Date | null => {
    if (!dateVal) return null;
    const date = Array.isArray(dateVal) 
    ? new Date(dateVal[0], dateVal[1] - 1, dateVal[2], dateVal[3] ?? 0, dateVal[4] ?? 0)
    : new Date(dateVal);
    return isNaN(date.getTime()) ? null : date;
  };

  // Spring's LocalDateTime serialises as either an ISO string or a [y,m,d,h,min,s] array
  const formatDate = (dateVal: string | number[]) => {
    const date = parseDate(dateVal);
    if (!date) return "-";
    return date.toLocaleDateString(undefined, {
      year: "numeric",
      month: "short",
      day: "numeric"
    });
  };

  const formatTime = (dateVal: string | number[]) => {
    const date = parseDate(dateVal);
    if (!date) return "-";
    return date.toLocaleTimeString(undefined, {
      hour: "2-digit",
      minute: "2-digit"
    });
  };

  return (
    <div className="min-h-screen flex flex-col bg-white dark:bg-black text-black dark:text-white transition-colors duration-300">
      <DashboardNavbar />

      <div className="max-w-4xl mx-auto w-full px-6 py-10">
        <h1 className="text-3xl font-bold mb-8">Your Orders</h1>

        {loading ? (
          <p className="text-gray-500 dark:text-gray-400">Loading orders...</p>
        ) : orders.length === 0 ? (
          <div className="text-center py-20">
            <p className="text-gray-500 dark:text-gray-400 mb-6">No orders yet.</p>
            <button
              onClick={() => navigate("/products")}
              className="border px-6 py-2
                border-black dark:border-white
                hover:bg-black hover:text-white
                dark:hover:bg-white dark:hover:text-black
                transition-all duration-200"
            >
              Start Shopping
            </button>
          </div>
        ) : (
          <div className="flex flex-col gap-4">
            {orders.map((order) => (
              <div
                key={order.id}
                className="border border-black dark:border-white transition-all duration-200"
              >
                {/* Order Header */}
                <button
                  onClick={() => toggleExpand(order.id)}
                  className="w-full p-5 flex items-center justify-between text-left hover:bg-black/5 dark:hover:bg-white/5 transition-colors duration-200"
                >
                  <div className="flex items-center gap-6">
                    <div>
                      <p className="text-xs text-gray-500 dark:text-gray-400 uppercase tracking-wide mb-1">
                        Order #{order.id}
                      </p>
                      <p className="font-semibold text-lg">
                        ${Number(order.totalAmount).toFixed(2)}
                      </p>
                    </div>
                    <div>
                      <p className="text-xs text-gray-500 dark:text-gray-400 uppercase tracking-wide mb-1">
                        Status
                      </p>
                      <span
                        className={`text-sm font-medium px-2 py-0.5 border ${
                          order.status === "PLACED" || order.status === "CREATED"
                            ? "border-indigo-400 text-indigo-400"
                            : order.status === "DELIVERED"
                            ? "border-green-500 text-green-500"
                            : "border-gray-400 text-gray-400"
                        }`}
                      >
                        {order.status}
                      </span>
                    </div>
                    <div>
                      <p className="text-xs text-gray-500 dark:text-gray-400 uppercase tracking-wide mb-1">
                        Ordered On
                      </p>
                      <p className="text-sm">{formatDate(order.orderDate)}</p>
                    </div>
                    <div>
                      <p className="text-xs text-gray-500 dark:text-gray-400 uppercase tracking-wide mb-1">
                        Time
                      </p>
                      <p className="text-sm">{formatTime(order.orderDate)}</p>
                    </div>
                  </div>
                  <span className="text-lg select-none">
                    {expanded === order.id ? "−" : "+"}
                  </span>
                </button>

                {/* Order Items */}
                {expanded === order.id && (
                  <div className="border-t border-black dark:border-white px-5 pb-5 pt-4 flex flex-col gap-3">
                    {(order.items ?? []).map((item, index) => (
                      <div
                        key={index}
                        className="flex items-center justify-between text-sm"
                      >
                        <p className="font-medium">{item.productName}</p>
                        <p className="text-gray-500 dark:text-gray-400">
                          {item.quantity} &times; ${Number(item.price).toFixed(2)}
                        </p>
                        <p className="font-semibold">
                          ${(item.quantity * Number(item.price)).toFixed(2)}
                        </p>
                      </div>
                    ))}
                    <div className="border-t border-black/20 dark:border-white/20 pt-3 flex justify-end">
                      <p className="font-bold">
                        Total: ${Number(order.totalAmount).toFixed(2)}
                      </p>
                    </div>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
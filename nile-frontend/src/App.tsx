import { Routes, Route, Navigate } from "react-router-dom";
import Landing from "./pages/Landing";
import Register from "./pages/Register";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import Products from "./pages/Products";
import Cart from "./pages/Cart";
import Orders from "./pages/Orders";
import AdminDashboard from "./pages/AdminDashboard";

function Protected({ children }: any) {
  const token = localStorage.getItem("token");
  return token ? children : <Navigate to="/login" />;
}

function AdminProtected({ children }: any) {
  const token = localStorage.getItem("token");
  const user = JSON.parse(localStorage.getItem("user") || "null");
  if (!token) return <Navigate to="/login" />;
  if (user?.role !== "ROLE_ADMIN") return <Navigate to="/dashboard" />;
  return children;
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Landing />} />
      <Route path="/register" element={<Register />} />
      <Route path="/login" element={<Login />} />

      <Route path="/dashboard" element={<Protected><Dashboard /></Protected>} />
      <Route path="/products"  element={<Protected><Products /></Protected>} />
      <Route path="/cart"      element={<Protected><Cart /></Protected>} />
      <Route path="/orders"    element={<Protected><Orders /></Protected>} />

      <Route path="/admin" element={<AdminProtected><AdminDashboard /></AdminProtected>} />
    </Routes>
  );
}
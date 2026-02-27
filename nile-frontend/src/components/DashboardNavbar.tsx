import { Link, useNavigate } from "react-router-dom";
import { useContext } from "react";
import { AuthContext } from "../context/AuthContext";

const DashboardNavbar = () => {
  const navigate = useNavigate();
  const { user, logout } = useContext(AuthContext);

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <nav className="w-full bg-gray-900 text-white shadow-md">
      <div className="max-w-7xl mx-auto px-6 py-4 flex justify-between items-center">
        
        {/* Left Section */}
        <Link
          to="/dashboard"
          className="text-xl font-semibold tracking-wide hover:text-indigo-400 transition"
        >
          Nile
        </Link>

        {/* Right Section */}
        <div className="flex items-center gap-8">
          <Link
            to="/products"
            className="hover:text-indigo-400 transition"
          >
            Products
          </Link>

          <Link
            to="/orders"
            className="hover:text-indigo-400 transition"
          >
            Orders
          </Link>

          <Link
            to="/cart"
            className="hover:text-indigo-400 transition"
          >
            Cart
          </Link>

          {user && (
            <span className="text-sm text-gray-300">
              Hi, {user.name}
            </span>
          )}

          <button
            onClick={handleLogout}
            className="bg-red-500 hover:bg-red-600 px-4 py-2 rounded-lg text-sm font-medium transition"
          >
            Logout
          </button>
        </div>
      </div>
    </nav>
  );
};

export default DashboardNavbar;
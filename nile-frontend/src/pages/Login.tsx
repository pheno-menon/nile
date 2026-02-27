import { useState, useContext } from "react";
import { useNavigate } from "react-router-dom";
import API from "../api/axios";
import { AuthContext } from "../context/AuthContext";
import { useTheme } from "../context/ThemeContext";


export default function Login() {
  const nav = useNavigate();
  const { login } = useContext(AuthContext);
  const { darkMode, toggleDarkMode } = useTheme();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const handle = async () => {
    try {
      const res = await API.post("/api/auth/login", { email, password });
      login(res.data.token, res.data.user);
      localStorage.setItem("user", JSON.stringify(res.data.user));
      nav("/dashboard");
    } catch {
      alert("Login failed");
    }
  };

  return (
    <div className="relative min-h-screen bg-white dark:bg-black text-black dark:text-white flex items-center justify-center transition-colors duration-300 ease-in-out">
      <button
        onClick={toggleDarkMode}
        className="absolute top-4 right-4
                  px-4 py-2
                  border border-black dark:border-white
                  bg-white dark:bg-black
                  text-black dark:text-white
                  hover:shadow-lg hover:bg-black hover:text-white
                  dark:hover:shadow-lg dark:hover:bg-white dark:hover:text-black
                  transition-all duration-200"
      >
        {darkMode ? "Light Mode" : "Dark Mode"}
      </button>
      <div className="w-full max-w-md space-y-4">
        <h1 className="text-3xl font-bold text-center">Login</h1>

        <input
          placeholder="Email"
          className="w-full border px-3 py-2"
          onChange={(e) => setEmail(e.target.value)}
        />

        <input
          placeholder="Password"
          type="password"
          className="w-full border px-3 py-2"
          onChange={(e) => setPassword(e.target.value)}
        />

        <button
          onClick={handle}
          className="w-full border px-3 py-2
          hover:bg-black hover:text-white
          dark:bg-black dark:border-white dark:text-white
          dark:hover:bg-white dark:hover:text-black
          transition-all duration-200"
        >
          Login
        </button>
        <button
          onClick={() => nav("/")}
          className="w-full border px-3 py-2
          hover:bg-black hover:text-white
          dark:bg-black dark:border-white dark:text-white
          dark:hover:bg-white dark:hover:text-black
          transition-all duration-200"
        >
          Home
        </button>
      </div>
    </div>
  );
}
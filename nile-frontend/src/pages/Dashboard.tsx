import { useContext } from "react";
import { AuthContext } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";
import { useTheme } from "../context/ThemeContext";


export default function Dashboard() {
  const { darkMode, toggleDarkMode } = useTheme();
  const nav = useNavigate();
  const { logout } = useContext(AuthContext);
  const user = JSON.parse(localStorage.getItem("user")!);

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
      <div className="text-center">
        <h1 className="text-4xl font-bold mb-6">Welcome, {user.name}</h1>
        <h3 className="text-xl">You're logged in!</h3><br></br>

        <button
          onClick={() => nav("/")}
          className="border px-4 py-2
          hover:bg-black hover:text-white
          dark:bg-black dark:border-white dark:text-white
          dark:hover:bg-white dark:hover:text-black
          transition-all duration-200"
        >
          Home
        </button>

        <button
          onClick={() => {
            logout();
            nav("/login");
          }}
          className="border px-4 py-2 ml-4
          hover:bg-black hover:text-white
          dark:bg-black dark:border-white dark:text-white
          dark:hover:bg-white dark:hover:text-black
          transition-all duration-200"
        >
          Logout
        </button>
      </div>
    </div>
  );
}
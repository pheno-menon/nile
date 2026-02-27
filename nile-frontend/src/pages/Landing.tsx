import { useNavigate } from "react-router-dom";
import { useTheme } from "../context/ThemeContext";


export default function Landing() {
  const nav = useNavigate();
  const { darkMode, toggleDarkMode } = useTheme();

  return (
    <div className="relative min-h-screen flex items-center justify-center bg-white dark:bg-black text-black dark:text-white transition-colors duration-300 ease-in-out">
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
        <h1 className="text-8xl font-extrabold">nile</h1>

        <div className="mt-10 flex gap-6 justify-center">
          <button
            onClick={() => nav("/register")}
            className="px-10 py-3 border border-black dark:border-white
            bg-white dark:bg-black
            text-black dark:text-white
            hover:bg-black dark:hover:bg-white 
            hover:text-white dark:hover:text-black 
            hover:shadow-lg transition-all duration-200"
          >
            Register
          </button>

          <button
            onClick={() => nav("/login")}
            className="px-10 py-3 border border-black dark:border-white
            bg-white dark:bg-black
            text-black dark:text-white
            hover:bg-black dark:hover:bg-white 
            hover:text-white dark:hover:text-black 
            hover:shadow-lg transition-all duration-200"
          >
            Login
          </button>
        </div>
      </div>
    </div>
  );
}
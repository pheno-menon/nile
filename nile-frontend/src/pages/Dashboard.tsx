import { useState, useEffect } from "react";
import { useTheme } from "../context/ThemeContext";
import DashboardNavbar from "../components/DashboardNavbar";


export default function Dashboard() {
  const { darkMode, toggleDarkMode } = useTheme();
  const user = JSON.parse(localStorage.getItem("user")!);
  const [isModalOpen, setIsModalOpen] = useState(false);

  return (
    <div className="min-h-screen flex flex-col bg-white dark:bg-black text-black dark:text-white transition-colors duration-300 ease-in-out">
      <DashboardNavbar />
      <div className="flex flex-1 flex-col justify-center items-center text-center px-4">
        <h1 className="text-4xl font-bold mb-6">Welcome, {user.name}</h1>
        <h3 className="text-xl mb-6">You're logged in!</h3><br></br>
        <div className="flex">
          <button
          onClick={() => setIsModalOpen(true)}
          className="border px-6 py-2 border-black
            hover:bg-black hover:text-white
            dark:border-white
            dark:hover:bg-white dark:hover:text-black
            transition-all duration-200"
        >
          About Project
        </button>
        <button
          onClick={toggleDarkMode}
          className="border px-4 py-2 ml-4
                    border border-black dark:border-white
                    bg-white dark:bg-black
                    text-black dark:text-white
                    hover:shadow-lg hover:bg-black hover:text-white
                    dark:hover:shadow-lg dark:hover:bg-white dark:hover:text-black
                    transition-all duration-200"
        >
          {darkMode ? "Light Mode" : "Dark Mode"}
      </button>
        </div>
      </div>
      {isModalOpen && (
        <FeaturesModal onClose={() => setIsModalOpen(false)} />
      )}
    </div>
  );
}

function FeaturesModal({ onClose }: { onClose: () => void }) {
  useEffect(() => {
    const handleEsc = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose();
    };
    window.addEventListener("keydown", handleEsc);
    return () => window.removeEventListener("keydown", handleEsc);
  }, [onClose]);

  return (
    <div
      className="
        fixed inset-0
        flex items-center justify-center
        bg-black/30 backdrop-blur-sm
        z-50
        transition-opacity duration-300
      "
      onClick={onClose}
    >
      <div
        onClick={(e) => e.stopPropagation()}
        className="
          relative
          bg-white/80 dark:bg-black/80
          backdrop-blur-md
          border border-black/20 dark:border-white/20
          p-10
          max-w-xl w-full
          shadow-2xl
          transform transition-all duration-300
          scale-100
        "
      >
        <button
          onClick={onClose}
          className="
            absolute top-4 right-4
            text-xl font-bold
            text-black dark:text-white
            transition-all duration-200
            hover:text-red-500 hover:scale-110
          "
        >
          ×
        </button>

        <h2 className="text-2xl font-bold mb-6">
          About Project
        </h2>
        <p className="text-sm text-gray-800 dark:text-gray-200">Nile is a Spring Boot based e-commerce backend application that provides 
          authentication, user management, product management, cart, and order APIs. 
          This project demonstrates real-world backend architecture</p><br></br>

        <div className="space-y-6 text-sm text-gray-800 dark:text-gray-200 text-left">

          <div>
            <h3 className="font-semibold mb-2">Tech Stack</h3>
            <ul className="list-disc list-inside space-y-1">
              <li>Java 17</li>
              <li>Spring Boot</li>
              <li>Spring Security (JWT Authentication)</li>
              <li>MySQL</li>
              <li>React + TypeScript</li>
              <li>Tailwind CSS</li>
              <li>Docker & Docker Compose</li>
            </ul>
          </div>

          <div>
            <h3 className="font-semibold mb-2">Features</h3>
            <ul className="list-disc list-inside space-y-1">
              <li>JWT-based secure login & registration</li>
              <li>Role-based authorization</li>
              <li>Product catalog management</li>
              <li>Shopping cart & order handling</li>
              <li>Dark / Light theme toggle</li>
              <li>Clean layered backend architecture</li>
              <li>Dockerized backend deployment</li>
            </ul>
          </div>

        </div>
      </div>
    </div>
  );
}
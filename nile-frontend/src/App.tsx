import { useState, useEffect } from "react";

export default function App() {
  const [isOpen, setIsOpen] = useState(false);
  const [dark, setDark] = useState(false);

  useEffect(() => {
    const stored = localStorage.getItem("theme");
    if (stored === "dark") {
      setDark(true);
      document.documentElement.classList.add("dark");
    }
  }, []);

  const toggleDark = () => {
    const newMode = !dark;
    setDark(newMode);
    if (newMode) {
      document.documentElement.classList.add("dark");
      localStorage.setItem("theme", "dark");
    } else {
      document.documentElement.classList.remove("dark");
      localStorage.setItem("theme", "light");
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-white dark:bg-black text-gray-900 dark:text-white transition-colors duration-300 relative">
      
      {/* Dark Mode Toggle */}
      <button
        onClick={toggleDark}
        className="absolute top-6 right-6 text-sm border border-current px-4 py-2 transition-all hover:-translate-y-1 hover:shadow-md"
      >
        {dark ? "Light Mode" : "Dark Mode"}
      </button>

      <div className="text-center px-6">
        <h1 className="text-7xl md:text-9xl font-extrabold tracking-tight">
          nile
        </h1>

        <div className="mt-14 flex flex-col sm:flex-row gap-6 justify-center">
          <ActionButton label="Register" />
          <ActionButton label="Login" />
          <ActionButton label="View Features" onClick={() => setIsOpen(true)} />
        </div>
      </div>

      {isOpen && <FeaturesModal onClose={() => setIsOpen(false)} />}
    </div>
  );
}

function ActionButton({
  label,
  onClick,
}: {
  label: string;
  onClick?: () => void;
}) {
  return (
    <button
      onClick={onClick}
      className="
        px-10 py-3
        border border-current
        bg-white dark:bg-black
        text-black dark:text-white
        text-sm font-medium
        transition-all duration-200 ease-out
        hover:bg-black hover:text-white
        dark:hover:bg-white dark:hover:text-black
        hover:-translate-y-1
        hover:shadow-lg
      "
    >
      {label}
    </button>
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
        bg-black/30 dark:bg-white/10
        backdrop-blur-sm
      "
      onClick={onClose}
    >
      <div
        onClick={(e) => e.stopPropagation()}
        className="
          bg-white/80 dark:bg-black/70
          backdrop-blur-md
          border border-current
          p-10
          max-w-lg w-full
          shadow-2xl
          transition-all duration-200
        "
      >
        <h2 className="text-2xl font-bold mb-6">Features & Tech Stack</h2>

        <ul className="space-y-4 text-sm">
          <li><strong>Authentication:</strong> JWT-based secure login</li>
          <li><strong>Backend:</strong> Spring Boot, JPA, MySQL, Docker</li>
          <li><strong>Frontend:</strong> React + Vite + Tailwind</li>
          <li><strong>Architecture:</strong> Clean layered REST design</li>
          <li><strong>Deployment:</strong> Docker Compose ready</li>
        </ul>

        <div className="mt-8 text-right">
          <button
            onClick={onClose}
            className="
              px-6 py-2
              border border-current
              bg-white dark:bg-black
              text-black dark:text-white
              text-sm font-medium
              transition-all duration-200
              hover:bg-black hover:text-white
              dark:hover:bg-white dark:hover:text-black
              hover:-translate-y-1
              hover:shadow-md
            "
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
}
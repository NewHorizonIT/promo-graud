import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";

export default function CategoriesSection() {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    fetch("http://localhost:8080/api/v1/products/type")
      .then(res => res.json())
      .then(data => { if (data?.result) setCategories(data.result); })
      .catch(err => console.error("Failed to fetch categories:", err))
      .finally(() => setLoading(false));
  }, []);

  const icons = ["👕","🧥","👖","👗","👟","👜","🕶️","🧢","👔","🩴"];

  return (
    <section className="w-full">
      <h2 className="text-2xl font-bold text-text-primary mb-6">Danh mục sản phẩm</h2>
      <div className="flex flex-wrap gap-3">
        {loading
          ? [1,2,3,4,5,6].map(i => (
              <div key={i} className="h-10 w-28 bg-surface-muted animate-pulse rounded-full" />
            ))
          : categories.map((cat, idx) => (
              <button
                key={cat.id}
                onClick={() => navigate(`/products?typeId=${cat.id}`)}
                className="flex items-center gap-2 px-5 py-2.5 rounded-full bg-surface border border-border text-sm font-medium text-text-secondary hover:bg-primary hover:text-white hover:border-primary transition-all duration-200 shadow-sm hover:shadow-level-1"
              >
                <span>{icons[idx % icons.length]}</span>
                {cat.type}
              </button>
            ))
        }
      </div>
    </section>
  );
}

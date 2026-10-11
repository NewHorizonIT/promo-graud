import { useState, useEffect } from "react";
import { Search, X } from "lucide-react";

export default function ProductSearchBar({ value = "", onSearch, placeholder = "Tìm kiếm theo tên sản phẩm..." }) {
  const [searchTerm, setSearchTerm] = useState(value);

  useEffect(() => {
    setSearchTerm(value);
  }, [value]);

  const handleSubmit = (e) => {
    e.preventDefault();
    onSearch?.(searchTerm.trim());
  };

  const handleClear = () => {
    setSearchTerm("");
    onSearch?.("");
  };

  return (
    <form onSubmit={handleSubmit} className="relative flex items-center w-full max-w-md">
      <input
        type="text"
        value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}
        placeholder={placeholder}
        className="
          w-full h-11 pl-4 pr-20 rounded-xl border border-border bg-surface
          text-sm text-text-primary placeholder:text-text-muted
          focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/20
          transition-all shadow-sm
        "
      />
      {searchTerm && (
        <button
          type="button"
          onClick={handleClear}
          className="absolute right-10 text-text-muted hover:text-text-primary p-1 transition-colors"
          title="Xóa tìm kiếm"
        >
          <X size={16} />
        </button>
      )}
      <button
        type="submit"
        className="
          absolute right-1.5 h-8 px-3 rounded-lg bg-primary hover:bg-primary-hover
          text-white text-xs font-semibold flex items-center gap-1.5
          transition-all
        "
      >
        <Search size={14} />
        <span>Tìm</span>
      </button>
    </form>
  );
}

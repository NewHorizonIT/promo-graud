import { useEffect } from "react";
import { X, ShoppingCart } from "lucide-react";
import { useCart } from "../hooks/useCart";
import { formatCurrency } from "../utils/format";

export default function ProductModal({ product, onClose }) {
  const { addToCart, cart } = useCart();
  const isAlreadyInCart = cart?.some((item) => item.id === product.id);

  useEffect(() => {
    const handler = (e) => {
      if (e.key === "Escape") onClose();
    };
    window.addEventListener("keydown", handler);
    document.body.style.overflow = "hidden";
    return () => {
      window.removeEventListener("keydown", handler);
      document.body.style.overflow = "";
    };
  }, [onClose]);

  const handleAddToCart = () => {
    const res = addToCart(product);
    if (res?.success) {
      onClose();
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-black/50 backdrop-blur-sm" onClick={onClose} />
      <div className="relative z-10 bg-surface rounded-2xl shadow-level-2 w-full max-w-3xl max-h-[90vh] overflow-y-auto flex flex-col md:flex-row">
        <button
          type="button"
          onClick={onClose}
          className="absolute top-4 right-4 z-20 p-2 rounded-full bg-surface-muted hover:bg-border text-text-secondary hover:text-text-primary transition-all cursor-pointer"
          aria-label="Đóng"
        >
          <X size={20} />
        </button>

        {/* Image */}
        <div className="md:w-1/2 aspect-square bg-surface-muted rounded-t-2xl md:rounded-l-2xl md:rounded-tr-none overflow-hidden flex-shrink-0">
          {product.images ? (
            <img src={product.images} alt={product.name} className="w-full h-full object-cover" />
          ) : (
            <div className="w-full h-full flex items-center justify-center text-8xl">👕</div>
          )}
        </div>

        {/* Details */}
        <div className="md:w-1/2 p-6 flex flex-col gap-4">
          <span className="inline-block w-fit px-3 py-1 bg-primary-light text-primary text-xs font-bold rounded-full">
            {product.typeName}
          </span>
          <h2 className="text-2xl font-bold text-text-primary leading-snug">{product.name}</h2>

          {product.description && (
            <p className="text-text-secondary text-sm leading-relaxed">{product.description}</p>
          )}

          <div className="flex items-baseline gap-3 mt-auto pt-4 border-t border-border">
            <span className="text-3xl font-bold text-primary">{formatCurrency(product.price)}</span>
            <span className="text-text-muted line-through text-sm">{formatCurrency(product.price * 1.25)}</span>
          </div>

          <button
            type="button"
            onClick={handleAddToCart}
            className={`w-full py-3 rounded-xl font-semibold flex items-center justify-center gap-2 transition-all cursor-pointer ${
              isAlreadyInCart
                ? "bg-emerald-500 hover:bg-emerald-600 text-white"
                : "bg-primary hover:bg-primary-hover text-white hover:shadow-glow"
            }`}
          >
            <ShoppingCart size={18} />
            {isAlreadyInCart ? "Đã có trong giỏ hàng" : "Thêm vào giỏ hàng"}
          </button>
        </div>
      </div>
    </div>
  );
}

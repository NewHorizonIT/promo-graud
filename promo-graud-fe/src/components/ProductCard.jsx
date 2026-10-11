import { ShoppingCart, Heart, Eye } from "lucide-react";
import { useCart } from "../hooks/useCart";
import { formatCurrency } from "../utils/format";

export default function ProductCard({ product, onDetail }) {
  const { addToCart, cart } = useCart();

  const isAlreadyInCart = cart?.some((item) => item.id === product.id);

  const handleAddToCart = (e) => {
    e.stopPropagation();
    addToCart(product);
  };

  return (
    <div className="bg-surface border border-border rounded-xl overflow-hidden hover:shadow-level-2 transition-all group relative flex flex-col">
      {/* Sale Tag */}
      <div className="absolute top-3 left-3 bg-primary text-white text-xs font-bold px-2 py-1 rounded-full z-10">
        -20%
      </div>

      {/* Wishlist */}
      <button
        type="button"
        className="absolute top-3 right-3 p-1.5 bg-white/80 backdrop-blur-sm rounded-full text-text-secondary hover:text-primary z-10 opacity-0 group-hover:opacity-100 transition-opacity cursor-pointer"
        aria-label="Thêm vào yêu thích"
      >
        <Heart size={16} />
      </button>

      {/* Image */}
      <div className="w-full aspect-[4/5] bg-surface-muted relative overflow-hidden">
        {product.images ? (
          <img
            src={product.images}
            alt={product.name}
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center text-text-muted text-5xl">
            👕
          </div>
        )}
        {/* Overlay "Xem chi tiết" */}
        {onDetail && (
          <div className="absolute inset-0 bg-black/30 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
            <button
              type="button"
              onClick={() => onDetail(product)}
              className="flex items-center gap-2 px-4 py-2 bg-white text-text-primary rounded-xl font-semibold text-sm shadow-level-2 hover:bg-primary hover:text-white transition-all cursor-pointer"
            >
              <Eye size={16} /> Xem chi tiết
            </button>
          </div>
        )}
      </div>

      {/* Content */}
      <div className="p-4 flex flex-col gap-2 flex-1">
        <div className="text-xs text-text-muted">{product.typeName}</div>
        <h3 className="text-sm font-semibold text-text-primary line-clamp-2 leading-tight flex-1">
          {product.name}
        </h3>
        <div className="flex items-center gap-2 mt-1">
          <span className="text-primary font-bold text-base">{formatCurrency(product.price)}</span>
          <span className="text-text-muted line-through text-xs">{formatCurrency(product.price * 1.25)}</span>
        </div>
        <button
          type="button"
          onClick={handleAddToCart}
          className={`w-full mt-1 py-2 border rounded-lg font-semibold text-sm transition-all flex items-center justify-center gap-2 cursor-pointer ${
            isAlreadyInCart
              ? "border-emerald-500 bg-emerald-50 text-emerald-600 hover:bg-emerald-100"
              : "border-primary text-primary hover:bg-primary hover:text-white"
          }`}
        >
          <ShoppingCart size={15} />
          {isAlreadyInCart ? "Đã trong giỏ" : "Thêm vào giỏ"}
        </button>
      </div>
    </div>
  );
}

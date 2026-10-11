import { ShoppingBag, ArrowRight } from "lucide-react";
import { Link } from "react-router-dom";

export default function EmptyCart() {
  return (
    <div className="flex flex-col items-center justify-center py-20 px-4 bg-surface rounded-3xl border border-border text-center shadow-level-1 max-w-2xl mx-auto my-8">
      <div className="w-24 h-24 rounded-full bg-primary-light flex items-center justify-center mb-6 text-primary">
        <ShoppingBag size={44} />
      </div>

      <h2 className="text-2xl font-bold text-text-primary mb-2">
        Giỏ hàng của bạn đang trống
      </h2>

      <p className="text-sm text-text-muted max-w-md mb-8 leading-relaxed">
        Bạn chưa thêm sản phẩm nào vào giỏ hàng. Hãy khám phá ngay các bộ sưu tập thời trang hot nhất hôm nay!
      </p>

      <Link
        to="/products"
        className="
          inline-flex items-center gap-2 px-8 py-3.5
          bg-primary hover:bg-primary-hover text-white
          rounded-xl font-semibold text-sm transition-all
          hover:shadow-glow active:scale-[0.98]
        "
      >
        <span>Khám phá sản phẩm ngay</span>
        <ArrowRight size={16} />
      </Link>
    </div>
  );
}

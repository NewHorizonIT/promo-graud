import { useState, useEffect } from 'react';
import { ChevronLeft, ChevronRight, ArrowRight } from 'lucide-react';
import { Link } from 'react-router-dom';
import ProductCard from '../../../components/ProductCard';
import { fetchProducts } from '../../products/services/productService';

export default function TrendingProducts({ onDetail }) {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const ITEMS_PER_PAGE = 4;

  useEffect(() => {
    fetchProducts({ limit: 12, offset: 0, name: '' })
      .then((data) => {
        if (data?.result?.data) {
          setProducts(data.result.data);
        }
      })
      .catch((err) => console.error('Failed to fetch products:', err))
      .finally(() => setLoading(false));
  }, []);

  const totalPages = Math.ceil(products.length / ITEMS_PER_PAGE);
  const visible = products.slice(page * ITEMS_PER_PAGE, page * ITEMS_PER_PAGE + ITEMS_PER_PAGE);

  return (
    <section className="w-full">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-2xl font-bold text-text-primary">Sản phẩm nổi bật</h2>
          <p className="text-sm text-text-muted mt-0.5">Các mặt hàng được săn đón nhiều nhất</p>
        </div>
        <Link
          to="/products"
          className="flex items-center gap-1 text-sm font-semibold text-primary hover:underline"
        >
          <span>Xem tất cả</span> <ArrowRight size={16} />
        </Link>
      </div>

      <div className="relative">
        {loading ? (
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {[1, 2, 3, 4].map((i) => (
              <div
                key={i}
                className="aspect-[4/5] bg-surface-muted animate-pulse rounded-xl border border-border"
              />
            ))}
          </div>
        ) : products.length > 0 ? (
          <>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              {visible.map((product) => (
                <ProductCard key={product.id} product={product} onDetail={onDetail} />
              ))}
            </div>

            {totalPages > 1 && (
              <div className="flex items-center justify-center gap-4 mt-6">
                <button
                  type="button"
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
                  disabled={page === 0}
                  className="p-2 rounded-full border border-border bg-surface hover:bg-surface-muted disabled:opacity-30 disabled:pointer-events-none transition-all cursor-pointer"
                  aria-label="Trang trước"
                >
                  <ChevronLeft size={18} />
                </button>

                <div className="flex items-center gap-2">
                  {Array.from({ length: totalPages }).map((_, i) => (
                    <button
                      key={i}
                      type="button"
                      onClick={() => setPage(i)}
                      className={`h-2.5 rounded-full transition-all cursor-pointer ${
                        i === page ? 'bg-primary w-6' : 'bg-border w-2.5 hover:bg-text-muted'
                      }`}
                      aria-label={`Trang ${i + 1}`}
                    />
                  ))}
                </div>

                <button
                  type="button"
                  onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                  disabled={page === totalPages - 1}
                  className="p-2 rounded-full border border-border bg-surface hover:bg-surface-muted disabled:opacity-30 disabled:pointer-events-none transition-all cursor-pointer"
                  aria-label="Trang sau"
                >
                  <ChevronRight size={18} />
                </button>
              </div>
            )}
          </>
        ) : (
          <div className="text-center py-10 text-text-muted bg-surface rounded-2xl border border-border">
            Không có sản phẩm nào.
          </div>
        )}
      </div>
    </section>
  );
}

import { useState, useEffect } from 'react';
import { ChevronLeft, ChevronRight, ArrowRight } from 'lucide-react';
import { Link } from 'react-router-dom';
import VoucherCard from '../../../components/VoucherCard';

export default function VoucherZone() {
  const [vouchers, setVouchers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const ITEMS_PER_PAGE = 4;

  useEffect(() => {
    fetch('http://localhost:8080/api/v1/vouchers?type=GENERIC&status=ACTIVE&pageSize=8')
      .then((res) => res.json())
      .then((data) => {
        if (data?.data) setVouchers(data.data);
      })
      .catch((err) => console.error('Failed to fetch vouchers:', err))
      .finally(() => setLoading(false));
  }, []);

  const totalPages = Math.ceil(vouchers.length / ITEMS_PER_PAGE);
  const visible = vouchers.slice(page * ITEMS_PER_PAGE, page * ITEMS_PER_PAGE + ITEMS_PER_PAGE);

  return (
    <section className="w-full bg-surface-muted/50 p-6 rounded-2xl border border-border">
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-2xl font-bold text-text-primary">Mã giảm giá dành cho bạn</h2>
        <Link
          to="/vouchers"
          className="flex items-center gap-1 text-sm font-semibold text-primary hover:underline"
        >
          Xem tất cả <ArrowRight size={16} />
        </Link>
      </div>

      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="h-28 bg-surface-muted animate-pulse rounded-xl" />
          ))}
        </div>
      ) : vouchers.length > 0 ? (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            {visible.map((voucher) => (
              <VoucherCard key={voucher.id} voucher={voucher} />
            ))}
          </div>
          {totalPages > 1 && (
            <div className="flex items-center justify-center gap-4 mt-6">
              <button
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
                className="p-2 rounded-full border border-border bg-surface hover:bg-surface-muted disabled:opacity-30 transition-all"
              >
                <ChevronLeft size={18} />
              </button>
              <div className="flex gap-2">
                {Array.from({ length: totalPages }).map((_, i) => (
                  <button
                    key={i}
                    onClick={() => setPage(i)}
                    className={`w-2.5 h-2.5 rounded-full transition-all ${i === page ? 'bg-primary w-6' : 'bg-border'}`}
                  />
                ))}
              </div>
              <button
                onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                disabled={page === totalPages - 1}
                className="p-2 rounded-full border border-border bg-surface hover:bg-surface-muted disabled:opacity-30 transition-all"
              >
                <ChevronRight size={18} />
              </button>
            </div>
          )}
        </>
      ) : (
        <div className="text-center py-8 text-text-muted">Hiện tại chưa có mã giảm giá nào.</div>
      )}
    </section>
  );
}

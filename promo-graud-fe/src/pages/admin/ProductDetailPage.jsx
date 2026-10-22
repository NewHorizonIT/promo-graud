import { useParams, Link } from 'react-router-dom';
import { ChevronLeft, Pencil } from 'lucide-react';
import ProductTypeBadge from '../../features/admin/products/components/ProductTypeBadge';
import { useProduct } from '../../features/admin/products/hooks/useProducts';
import { formatCurrency } from '../../utils/format';

function formatDate(iso) {
  return iso ? new Date(iso).toLocaleDateString('vi-VN') : '—';
}

export default function ProductDetailPage() {
  const { id } = useParams();
  const { data: product, isLoading } = useProduct(id);

  if (isLoading) {
    return (
      <div className="space-y-4">
        {[1, 2, 3].map((i) => (
          <div key={i} className="h-20 bg-surface-muted rounded-xl animate-pulse" />
        ))}
      </div>
    );
  }

  if (!product) {
    return <p className="text-sm text-text-muted">Không tìm thấy sản phẩm.</p>;
  }

  const images =
    product.images
      ?.split(',')
      .map((s) => s.trim())
      .filter(Boolean) ?? [];
  const typeName = product.typeOfProduct?.type ?? product.typeName;

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <Link
          to="/admin/products"
          className="inline-flex items-center gap-1.5 text-sm text-text-secondary hover:text-text-primary transition-colors"
        >
          <ChevronLeft size={16} /> Danh sách Sản phẩm
        </Link>
        <Link
          to={`/admin/products/${id}/edit`}
          className="flex items-center gap-1.5 px-3 py-1.5 border border-border bg-surface hover:bg-surface-muted text-sm font-semibold text-text-primary rounded-xl transition-colors"
        >
          <Pencil size={14} /> Chỉnh sửa
        </Link>
      </div>

      <div className="bg-surface border border-border rounded-2xl p-6 shadow-level-1">
        <div className="flex items-start justify-between gap-4">
          <div>
            <h2 className="text-2xl font-bold text-text-primary">{product.name}</h2>
            <p className="text-sm text-text-muted mt-1">ID: #{product.id}</p>
          </div>
          <ProductTypeBadge name={typeName} />
        </div>

        <div className="grid grid-cols-3 gap-4 mt-5">
          <div>
            <p className="text-xs text-text-muted mb-0.5">Giá</p>
            <p className="text-sm font-medium text-text-primary">{formatCurrency(product.price)}</p>
          </div>
          <div>
            <p className="text-xs text-text-muted mb-0.5">Ngày tạo</p>
            <p className="text-sm font-medium text-text-primary">{formatDate(product.createdAt)}</p>
          </div>
          <div>
            <p className="text-xs text-text-muted mb-0.5">Cập nhật</p>
            <p className="text-sm font-medium text-text-primary">{formatDate(product.updatedAt)}</p>
          </div>
        </div>
      </div>

      <div className="bg-surface border border-border rounded-xl p-5 shadow-level-1">
        <p className="text-xs font-semibold text-text-muted uppercase tracking-wide mb-2">Mô tả</p>
        <p className="text-sm text-text-secondary whitespace-pre-line">
          {product.description || 'Chưa có mô tả.'}
        </p>
      </div>

      <div className="bg-surface border border-border rounded-xl p-5 shadow-level-1">
        <p className="text-xs font-semibold text-text-muted uppercase tracking-wide mb-3">
          Hình ảnh
        </p>
        {images.length ? (
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
            {images.map((src) => (
              <img
                key={src}
                src={src}
                alt={product.name}
                className="w-full aspect-square object-cover rounded-xl border border-border bg-surface-muted"
              />
            ))}
          </div>
        ) : (
          <p className="text-sm text-text-muted">Chưa có hình ảnh.</p>
        )}
      </div>
    </div>
  );
}

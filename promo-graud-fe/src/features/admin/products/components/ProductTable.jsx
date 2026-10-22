import { Link } from 'react-router-dom';
import { Pencil, Trash2, Eye, Image as ImageIcon } from 'lucide-react';
import { formatCurrency } from '../../../../utils/format';
import ProductTypeBadge from './ProductTypeBadge';

function formatDate(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleDateString('vi-VN');
}

// Cột `images` (TEXT) lưu các URL ngăn cách bằng dấu phẩy -> lấy ảnh đầu làm thumbnail
function getThumbnail(images) {
  return images?.split(',')[0]?.trim() || null;
}

export default function ProductTable({ products = [], onDelete }) {
  if (!products.length) {
    return (
      <div className="flex flex-col items-center justify-center py-16 text-center bg-surface border border-border rounded-xl">
        <div className="w-14 h-14 rounded-full bg-surface-muted flex items-center justify-center mb-3 text-2xl">
          📦
        </div>
        <p className="text-sm font-semibold text-text-primary">Chưa có sản phẩm nào</p>
        <p className="text-xs text-text-muted mt-1">Tạo sản phẩm đầu tiên để bắt đầu</p>
      </div>
    );
  }

  return (
    <div className="bg-surface border border-border rounded-xl overflow-hidden shadow-level-1">
      <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-surface-muted border-b border-border">
            <tr>
              <th className="text-left py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">
                Sản phẩm
              </th>
              <th className="text-left py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">
                Loại
              </th>
              <th className="text-left py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">
                Giá
              </th>
              <th className="text-left py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">
                Ngày tạo
              </th>
              <th className="text-right py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">
                Thao tác
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {products.map((p) => {
              const thumb = getThumbnail(p.images);
              // Tùy DTO backend: typeOfProduct.type hoặc typeName
              const typeName = p.typeOfProduct?.type ?? p.typeName;

              return (
                <tr key={p.id} className="hover:bg-surface-muted/40 transition-colors">
                  <td className="py-3 px-4">
                    <div className="flex items-center gap-3 min-w-0">
                      <div className="shrink-0 w-10 h-10 rounded-lg bg-primary/10 text-primary flex items-center justify-center overflow-hidden">
                        {thumb ? (
                          <img src={thumb} alt="" className="w-full h-full object-cover" />
                        ) : (
                          <ImageIcon size={16} />
                        )}
                      </div>
                      <div className="min-w-0 max-w-xs">
                        <p className="font-medium text-text-primary truncate">{p.name}</p>
                        {p.description && (
                          <p className="text-xs text-text-muted truncate">{p.description}</p>
                        )}
                      </div>
                    </div>
                  </td>
                  <td className="py-3 px-4">
                    <ProductTypeBadge name={typeName} />
                  </td>
                  <td className="py-3 px-4 font-medium text-text-primary">
                    {formatCurrency(p.price)}
                  </td>
                  <td className="py-3 px-4 text-text-secondary text-xs">
                    {formatDate(p.createdAt)}
                  </td>
                  <td className="py-3 px-4">
                    <div className="flex items-center justify-end gap-1">
                      <Link
                        to={`/admin/products/${p.id}`}
                        className="p-1.5 rounded-lg text-text-muted hover:text-text-primary hover:bg-surface-muted transition-colors"
                        title="Xem chi tiết"
                        aria-label="Xem chi tiết"
                      >
                        <Eye size={15} />
                      </Link>
                      <Link
                        to={`/admin/products/${p.id}/edit`}
                        className="p-1.5 rounded-lg text-text-muted hover:text-text-primary hover:bg-surface-muted transition-colors"
                        title="Chỉnh sửa"
                        aria-label="Chỉnh sửa"
                      >
                        <Pencil size={15} />
                      </Link>
                      <button
                        type="button"
                        onClick={() => onDelete?.(p)}
                        className="p-1.5 rounded-lg text-text-muted hover:text-error-text hover:bg-error-bg transition-colors cursor-pointer"
                        title="Xóa"
                        aria-label="Xóa"
                      >
                        <Trash2 size={15} />
                      </button>
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}

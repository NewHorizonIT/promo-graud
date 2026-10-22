import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Plus, Tags, Search, Filter, X } from 'lucide-react';
import ProductTable from '../../features/admin/products/components/ProductTable';
import {
  useProducts,
  useDeleteProduct,
  useTypeProduct,
} from '../../features/admin/products/hooks/useProducts';
// QUAN TRỌNG: Hãy sửa lại đường dẫn import Toast cho đúng với cấu trúc thư mục của bạn
import Toast from '../../components/Toast';

const PAGE_SIZE = 10;

export default function ProductPage() {
  // State quản lý Toast custom
  const [toast, setToast] = useState(null);

  // State lưu tạm giá trị đang nhập ở Form
  const [formValues, setFormValues] = useState({
    name: '',
    typeId: '',
    minPrice: '',
    maxPrice: '',
  });

  // State chính thức dùng để gọi API (khi nhấn Lọc mới cập nhật)
  const [queryParams, setQueryParams] = useState({
    page: 1,
    name: '',
    typeId: '',
    minPrice: '',
    maxPrice: '',
  });

  // Gọi API với React Query
  const params = {
    page: queryParams.page,
    size: PAGE_SIZE,
    ...(queryParams.name && { name: queryParams.name }),
    ...(queryParams.typeId && { typeId: queryParams.typeId }),
    ...(queryParams.minPrice && { minPrice: queryParams.minPrice }),
    ...(queryParams.maxPrice && { maxPrice: queryParams.maxPrice }),
  };

  const { data, isLoading } = useProducts(params);
  const { data: types = [] } = useTypeProduct();
  const deleteMutation = useDeleteProduct();

  // Xử lý dữ liệu trả về
  const products = data?.data ?? data?.result?.data ?? [];
  const total = data?.totalItems ?? data?.result?.totalItems ?? 0;
  const totalPages = data?.totalPages ?? data?.result?.totalPages ?? 1;

  const handleDelete = (product) => {
    if (!confirm(`Xóa sản phẩm "${product.name}"?`)) return;
    deleteMutation.mutate(product.id, {
      // Dùng setState của Toast custom thay vì react-hot-toast
      onSuccess: () => setToast({ type: 'success', message: 'Xóa sản phẩm thành công!' }),
      onError: () => setToast({ type: 'error', message: 'Có lỗi xảy ra khi xóa sản phẩm.' }),
    });
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormValues((prev) => ({ ...prev, [name]: value }));
  };

  const handleApplyFilters = (e) => {
    e.preventDefault();
    setQueryParams({ ...formValues, page: 1 });
  };

  const handleClearFilters = () => {
    const emptyFilters = { name: '', typeId: '', minPrice: '', maxPrice: '' };
    setFormValues(emptyFilters);
    setQueryParams({ ...emptyFilters, page: 1 });
  };

  const handlePageChange = (pg) => {
    setQueryParams((prev) => ({ ...prev, page: pg }));
  };

  const inputClass =
    'w-full h-10 px-3 rounded-xl border border-border bg-surface-muted text-sm text-text-primary placeholder:text-text-muted focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/50 transition-all';

  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <p className="text-sm text-text-muted">{total} sản phẩm</p>
        <div className="flex items-center gap-2">
          <Link
            to="/admin/product-types"
            className="flex items-center gap-2 px-4 py-2 bg-surface border border-border hover:bg-surface-muted text-text-secondary rounded-xl text-sm font-semibold transition-all"
          >
            <Tags size={16} /> Loại sản phẩm
          </Link>
          <Link
            to="/admin/products/new"
            className="flex items-center gap-2 px-4 py-2 bg-primary hover:bg-primary-hover text-white rounded-xl text-sm font-semibold transition-all hover:shadow-glow"
          >
            <Plus size={16} /> Tạo Sản phẩm
          </Link>
        </div>
      </div>

      {/* Thanh công cụ Tìm kiếm & Lọc */}
      <form
        onSubmit={handleApplyFilters}
        className="bg-surface p-4 rounded-xl border border-border flex flex-wrap gap-4 items-end shadow-sm"
      >
        {/* Tìm theo tên */}
        <div className="flex-1 min-w-50]">
          <label className="block text-xs font-medium text-text-secondary mb-1.5">
            Tên sản phẩm
          </label>
          <div className="relative">
            <Search
              className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted"
              size={15}
            />
            <input
              type="text"
              name="name"
              value={formValues.name}
              onChange={handleInputChange}
              placeholder="Nhập tên sản phẩm..."
              className={`${inputClass} pl-9`}
            />
          </div>
        </div>

        {/* Lọc theo loại */}
        <div className="w-full sm:w-48">
          <label className="block text-xs font-medium text-text-secondary mb-1.5">
            Loại sản phẩm
          </label>
          <select
            name="typeId"
            value={formValues.typeId}
            onChange={handleInputChange}
            className={inputClass}
          >
            <option value="">Tất cả các loại</option>
            {types.map((t) => (
              <option key={t.id} value={String(t.id)}>
                {t.type}
              </option>
            ))}
          </select>
        </div>

        {/* Khoảng giá */}
        <div className="flex items-center gap-2 w-full sm:w-auto">
          <div className="w-full sm:w-32">
            <label className="block text-xs font-medium text-text-secondary mb-1.5">
              Giá từ (VNĐ)
            </label>
            <input
              type="number"
              name="minPrice"
              value={formValues.minPrice}
              onChange={handleInputChange}
              placeholder="Min"
              min="0"
              className={inputClass}
            />
          </div>
          <span className="text-text-muted self-end pb-2">-</span>
          <div className="w-full sm:w-32">
            <label className="block text-xs font-medium text-text-secondary mb-1.5">
              Đến giá (VNĐ)
            </label>
            <input
              type="number"
              name="maxPrice"
              value={formValues.maxPrice}
              onChange={handleInputChange}
              placeholder="Max"
              min="0"
              className={inputClass}
            />
          </div>
        </div>

        {/* Nút thao tác */}
        <div className="flex items-center gap-2 w-full sm:w-auto mt-2 sm:mt-0">
          <button
            type="submit"
            className="flex items-center gap-2 px-4 h-10 bg-surface-muted border border-border hover:bg-border text-text-primary rounded-xl text-sm font-semibold transition-colors w-full sm:w-auto justify-center cursor-pointer"
          >
            <Filter size={15} /> Lọc
          </button>

          {(queryParams.name ||
            queryParams.typeId ||
            queryParams.minPrice ||
            queryParams.maxPrice) && (
            <button
              type="button"
              onClick={handleClearFilters}
              title="Xóa bộ lọc"
              className="flex items-center justify-center w-10 h-10 text-error-text bg-error-bg hover:bg-error-text hover:text-white rounded-xl transition-colors shrink-0 cursor-pointer"
            >
              <X size={16} />
            </button>
          )}
        </div>
      </form>

      {/* Bảng Dữ liệu */}
      {isLoading ? (
        <div className="space-y-2">
          {[...Array(5)].map((_, i) => (
            <div key={i} className="h-14 bg-surface-muted rounded-xl animate-pulse" />
          ))}
        </div>
      ) : (
        <ProductTable products={products} onDelete={handleDelete} />
      )}

      {/* Phân trang */}
      {totalPages > 1 && (
        <div className="flex justify-center gap-2 pt-2">
          {[...Array(totalPages)].map((_, i) => {
            const pg = i + 1;
            return (
              <button
                key={pg}
                type="button"
                onClick={() => handlePageChange(pg)}
                className={`min-w-9 h-9 px-3 rounded-xl text-sm font-semibold transition-all ${
                  pg === queryParams.page
                    ? 'bg-primary text-white shadow-glow'
                    : 'border border-border bg-surface text-text-secondary hover:bg-surface-muted'
                }`}
              >
                {pg}
              </button>
            );
          })}
        </div>
      )}

      {/* Gọi Component Toast ở đây */}
      <Toast toast={toast} onClose={() => setToast(null)} />
    </div>
  );
}

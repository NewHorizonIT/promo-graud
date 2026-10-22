import { useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { ChevronLeft, Plus } from 'lucide-react';
import {
  useCreateTypeProduct,
  useTypeProducts,
  useUpdateTypeProduct,
} from '../../features/admin/products/hooks/useProducts';
import ProductTypeTable from '../../features/admin/products/components/ProductTypetable';
import ProductTypeModal from '../../features/admin/products/components/ProductTypeModal';

const PAGE_SIZE = 10;

export default function ProductTypesPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const page = Math.max(1, Number(searchParams.get('page') ?? 1));
  const params = { page, pageSize: PAGE_SIZE };

  const { data, isLoading } = useTypeProducts(params);
  const createMutation = useCreateTypeProduct();
  const updateMutation = useUpdateTypeProduct();

  const types = data?.content ?? data?.data ?? (Array.isArray(data) ? data : []);
  const totalItems = data?.totalElements ?? data?.totalItems ?? types.length;
  const totalPages = data?.totalPages ?? 1;
  // item = null -> tạo mới, item = { id, type } -> chỉnh sửa
  const [modal, setModal] = useState({ open: false, item: null });
  const [error, setError] = useState('');

  const saving = createMutation.isPending || updateMutation.isPending;

  const openCreate = () => {
    setError('');
    setModal({ open: true, item: null });
  };

  const openEdit = (item) => {
    setError('');
    setModal({ open: true, item });
  };

  const closeModal = () => setModal({ open: false, item: null });

  const handleSubmit = (body) => {
    setError('');
    const options = {
      onSuccess: closeModal,
      onError: (err) => setError(err?.response?.data?.message ?? 'Có lỗi xảy ra.'),
    };
    if (modal.item) {
      updateMutation.mutate({ id: modal.item.id, body }, options);
    } else {
      createMutation.mutate(body, options);
    }
  };

  return (
    <div className="space-y-5">
      <Link
        to="/admin/products"
        className="inline-flex items-center gap-1.5 text-sm text-text-secondary hover:text-text-primary transition-colors"
      >
        <ChevronLeft size={16} /> Danh sách Sản phẩm
      </Link>

      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <p className="text-sm text-text-muted">{totalItems} loại sản phẩm</p>
        <button
          type="button"
          onClick={openCreate}
          className="flex items-center gap-2 px-4 py-2.5 bg-primary hover:bg-primary-hover text-white rounded-xl text-sm font-semibold transition-all hover:shadow-glow cursor-pointer"
        >
          <Plus size={16} /> Tạo Loại
        </button>
      </div>

      {isLoading ? (
        <div className="space-y-2">
          {[...Array(4)].map((_, i) => (
            <div key={i} className="h-14 bg-surface-muted rounded-xl animate-pulse" />
          ))}
        </div>
      ) : (
        <ProductTypeTable types={types} onEdit={openEdit} />
      )}

      {totalPages > 1 && (
        <div className="flex justify-center gap-2 pt-2">
          {[...Array(totalPages)].map((_, i) => {
            const pg = i + 1;
            return (
              <button
                key={pg}
                type="button"
                // Khi click chuyển trang, set URL params mới
                onClick={() => setSearchParams({ page: String(pg) })}
                className={`min-w-9 h-9 px-3 rounded-xl text-sm font-semibold transition-all ${
                  pg === page
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

      {modal.open && (
        <ProductTypeModal
          key={modal.item?.id ?? 'new'}
          initial={modal.item}
          existingTypes={types}
          onSubmit={handleSubmit}
          onClose={closeModal}
          loading={saving}
          error={error}
        />
      )}
    </div>
  );
}

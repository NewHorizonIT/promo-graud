import { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { ChevronLeft } from 'lucide-react';
import ProductForm from '../../features/admin/products/components/ProductForm';
import {
  useProduct,
  useCreateProduct,
  useUpdateProduct,
} from '../../features/admin/products/hooks/useProducts';

// 1. IMPORT TOAST CUSTOM CỦA BẠN (Sửa đường dẫn lại cho đúng với folder của bạn nhé)
import Toast from '../../components/Toast';

export default function ProductFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = Boolean(id);

  const [initial, setInitial] = useState(null);

  // 2. KHAI BÁO STATE CHO TOAST (Thay vì biến error text bình thường)
  const [toast, setToast] = useState(null);

  const initializedIdRef = useRef(null);

  const { data: product, isLoading: fetchLoading } = useProduct(id);
  const createMutation = useCreateProduct();
  const updateMutation = useUpdateProduct();

  // Reset toast khi id thay đổi
  useEffect(() => {
    setToast(null);
  }, [id]);

  useEffect(() => {
    if (!product || initializedIdRef.current === id) return;
    initializedIdRef.current = id;
    setInitial({
      name: product.name,
      type_id: String(product.typeId ?? product.typeOfProduct?.id ?? ''),
      price: String(Math.round(Number(product.price ?? 0)) || ''),
      images:
        product.images
          ?.split(',')
          .map((s) => s.trim())
          .filter(Boolean)
          .join('\n') ?? '',
      description: product.description ?? '',
    });
  }, [product, id]);

  const loading = createMutation.isPending || updateMutation.isPending;

  const handleSubmit = (body) => {
    setToast(null); // Reset toast khi bắt đầu submit

    if (isEdit) {
      updateMutation.mutate(
        { id, body },
        {
          onSuccess: () => {
            // Hiện Toast màu xanh
            setToast({ type: 'success', message: 'Cập nhật sản phẩm thành công!' });
            // Dừng 1 giây để người dùng kịp nhìn thấy thông báo rồi mới chuyển trang
            setTimeout(() => {
              navigate(`/admin/products/${id}`);
            }, 1000);
          },
          onError: (err) => {
            // Hiện Toast màu đỏ
            setToast({ type: 'error', message: err?.response?.data?.message ?? 'Có lỗi xảy ra.' });
          },
        },
      );
    } else {
      createMutation.mutate(body, {
        onSuccess: () => {
          // Hiện Toast màu xanh
          setToast({ type: 'success', message: 'Tạo sản phẩm mới thành công!' });
          // Dừng 1 giây để người dùng kịp nhìn thấy thông báo rồi mới chuyển trang
          setTimeout(() => {
            navigate('/admin/products');
          }, 1000);
        },
        onError: (err) => {
          // Hiện Toast màu đỏ
          setToast({ type: 'error', message: err?.response?.data?.message ?? 'Có lỗi xảy ra.' });
        },
      });
    }
  };

  return (
    <div className="max-w-2xl mx-auto space-y-5">
      <Link
        to={isEdit ? `/admin/products/${id}` : '/admin/products'}
        className="inline-flex items-center gap-1.5 text-sm text-text-secondary hover:text-text-primary transition-colors"
      >
        <ChevronLeft size={16} />
        {isEdit ? 'Quay lại chi tiết' : 'Quay lại danh sách'}
      </Link>

      <div className="bg-surface border border-border rounded-2xl p-6 shadow-level-1 relative">
        <h2 className="text-xl font-bold text-text-primary mb-6">
          {isEdit ? 'Chỉnh sửa Sản phẩm' : 'Tạo Sản phẩm mới'}
        </h2>

        {isEdit && fetchLoading ? (
          <div className="space-y-4">
            {[1, 2, 3].map((i) => (
              <div key={i} className="h-11 bg-surface-muted rounded-xl animate-pulse" />
            ))}
          </div>
        ) : (
          <ProductForm initial={initial ?? undefined} onSubmit={handleSubmit} loading={loading} />
        )}
      </div>

      {/* 3. NHÚNG TOAST XUỐNG CUỐI CÙNG TRONG GIAO DIỆN */}
      <Toast toast={toast} onClose={() => setToast(null)} />
    </div>
  );
}

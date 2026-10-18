import { useState, useEffect, useCallback, useMemo } from 'react';
import { useSearchParams } from 'react-router-dom';
import { SlidersHorizontal, X, ChevronLeft, ChevronRight } from 'lucide-react';
import ProductCard from '../components/ProductCard';
import ProductModal from '../components/ProductModal';
import ProductFilterSidebar from '../features/products/components/ProductFilterSidebar';
import ProductSearchBar from '../features/products/components/ProductSearchBar';
import ProductSkeleton from '../features/products/components/ProductSkeleton';
import { fetchProducts, fetchProductTypes } from '../features/products/services/productService';
import { DEFAULT_PAGE_LIMIT } from '../config/constants';
import { formatCurrency } from '../utils/format';

export default function ProductsPage() {
  const [searchParams, setSearchParams] = useSearchParams();

  // Read current active params from URL
  const searchNameFromUrl = searchParams.get('name') || searchParams.get('search') || '';
  const typeIdFromUrl = searchParams.get('typeId') || '';
  const minPriceFromUrl = searchParams.get('minPrice') || '';
  const maxPriceFromUrl = searchParams.get('maxPrice') || '';
  const pageFromUrl = Math.max(1, parseInt(searchParams.get('page') || '1', 10));

  // Applied filter state (fetch ONLY happens when these change)
  const [appliedFilters, setAppliedFilters] = useState({
    typeId: typeIdFromUrl,
    minPrice: minPriceFromUrl,
    maxPrice: maxPriceFromUrl,
  });

  const [searchName, setSearchName] = useState(searchNameFromUrl);
  const [page, setPage] = useState(pageFromUrl);

  // Data states
  const [products, setProducts] = useState([]);
  const [types, setTypes] = useState([]);
  const [meta, setMeta] = useState({ totalItems: 0, totalPages: 1 });
  const [loading, setLoading] = useState(true);
  const [selectedProduct, setSelectedProduct] = useState(null);
  const [showMobileFilter, setShowMobileFilter] = useState(false);

  // Fetch product types on mount
  useEffect(() => {
    fetchProductTypes()
      .then((data) => {
        if (data?.result) setTypes(data.result);
      })
      .catch((err) => console.error('Error fetching product types:', err));
  }, []);

  // Sync state if URL query params changed (e.g., navigated from Header search)
  useEffect(() => {
    const urlName = searchParams.get('name') || searchParams.get('search') || '';
    const urlTypeId = searchParams.get('typeId') || '';
    const urlMin = searchParams.get('minPrice') || '';
    const urlMax = searchParams.get('maxPrice') || '';
    const urlPage = Math.max(1, parseInt(searchParams.get('page') || '1', 10));

    setSearchName(urlName);
    setAppliedFilters({
      typeId: urlTypeId,
      minPrice: urlMin,
      maxPrice: urlMax,
    });
    setPage(urlPage);
  }, [searchParams]);

  // Execute fetch products using currently APPLIED filters & search keyword
  const loadProducts = useCallback(() => {
    setLoading(true);
    const offset = (page - 1) * DEFAULT_PAGE_LIMIT;

    fetchProducts({
      limit: DEFAULT_PAGE_LIMIT,
      offset,
      typeId: appliedFilters.typeId,
      minPrice: appliedFilters.minPrice,
      maxPrice: appliedFilters.maxPrice,
      name: searchName,
    })
      .then((data) => {
        if (data?.result) {
          setProducts(data.result.data || []);
          setMeta({
            totalItems: data.result.totalItems || 0,
            totalPages: Math.max(1, data.result.totalPages || 1),
          });
        }
      })
      .catch((err) => {
        console.error('Error loading products:', err);
        setProducts([]);
        setMeta({ totalItems: 0, totalPages: 1 });
      })
      .finally(() => setLoading(false));
  }, [appliedFilters, searchName, page]);

  // Trigger fetch when appliedFilters, searchName, or page updates
  useEffect(() => {
    loadProducts();
  }, [loadProducts]);

  // Helper to sync updated filters and search to URL query params
  const updateUrlParams = (newFilters, newSearch, newPage = 1) => {
    const nextParams = {};
    if (newSearch) nextParams.name = newSearch;
    if (newFilters.typeId) nextParams.typeId = newFilters.typeId;
    if (newFilters.minPrice) nextParams.minPrice = newFilters.minPrice;
    if (newFilters.maxPrice) nextParams.maxPrice = newFilters.maxPrice;
    if (newPage > 1) nextParams.page = String(newPage);

    setSearchParams(nextParams, { replace: false });
  };

  // Called when user clicks "Áp dụng" on filter sidebar
  const handleApplyFilters = ({ typeId, minPrice, maxPrice }) => {
    const nextFilters = {
      typeId: typeId || '',
      minPrice: minPrice ? String(minPrice) : '',
      maxPrice: maxPrice ? String(maxPrice) : '',
    };
    setAppliedFilters(nextFilters);
    setPage(1);
    updateUrlParams(nextFilters, searchName, 1);
    setShowMobileFilter(false);
  };

  // Called when user clicks "Xóa bộ lọc"
  const handleResetFilters = () => {
    const clearedFilters = { typeId: '', minPrice: '', maxPrice: '' };
    setAppliedFilters(clearedFilters);
    setPage(1);
    updateUrlParams(clearedFilters, searchName, 1);
    setShowMobileFilter(false);
  };

  // Called when user searches from the ProductSearchBar on this page
  const handleSearchOnPage = (keyword) => {
    setSearchName(keyword);
    setPage(1);
    updateUrlParams(appliedFilters, keyword, 1);
  };

  // Clear search keyword
  const handleClearSearch = () => {
    setSearchName('');
    setPage(1);
    updateUrlParams(appliedFilters, '', 1);
  };

  // Active filter label helpers
  const activeTypeName = useMemo(() => {
    if (!appliedFilters.typeId) return null;
    return types.find((t) => String(t.id) === String(appliedFilters.typeId))?.type;
  }, [appliedFilters.typeId, types]);

  const hasActiveFilters = Boolean(
    appliedFilters.typeId || appliedFilters.minPrice || appliedFilters.maxPrice || searchName,
  );

  const startIdx = (page - 1) * DEFAULT_PAGE_LIMIT + 1;
  const endIdx = Math.min(page * DEFAULT_PAGE_LIMIT, meta.totalItems);

  return (
    <div className="space-y-6">
      {/* Top Banner & Search Controls */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-2 border-b border-border">
        <div>
          <h1 className="text-2xl lg:text-3xl font-bold text-text-primary">Danh mục sản phẩm</h1>
          <p className="text-sm text-text-muted mt-1">
            {!loading && meta.totalItems > 0 ? (
              <>
                Hiển thị {startIdx}–{endIdx} trong số <b>{meta.totalItems}</b> sản phẩm
              </>
            ) : (
              'Khám phá các sản phẩm thời trang chất lượng và ưu đãi hấp dẫn'
            )}
          </p>
        </div>

        {/* Search bar on product page & Mobile filter button */}
        <div className="flex items-center gap-3 w-full md:w-auto">
          <ProductSearchBar
            value={searchName}
            onSearch={handleSearchOnPage}
            placeholder="Tìm theo tên sản phẩm..."
          />

          <button
            type="button"
            onClick={() => setShowMobileFilter(true)}
            className="md:hidden shrink-0 flex items-center gap-2 px-3.5 h-11 border border-border bg-surface rounded-xl text-sm font-medium hover:bg-surface-muted transition-all shadow-sm"
          >
            <SlidersHorizontal size={17} className="text-primary" />
            <span>Lọc</span>
          </button>
        </div>
      </div>

      {/* Active Filter Chips / Badges */}
      {hasActiveFilters && (
        <div className="flex flex-wrap items-center gap-2 pt-1 text-xs">
          <span className="text-text-muted font-medium">Đang lọc theo:</span>

          {searchName && (
            <span className="inline-flex items-center gap-1.5 px-3 py-1 bg-surface-muted border border-border text-text-primary rounded-full">
              Từ khóa: <b>"{searchName}"</b>
              <button
                type="button"
                onClick={handleClearSearch}
                className="hover:text-primary ml-1"
                title="Bỏ từ khóa"
              >
                <X size={12} />
              </button>
            </span>
          )}

          {activeTypeName && (
            <span className="inline-flex items-center gap-1.5 px-3 py-1 bg-surface-muted border border-border text-text-primary rounded-full">
              Loại: <b>{activeTypeName}</b>
              <button
                type="button"
                onClick={() => handleApplyFilters({ ...appliedFilters, typeId: '' })}
                className="hover:text-primary ml-1"
                title="Bỏ loại này"
              >
                <X size={12} />
              </button>
            </span>
          )}

          {(appliedFilters.minPrice || appliedFilters.maxPrice) && (
            <span className="inline-flex items-center gap-1.5 px-3 py-1 bg-surface-muted border border-border text-text-primary rounded-full">
              Giá:{' '}
              <b>
                {appliedFilters.minPrice ? formatCurrency(appliedFilters.minPrice) : '0 ₫'} -{' '}
                {appliedFilters.maxPrice ? formatCurrency(appliedFilters.maxPrice) : 'Tối đa'}
              </b>
              <button
                type="button"
                onClick={() =>
                  handleApplyFilters({ ...appliedFilters, minPrice: '', maxPrice: '' })
                }
                className="hover:text-primary ml-1"
                title="Bỏ lọc giá"
              >
                <X size={12} />
              </button>
            </span>
          )}

          <button
            type="button"
            onClick={handleResetFilters}
            className="text-primary hover:underline text-xs font-semibold ml-1 cursor-pointer"
          >
            Xóa tất cả
          </button>
        </div>
      )}

      {/* Main Content Layout */}
      <div className="flex gap-8 items-start">
        {/* Desktop Sidebar (Dual Thumb Slider + Types + Apply button) */}
        <div className="hidden md:block">
          <ProductFilterSidebar
            types={types}
            currentFilters={appliedFilters}
            onApply={handleApplyFilters}
            onReset={handleResetFilters}
          />
        </div>

        {/* Mobile Filter Drawer */}
        {showMobileFilter && (
          <div className="fixed inset-0 z-50 md:hidden flex justify-end">
            <div
              className="fixed inset-0 bg-black/40 backdrop-blur-xs transition-opacity"
              onClick={() => setShowMobileFilter(false)}
            />
            <div className="relative w-[85%] max-w-sm h-full bg-surface shadow-2xl overflow-y-auto p-5 z-10">
              <div className="flex items-center justify-between pb-3 mb-4 border-b border-border">
                <h3 className="font-bold text-lg text-text-primary">Bộ lọc sản phẩm</h3>
                <button
                  type="button"
                  onClick={() => setShowMobileFilter(false)}
                  className="p-1 text-text-muted hover:text-text-primary rounded-lg"
                >
                  <X size={20} />
                </button>
              </div>
              <ProductFilterSidebar
                types={types}
                currentFilters={appliedFilters}
                onApply={handleApplyFilters}
                onReset={handleResetFilters}
              />
            </div>
          </div>
        )}

        {/* Product Grid / Empty State */}
        <div className="flex-1 min-w-0">
          {loading ? (
            <ProductSkeleton count={DEFAULT_PAGE_LIMIT} />
          ) : products.length > 0 ? (
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-5">
              {products.map((p) => (
                <ProductCard key={p.id} product={p} onDetail={setSelectedProduct} />
              ))}
            </div>
          ) : (
            <div className="flex flex-col items-center justify-center py-20 px-4 bg-surface rounded-2xl border border-border text-center">
              <div className="w-16 h-16 rounded-full bg-surface-muted flex items-center justify-center mb-4 text-3xl">
                🔍
              </div>
              <h3 className="text-lg font-bold text-text-primary mb-1">
                Không tìm thấy sản phẩm nào
              </h3>
              <p className="text-sm text-text-muted max-w-md mb-5">
                Rất tiếc, không có sản phẩm nào phù hợp với bộ lọc và từ khóa hiện tại. Bạn vui lòng
                thử điều chỉnh hoặc xóa bộ lọc.
              </p>
              {hasActiveFilters && (
                <button
                  type="button"
                  onClick={handleResetFilters}
                  className="px-5 py-2.5 bg-primary hover:bg-primary-hover text-white rounded-xl text-sm font-semibold transition-all hover:shadow-glow"
                >
                  Xóa bộ lọc & tìm lại
                </button>
              )}
            </div>
          )}

          {/* Pagination */}
          {!loading && meta.totalPages > 1 && (
            <div className="flex items-center justify-center gap-2 mt-10 pb-4">
              <button
                type="button"
                onClick={() => {
                  const prevPage = Math.max(1, page - 1);
                  setPage(prevPage);
                  updateUrlParams(appliedFilters, searchName, prevPage);
                  window.scrollTo({ top: 0, behavior: 'smooth' });
                }}
                disabled={page <= 1}
                className="p-2 rounded-xl border border-border bg-surface hover:bg-surface-muted disabled:opacity-30 disabled:pointer-events-none transition-all"
                title="Trang trước"
              >
                <ChevronLeft size={18} />
              </button>

              {Array.from({ length: meta.totalPages }, (_, i) => i + 1).map((pg) => {
                // Show condensed page numbers if total pages is large
                if (pg === 1 || pg === meta.totalPages || (pg >= page - 2 && pg <= page + 2)) {
                  return (
                    <button
                      key={pg}
                      type="button"
                      onClick={() => {
                        setPage(pg);
                        updateUrlParams(appliedFilters, searchName, pg);
                        window.scrollTo({ top: 0, behavior: 'smooth' });
                      }}
                      className={`min-w-9 h-9 px-3 rounded-xl text-sm font-semibold transition-all
                        ${
                          pg === page
                            ? 'bg-primary text-white shadow-glow'
                            : 'border border-border bg-surface text-text-secondary hover:bg-surface-muted'
                        }`}
                    >
                      {pg}
                    </button>
                  );
                }
                if (pg === page - 3 || pg === page + 3) {
                  return (
                    <span key={pg} className="px-1 text-text-muted select-none">
                      ...
                    </span>
                  );
                }
                return null;
              })}

              <button
                type="button"
                onClick={() => {
                  const nextPage = Math.min(meta.totalPages, page + 1);
                  setPage(nextPage);
                  updateUrlParams(appliedFilters, searchName, nextPage);
                  window.scrollTo({ top: 0, behavior: 'smooth' });
                }}
                disabled={page >= meta.totalPages}
                className="p-2 rounded-xl border border-border bg-surface hover:bg-surface-muted disabled:opacity-30 disabled:pointer-events-none transition-all"
                title="Trang sau"
              >
                <ChevronRight size={18} />
              </button>
            </div>
          )}
        </div>
      </div>

      {/* Product Detail Modal */}
      {selectedProduct && (
        <ProductModal product={selectedProduct} onClose={() => setSelectedProduct(null)} />
      )}
    </div>
  );
}

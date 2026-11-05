import { useState } from 'react';
import HeroBanner from '../features/home/components/HeroBanner';
import VoucherZone from '../features/home/components/VoucherZone';
import CategoriesSection from '../features/home/components/CategoriesSection';
import TrendingProducts from '../features/home/components/TrendingProducts';
import ProductModal from '../components/ProductModal';

export default function HomePage() {
  const [selectedProduct, setSelectedProduct] = useState(null);

  return (
    <div className="flex flex-col gap-10">
      <HeroBanner />

      <VoucherZone />
      <CategoriesSection />
      <TrendingProducts onDetail={setSelectedProduct} />

      {selectedProduct && (
        <ProductModal product={selectedProduct} onClose={() => setSelectedProduct(null)} />
      )}
    </div>
  );
}

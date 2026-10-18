import { useState, useEffect } from 'react';
import banner1 from '../../../assets/images/banner1.png';
import banner2 from '../../../assets/images/banner2.png';
import banner3 from '../../../assets/images/banner3.png';
import banner4 from '../../../assets/images/banner4.png';

export default function HeroBanner() {
  const banners = [banner1, banner2, banner3, banner4];
  const [currentIndex, setCurrentIndex] = useState(0);

  // Tự động chuyển ảnh mỗi 3 giây
  useEffect(() => {
    const timer = setInterval(() => {
      // Nếu đến ảnh cuối thì quay lại ảnh đầu tiên (chia lấy dư % vòng tròn)
      setCurrentIndex((prevIndex) => (prevIndex + 1) % banners.length);
    }, 3000);

    // Dọn dẹp timer khi component unmount
    return () => clearInterval(timer);
  }, [banners.length]);

  return (
    <section className="w-full h-[400px] rounded-xl overflow-hidden relative group">
      {/* Khung trượt chứa tất cả các ảnh */}
      <div
        className="w-full h-full flex transition-transform duration-700 ease-in-out"
        style={{ transform: `translateX(-${currentIndex * 100}%)` }}
      >
        {banners.map((banner, index) => (
          <img
            key={index}
            src={banner}
            alt={`Banner ${index + 1}`}
            className="w-full h-full object-cover flex-shrink-0"
          />
        ))}
      </div>

      {/* Tùy chọn: Nút chấm bi (dots) ở dưới cùng để biết đang ở banner thứ mấy */}
      <div className="absolute bottom-4 left-1/2 transform -translate-x-1/2 flex gap-2">
        {banners.map((_, index) => (
          <button
            key={index}
            onClick={() => setCurrentIndex(index)}
            className={`w-3 h-3 rounded-full transition-all duration-300 ${
              currentIndex === index ? 'bg-blue-600 w-8' : 'bg-white/60 hover:bg-white'
            }`}
          />
        ))}
      </div>
    </section>
  );
}

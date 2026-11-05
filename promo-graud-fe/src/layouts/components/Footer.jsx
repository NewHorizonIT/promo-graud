export default function Footer() {
  return (
    <footer className="w-full bg-surface border-t border-border mt-12 py-10">
      <div className="max-w-[1280px] mx-auto px-4 md:px-6 grid grid-cols-1 md:grid-cols-4 gap-8">
        <div className="flex flex-col gap-4">
          <h2 className="text-2xl font-bold tracking-tight">
            Promo<span className="text-primary">Wear</span>
          </h2>
          <p className="text-sm text-text-secondary">
            Nền tảng mua sắm thời trang kết hợp trải nghiệm săn voucher đỉnh cao.
          </p>
        </div>

        <div className="flex flex-col gap-3">
          <h3 className="font-semibold text-text-primary">Về chúng tôi</h3>
          <a href="#" className="text-sm text-text-secondary hover:text-primary transition-colors">
            Giới thiệu
          </a>
          <a href="#" className="text-sm text-text-secondary hover:text-primary transition-colors">
            Tuyển dụng
          </a>
          <a href="#" className="text-sm text-text-secondary hover:text-primary transition-colors">
            Chính sách bảo mật
          </a>
        </div>

        <div className="flex flex-col gap-3">
          <h3 className="font-semibold text-text-primary">Hỗ trợ khách hàng</h3>
          <a href="#" className="text-sm text-text-secondary hover:text-primary transition-colors">
            Trung tâm trợ giúp
          </a>
          <a href="#" className="text-sm text-text-secondary hover:text-primary transition-colors">
            Hướng dẫn mua hàng
          </a>
          <a href="#" className="text-sm text-text-secondary hover:text-primary transition-colors">
            Theo dõi đơn hàng
          </a>
        </div>

        <div className="flex flex-col gap-3">
          <h3 className="font-semibold text-text-primary">Liên hệ</h3>
          <p className="text-sm text-text-secondary">Email: support@promowear.vn</p>
          <p className="text-sm text-text-secondary">Hotline: 1900 6868</p>
        </div>
      </div>

      <div className="max-w-[1280px] mx-auto px-4 md:px-6 mt-10 pt-6 border-t border-border text-center text-sm text-text-muted">
        &copy; {new Date().getFullYear()} PromoWear. All rights reserved.
      </div>
    </footer>
  );
}

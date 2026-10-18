import { Routes, Route } from 'react-router-dom';
import MainLayout from '../layouts/MainLayout';
import AdminLayout from '../layouts/AdminLayout';
import HomePage from '../pages/HomePage';
import LoginPage from '../pages/LoginPage';
import ProductsPage from '../pages/ProductsPage';
import CartPage from '../pages/CartPage';
import ProfilePage from '../pages/ProfilePage';
import VoucherCollectionPage from '../pages/VoucherCollectionPage';
import ProtectedRoute from './ProtectedRoute';
import AdminLoginPage from '../pages/admin/AdminLoginPage';
import DashboardPage from '../pages/admin/DashboardPage';
import CampaignsPage from '../pages/admin/CampaignsPage';
import CampaignDetailPage from '../pages/admin/CampaignDetailPage';
import CampaignFormPage from '../pages/admin/CampaignFormPage';
import VouchersPage from '../pages/admin/VouchersPage';
import VoucherDetailPage from '../pages/admin/VoucherDetailPage';
import VoucherFormPage from '../pages/admin/VoucherFormPage';
import DistributionPage from '../pages/admin/DistributionPage';
import FraudPage from '../pages/admin/FraudPage';

export default function AppRoutes() {
  return (
    <Routes>
      {/* ── User routes ── */}
      <Route path="/" element={<MainLayout />}>
        <Route path="login" element={<LoginPage />} />
        <Route index element={<HomePage />} />
        <Route path="products" element={<ProductsPage />} />
        <Route path="cart" element={<CartPage />} />
        <Route path="profile" element={<ProfilePage />} />
        <Route
          path="vouchers"
          element={
            <ProtectedRoute>
              <VoucherCollectionPage />
            </ProtectedRoute>
          }
        />
      </Route>

      {/* ── Admin standalone (no layout) ── */}
      <Route path="/admin/login" element={<AdminLoginPage />} />

      {/* ── Admin with sidebar layout ── */}
      <Route path="/admin" element={<AdminLayout />}>
        <Route index element={<DashboardPage />} />
        <Route path="campaigns" element={<CampaignsPage />} />
        <Route path="campaigns/new" element={<CampaignFormPage />} />
        <Route path="campaigns/:id" element={<CampaignDetailPage />} />
        <Route path="campaigns/:id/edit" element={<CampaignFormPage />} />
        <Route path="vouchers" element={<VouchersPage />} />
        <Route path="vouchers/new" element={<VoucherFormPage />} />
        <Route path="vouchers/:id" element={<VoucherDetailPage />} />
        <Route path="distribution" element={<DistributionPage />} />
        <Route path="fraud" element={<FraudPage />} />
      </Route>
    </Routes>
  );
}

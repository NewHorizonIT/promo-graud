import { Routes, Route } from 'react-router-dom';
import MainLayout from '../layouts/MainLayout';
import AdminLayout from '../layouts/AdminLayout';
import HomePage from '../pages/HomePage';
import LoginPage from '../pages/LoginPage';
import ProductsPage from '../pages/ProductsPage';
import CartPage from '../pages/CartPage';
import ProfilePage from '../pages/ProfilePage';
import OrderHistoryPage from '../pages/OrderHistoryPage';
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
import OrdersPage from '../pages/admin/OrdersPage';
import OrderDetailPage from '../pages/admin/OrderDetailPage';
import ProductFormPage from '../pages/admin/ProductFormPage';
import ProductDetailPage from '../pages/admin/ProductDetailPage';
import ProductPage from '../pages/admin/ProductPage';
import ProductTypesPage from '../pages/admin/ProductTypesPage';
import UsersPage from '../pages/admin/UserPage';
import UserTypesPage from '../pages/admin/UserTypePage';
import UserFormPage from '../pages/admin/UserFormPage';
import UserDetailPage from '../pages/admin/UserDetailPage';

export default function AppRoutes() {
  return (
    <Routes>
      {/* ── User routes ── */}
      <Route path="/" element={<MainLayout />}>
        <Route path="login" element={<LoginPage />} />
        <Route index element={<HomePage />} />
        <Route path="products" element={<ProductsPage />} />
        <Route path="cart" element={<CartPage />} />
        <Route
          path="profile"
          element={
            <ProtectedRoute>
              <ProfilePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="orders"
          element={
            <ProtectedRoute>
              <OrderHistoryPage />
            </ProtectedRoute>
          }
        />
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
        <Route path="orders" element={<OrdersPage />} />
        <Route path="orders/:id" element={<OrderDetailPage />} />
        <Route path="products" element={<ProductPage />} />
        <Route path="products/new" element={<ProductFormPage />} />
        <Route path="products/:id" element={<ProductDetailPage />} />
        <Route path="products/:id/edit" element={<ProductFormPage />} />
        <Route path="product-types" element={<ProductTypesPage />} />
        <Route path="users" element={<UsersPage />} />
        <Route path="users/new" element={<UserFormPage />} />
        <Route path="users/:id" element={<UserDetailPage />} />
        <Route path="users/:id/edit" element={<UserFormPage />} />
        <Route path="user-types" element={<UserTypesPage />} />
      </Route>
    </Routes>
  );
}

import { Outlet } from 'react-router-dom';
import Header from './components/Header';
import Footer from './components/Footer';

export default function MainLayout() {
  return (
    <div className="flex flex-col min-h-screen bg-background">
      <Header />
      <main className="flex-1 w-full max-w-[1280px] mx-auto px-4 md:px-6 py-6">
        <Outlet />
      </main>
      <Footer />
    </div>
  );
}

import AppRoutes from "./routes";
import { CartProvider } from "./store/CartContext";

function App() {
  return (
    <CartProvider>
      <AppRoutes />
    </CartProvider>
  );
}

export default App;

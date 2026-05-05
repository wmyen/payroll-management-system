import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useAuthStore } from './stores/authStore';
import MainLayout from './components/layout/MainLayout';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import PaystubList from './pages/paystubs/PaystubList';
import PaystubDetail from './pages/paystubs/PaystubDetail';
import LeavePage from './pages/leaves/LeavePage';
import OvertimePage from './pages/overtime/OvertimePage';
import ProfilePage from './pages/profile/ProfilePage';

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  if (!isAuthenticated) return <Navigate to="/ess/login" />;
  return <>{children}</>;
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/ess/login" element={<Login />} />
        <Route path="/ess/" element={<ProtectedRoute><MainLayout /></ProtectedRoute>}>
          <Route index element={<Dashboard />} />
          <Route path="paystubs" element={<PaystubList />} />
          <Route path="paystubs/:id" element={<PaystubDetail />} />
          <Route path="leaves" element={<LeavePage />} />
          <Route path="overtime" element={<OvertimePage />} />
          <Route path="profile" element={<ProfilePage />} />
        </Route>
        <Route path="*" element={<Navigate to="/ess/" />} />
      </Routes>
    </BrowserRouter>
  );
}

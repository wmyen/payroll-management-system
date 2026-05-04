import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useAuthStore } from './stores/authStore';
import MainLayout from './components/layout/MainLayout';
import Login from './pages/Login';
import EmployeeList from './pages/employees/EmployeeList';
import EmployeeForm from './pages/employees/EmployeeForm';
import DepartmentTree from './pages/departments/DepartmentTree';

function Dashboard() {
  return <div className="text-gray-600">歡迎使用薪資管理系統</div>;
}

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  if (!isAuthenticated) return <Navigate to="/login" />;
  return <>{children}</>;
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/" element={<ProtectedRoute><MainLayout /></ProtectedRoute>}>
          <Route index element={<Dashboard />} />
          <Route path="employees" element={<EmployeeList />} />
          <Route path="employees/new" element={<EmployeeForm />} />
          <Route path="employees/:id" element={<EmployeeForm />} />
          <Route path="departments" element={<DepartmentTree />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

import { useAuthStore } from '../../stores/authStore';
import { useNavigate } from 'react-router-dom';

export default function Header() {
  const logout = useAuthStore((s) => s.logout);
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="bg-white border-b px-6 py-3 flex justify-between items-center">
      <h1 className="text-lg font-semibold">HR 管理後台</h1>
      <button onClick={handleLogout} className="text-sm text-gray-600 hover:text-gray-900">
        登出
      </button>
    </header>
  );
}

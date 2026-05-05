import { NavLink } from 'react-router-dom';

const navItems = [
  { to: '/ess/', label: '首頁', end: true },
  { to: '/ess/paystubs', label: '薪資單' },
  { to: '/ess/leaves', label: '請假管理' },
  { to: '/ess/overtime', label: '加班管理' },
  { to: '/ess/profile', label: '個人資料' },
];

export default function Sidebar() {
  return (
    <aside className="w-48 bg-slate-700 text-white min-h-screen p-4">
      <h2 className="text-lg font-bold mb-6">員工自助服務</h2>
      <nav className="space-y-2">
        {navItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.end}
            className={({ isActive }) =>
              `block px-3 py-2 rounded ${isActive ? 'bg-teal-600' : 'hover:bg-slate-600'}`
            }
          >
            {item.label}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}

import { NavLink } from 'react-router-dom';

const navItems = [
  { to: '/', label: '首頁' },
  { to: '/employees', label: '員工管理' },
  { to: '/departments', label: '部門管理' },
];

const attendanceItems = [
  { to: '/attendance', label: '出勤記錄' },
  { to: '/leaves', label: '請假管理' },
  { to: '/overtime', label: '加班管理' },
  { to: '/holidays', label: '假日管理' },
];

const payrollItems = [
  { to: '/payroll/periods', label: '薪資期間' },
  { to: '/payroll/summary', label: '薪資總表' },
];

export default function Sidebar() {
  return (
    <aside className="w-48 bg-gray-800 text-white min-h-screen p-4">
      <h2 className="text-lg font-bold mb-6">薪資管理系統</h2>
      <nav className="space-y-2">
        {navItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.to === '/'}
            className={({ isActive }) =>
              `block px-3 py-2 rounded ${isActive ? 'bg-blue-600' : 'hover:bg-gray-700'}`
            }
          >
            {item.label}
          </NavLink>
        ))}
        <div className="mt-4 mb-2 px-3 text-xs text-gray-400 uppercase tracking-wider">考勤管理</div>
        {attendanceItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) =>
              `block px-3 py-2 rounded ${isActive ? 'bg-blue-600' : 'hover:bg-gray-700'}`
            }
          >
            {item.label}
          </NavLink>
        ))}
        <div className="mt-4 mb-2 px-3 text-xs text-gray-400 uppercase tracking-wider">薪資管理</div>
        {payrollItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) =>
              `block px-3 py-2 rounded ${isActive ? 'bg-blue-600' : 'hover:bg-gray-700'}`
            }
          >
            {item.label}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}

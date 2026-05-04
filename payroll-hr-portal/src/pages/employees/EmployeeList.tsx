import { useEffect, useState } from 'react';
import { employeeApi } from '../../api/employees';
import type { Employee } from '../../types';
import { useNavigate } from 'react-router-dom';

export default function EmployeeList() {
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    employeeApi.search({ name: search || undefined, page, size: 20 })
      .then(({ data }) => {
        setEmployees(data.data.content);
        setTotalPages(data.data.totalPages);
      });
  }, [page, search]);

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-xl font-semibold">員工管理</h2>
        <button
          onClick={() => navigate('/employees/new')}
          className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
        >
          新增員工
        </button>
      </div>

      <div className="mb-4">
        <input
          type="text"
          placeholder="搜尋員工姓名..."
          value={search}
          onChange={(e) => { setSearch(e.target.value); setPage(0); }}
          className="border rounded px-3 py-2 w-64"
        />
      </div>

      <table className="w-full bg-white rounded shadow">
        <thead className="bg-gray-50">
          <tr>
            <th className="px-4 py-2 text-left">姓名</th>
            <th className="px-4 py-2 text-left">部門</th>
            <th className="px-4 py-2 text-left">職位級別</th>
            <th className="px-4 py-2 text-left">到職日</th>
            <th className="px-4 py-2 text-left">狀態</th>
            <th className="px-4 py-2 text-left">操作</th>
          </tr>
        </thead>
        <tbody>
          {employees.map((emp) => (
            <tr key={emp.id} className="border-t hover:bg-gray-50">
              <td className="px-4 py-2">{emp.name}</td>
              <td className="px-4 py-2">{emp.department?.name ?? '-'}</td>
              <td className="px-4 py-2">{emp.jobLevel ?? '-'}</td>
              <td className="px-4 py-2">{emp.hireDate}</td>
              <td className="px-4 py-2">
                <span className={`px-2 py-1 rounded text-xs ${
                  emp.status === 'ACTIVE' ? 'bg-green-100 text-green-800' :
                  emp.status === 'LEFT' ? 'bg-red-100 text-red-800' :
                  'bg-yellow-100 text-yellow-800'
                }`}>
                  {emp.status === 'ACTIVE' ? '在職' : emp.status === 'LEFT' ? '離職' : '停職'}
                </span>
              </td>
              <td className="px-4 py-2">
                <button
                  onClick={() => navigate(`/employees/${emp.id}`)}
                  className="text-blue-600 hover:underline"
                >
                  檢視
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <div className="flex justify-center gap-2 mt-4">
        <button
          onClick={() => setPage((p) => Math.max(0, p - 1))}
          disabled={page === 0}
          className="px-3 py-1 border rounded disabled:opacity-50"
        >
          上一頁
        </button>
        <span className="px-3 py-1">第 {page + 1} / {totalPages} 頁</span>
        <button
          onClick={() => setPage((p) => p + 1)}
          disabled={page >= totalPages - 1}
          className="px-3 py-1 border rounded disabled:opacity-50"
        >
          下一頁
        </button>
      </div>
    </div>
  );
}

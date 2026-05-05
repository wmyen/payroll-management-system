import { useState, useEffect } from 'react';
import { reportApi, type DepartmentCostReport as DepartmentCostData } from '../../api/payroll';

const fmt = (n: number) => n.toLocaleString('zh-TW', { minimumFractionDigits: 0, maximumFractionDigits: 0 });

export default function DepartmentCostReport() {
  const [year, setYear] = useState(new Date().getFullYear());
  const [month, setMonth] = useState<number | ''>('');
  const [report, setReport] = useState<DepartmentCostData | null>(null);

  useEffect(() => { fetchReport(); }, [year, month]);

  const fetchReport = async () => {
    const res = await reportApi.departmentCost(year, month || undefined);
    setReport(res.data.data);
  };

  const maxNet = report ? Math.max(...report.departments.map(d => d.totalNetPay), 1) : 1;

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-xl font-bold">部門成本分析</h1>
        <div className="flex gap-2 items-center">
          <input type="number" value={year} onChange={(e) => setYear(parseInt(e.target.value))}
            className="border rounded px-2 py-1 w-24 text-sm" />
          <select value={month} onChange={(e) => setMonth(e.target.value ? parseInt(e.target.value) : '')}
            className="border rounded px-2 py-1 text-sm">
            <option value="">全年</option>
            {Array.from({ length: 12 }, (_, i) => i + 1).map(m => (
              <option key={m} value={m}>{m}月</option>
            ))}
          </select>
        </div>
      </div>

      {!report || report.departments.length === 0 ? (
        <div className="text-gray-400">查無資料</div>
      ) : (
        <>
          <div className="grid grid-cols-2 gap-4 mb-4">
            <div className="bg-white p-4 rounded shadow text-center">
              <div className="text-sm text-gray-500">全公司實發總額</div>
              <div className="text-xl font-bold text-green-600">${fmt(report.companyTotal.totalNetPay)}</div>
            </div>
            <div className="bg-white p-4 rounded shadow text-center">
              <div className="text-sm text-gray-500">全公司雇主成本</div>
              <div className="text-xl font-bold text-blue-600">${fmt(report.companyTotal.totalEmployerCost)}</div>
            </div>
          </div>

          <div className="space-y-3 mb-6">
            <h3 className="font-semibold text-sm text-gray-600">成本佔比（CSS 橫條圖）</h3>
            {report.departments.map((dept, i) => (
              <div key={i} className="flex items-center gap-3">
                <div className="w-32 text-sm text-right">{dept.departmentName}</div>
                <div className="flex-1 bg-gray-100 rounded h-6 relative">
                  <div className="bg-blue-500 h-6 rounded flex items-center px-2 text-white text-xs"
                    style={{ width: `${(dept.totalNetPay / maxNet) * 100}%` }}>
                    ${fmt(dept.totalNetPay)}
                  </div>
                </div>
                <div className="w-16 text-sm text-right">{dept.percentage}%</div>
              </div>
            ))}
          </div>

          <div className="bg-white rounded shadow overflow-auto">
            <table className="w-full text-sm">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-3 py-2 text-left">部門</th>
                  <th className="px-3 py-2 text-right">人數</th>
                  <th className="px-3 py-2 text-right">實發合計</th>
                  <th className="px-3 py-2 text-right">雇主成本</th>
                  <th className="px-3 py-2 text-right">佔比</th>
                </tr>
              </thead>
              <tbody>
                {report.departments.map((dept, i) => (
                  <tr key={i} className="border-t hover:bg-gray-50">
                    <td className="px-3 py-2">{dept.departmentName}</td>
                    <td className="px-3 py-2 text-right">{dept.employeeCount}</td>
                    <td className="px-3 py-2 text-right">${fmt(dept.totalNetPay)}</td>
                    <td className="px-3 py-2 text-right">${fmt(dept.totalEmployerCost)}</td>
                    <td className="px-3 py-2 text-right">{dept.percentage}%</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  );
}

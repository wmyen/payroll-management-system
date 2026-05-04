import { useState, useEffect } from 'react';
import { payrollApi, PayrollPeriod } from '../../api/payroll';

const statusLabels: Record<string, { text: string; cls: string }> = {
  DRAFT: { text: '草稿', cls: 'bg-gray-100 text-gray-700' },
  PROCESSING: { text: '計算中', cls: 'bg-yellow-100 text-yellow-700' },
  CONFIRMED: { text: '已計算', cls: 'bg-blue-100 text-blue-700' },
  LOCKED: { text: '已鎖定', cls: 'bg-green-100 text-green-700' },
};

export default function PayrollPeriodList() {
  const [periods, setPeriods] = useState<PayrollPeriod[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [year, setYear] = useState(new Date().getFullYear());
  const [month, setMonth] = useState(new Date().getMonth() + 1);
  const [loading, setLoading] = useState(false);

  useEffect(() => { fetchPeriods(); }, []);

  const fetchPeriods = async () => {
    const res = await payrollApi.listPeriods();
    setPeriods(res.data.data);
  };

  const handleCreate = async () => {
    const startDate = `${year}-${String(month).padStart(2, '0')}-01`;
    const lastDay = new Date(year, month, 0).getDate();
    const endDate = `${year}-${String(month).padStart(2, '0')}-${lastDay}`;
    const payDate = `${year}-${String(month).padStart(2, '0')}-${Math.min(lastDay, 5)}`;
    await payrollApi.createPeriod({ year, month, startDate, endDate, payDate });
    setShowForm(false);
    fetchPeriods();
  };

  const handleCalculate = async (id: number) => {
    if (!confirm('確認計算此期間所有員工薪資？')) return;
    setLoading(true);
    try {
      await payrollApi.calculate(id);
      fetchPeriods();
    } finally { setLoading(false); }
  };

  const handleConfirm = async (id: number) => {
    if (!confirm('確認鎖定？鎖定後無法修改。')) return;
    await payrollApi.confirm(id);
    fetchPeriods();
  };

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-xl font-bold">薪資期間管理</h1>
        <button onClick={() => setShowForm(!showForm)} className="bg-blue-600 text-white px-4 py-2 rounded">
          新增期間
        </button>
      </div>

      {showForm && (
        <div className="bg-white p-4 rounded shadow mb-4 flex gap-4 items-end">
          <div>
            <label className="block text-sm text-gray-600">年份</label>
            <input type="number" value={year} onChange={(e) => setYear(Number(e.target.value))}
              className="border rounded px-2 py-1 w-24" />
          </div>
          <div>
            <label className="block text-sm text-gray-600">月份</label>
            <select value={month} onChange={(e) => setMonth(Number(e.target.value))}
              className="border rounded px-2 py-1">
              {Array.from({ length: 12 }, (_, i) => i + 1).map(m => (
                <option key={m} value={m}>{m}月</option>
              ))}
            </select>
          </div>
          <button onClick={handleCreate} className="bg-green-600 text-white px-4 py-2 rounded">確認建立</button>
          <button onClick={() => setShowForm(false)} className="border px-4 py-2 rounded">取消</button>
        </div>
      )}

      <table className="w-full bg-white rounded shadow">
        <thead className="bg-gray-50">
          <tr>
            <th className="px-4 py-2 text-left text-sm">期間</th>
            <th className="px-4 py-2 text-left text-sm">起迄日</th>
            <th className="px-4 py-2 text-left text-sm">發薪日</th>
            <th className="px-4 py-2 text-left text-sm">狀態</th>
            <th className="px-4 py-2 text-left text-sm">操作</th>
          </tr>
        </thead>
        <tbody>
          {periods.map((p) => {
            const s = statusLabels[p.status] || statusLabels.DRAFT;
            return (
              <tr key={p.id} className="border-t hover:bg-gray-50">
                <td className="px-4 py-2 font-medium">{p.year}年 {p.month}月</td>
                <td className="px-4 py-2 text-sm">{p.startDate} ~ {p.endDate}</td>
                <td className="px-4 py-2 text-sm">{p.payDate}</td>
                <td className="px-4 py-2">
                  <span className={`px-2 py-1 rounded text-xs ${s.cls}`}>{s.text}</span>
                </td>
                <td className="px-4 py-2 space-x-2">
                  {p.status === 'DRAFT' && (
                    <button onClick={() => handleCalculate(p.id)} disabled={loading}
                      className="bg-blue-500 text-white px-3 py-1 rounded text-sm">
                      {loading ? '計算中...' : '計算薪資'}
                    </button>
                  )}
                  {p.status === 'CONFIRMED' && (
                    <button onClick={() => handleConfirm(p.id)}
                      className="bg-green-600 text-white px-3 py-1 rounded text-sm">鎖定</button>
                  )}
                  {(p.status === 'CONFIRMED' || p.status === 'LOCKED') && (
                    <a href={`/payroll/summary?periodId=${p.id}`}
                      className="bg-gray-600 text-white px-3 py-1 rounded text-sm inline-block">檢視總表</a>
                  )}
                </td>
              </tr>
            );
          })}
          {periods.length === 0 && (
            <tr><td colSpan={5} className="px-4 py-8 text-center text-gray-400">尚無薪資期間</td></tr>
          )}
        </tbody>
      </table>
    </div>
  );
}

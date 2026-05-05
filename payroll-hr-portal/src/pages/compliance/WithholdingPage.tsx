import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { complianceApi, WithholdingStatement } from '../../api/payroll';

const fmt = (n: number) => n.toLocaleString('zh-TW', { minimumFractionDigits: 0, maximumFractionDigits: 0 });

export default function WithholdingPage() {
  const navigate = useNavigate();
  const [year, setYear] = useState(new Date().getFullYear());
  const [statements, setStatements] = useState<WithholdingStatement[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => { fetchStatements(); }, [year]);

  const fetchStatements = async () => {
    const res = await complianceApi.listWithholding(year);
    setStatements(res.data.data);
  };

  const handleGenerate = async () => {
    setLoading(true);
    try {
      await complianceApi.generateWithholding(year);
      fetchStatements();
    } finally {
      setLoading(false);
    }
  };

  const handleConfirmAll = async () => {
    if (!confirm(`確認 ${year} 年度所有扣繳憑單？此操作不可撤銷。`)) return;
    await complianceApi.confirmAllWithholding(year);
    fetchStatements();
  };

  const handleConfirm = async (id: number) => {
    await complianceApi.confirmWithholding(id);
    fetchStatements();
  };

  const totalGross = statements.reduce((s, w) => s + w.totalGross, 0);
  const totalTax = statements.reduce((s, w) => s + w.totalIncomeTax, 0);
  const totalNet = statements.reduce((s, w) => s + w.totalNetPay, 0);

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-xl font-bold">年度扣繳憑單</h1>
        <div className="flex gap-2 items-center">
          <input type="number" value={year} onChange={(e) => setYear(parseInt(e.target.value))}
            className="border rounded px-2 py-1 w-24" />
          <button onClick={handleGenerate} disabled={loading}
            className="bg-blue-600 text-white px-4 py-2 rounded text-sm disabled:opacity-50">
            {loading ? '產出中...' : '批次產出'}
          </button>
          {statements.length > 0 && (
            <button onClick={handleConfirmAll}
              className="bg-green-600 text-white px-4 py-2 rounded text-sm">
              全部確認
            </button>
          )}
        </div>
      </div>

      {statements.length > 0 && (
        <div className="grid grid-cols-3 gap-4 mb-4">
          <div className="bg-white p-4 rounded shadow text-center">
            <div className="text-sm text-gray-500">全年應稅總額</div>
            <div className="text-xl font-bold">${fmt(totalGross)}</div>
          </div>
          <div className="bg-white p-4 rounded shadow text-center">
            <div className="text-sm text-gray-500">全年扣繳稅額</div>
            <div className="text-xl font-bold text-red-600">${fmt(totalTax)}</div>
          </div>
          <div className="bg-white p-4 rounded shadow text-center">
            <div className="text-sm text-gray-500">全年實領總額</div>
            <div className="text-xl font-bold text-green-600">${fmt(totalNet)}</div>
          </div>
        </div>
      )}

      <div className="bg-white rounded shadow overflow-auto">
        <table className="w-full text-sm">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-3 py-2 text-left">員工ID</th>
              <th className="px-3 py-2 text-right">計薪月份</th>
              <th className="px-3 py-2 text-right">應稅所得</th>
              <th className="px-3 py-2 text-right">勞保費</th>
              <th className="px-3 py-2 text-right">健保費</th>
              <th className="px-3 py-2 text-right">所得稅</th>
              <th className="px-3 py-2 text-right">實領合計</th>
              <th className="px-3 py-2 text-right">雇主成本</th>
              <th className="px-3 py-2 text-center">狀態</th>
              <th className="px-3 py-2"></th>
            </tr>
          </thead>
          <tbody>
            {statements.map((w) => (
              <tr key={w.id} className="border-t hover:bg-gray-50 cursor-pointer"
                onClick={() => navigate(`/compliance/withholding/${w.id}`)}>
                <td className="px-3 py-2">{w.employeeId}</td>
                <td className="px-3 py-2 text-right">{w.monthCount} 個月</td>
                <td className="px-3 py-2 text-right">${fmt(w.totalGross)}</td>
                <td className="px-3 py-2 text-right">${fmt(w.totalLaborInsurance)}</td>
                <td className="px-3 py-2 text-right">${fmt(w.totalHealthInsurance)}</td>
                <td className="px-3 py-2 text-right">${fmt(w.totalIncomeTax)}</td>
                <td className="px-3 py-2 text-right font-semibold">${fmt(w.totalNetPay)}</td>
                <td className="px-3 py-2 text-right">${fmt(w.totalEmployerCost)}</td>
                <td className="px-3 py-2 text-center">
                  <span className={`px-2 py-0.5 rounded text-xs ${
                    w.status === 'CONFIRMED' ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-600'
                  }`}>
                    {w.status === 'CONFIRMED' ? '已確認' : '草稿'}
                  </span>
                </td>
                <td className="px-3 py-2">
                  {w.status !== 'CONFIRMED' && (
                    <button onClick={(e) => { e.stopPropagation(); handleConfirm(w.id); }}
                      className="text-blue-600 text-xs">確認</button>
                  )}
                </td>
              </tr>
            ))}
            {statements.length === 0 && (
              <tr><td colSpan={10} className="px-3 py-4 text-center text-gray-400">
                {year} 年尚無扣繳憑單，點擊「批次產出」產生
              </td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

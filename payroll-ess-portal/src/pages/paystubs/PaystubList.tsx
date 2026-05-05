import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { essApi, type PaystubSummary } from '../../api/ess';

const fmt = (n: number) => n.toLocaleString('zh-TW', { minimumFractionDigits: 0, maximumFractionDigits: 0 });

export default function PaystubList() {
  const [paystubs, setPaystubs] = useState<PaystubSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    essApi.paystubs().then(res => {
      setPaystubs(res.data.data);
    }).finally(() => setLoading(false));
  }, []);

  return (
    <div>
      <h1 className="text-xl font-bold mb-4">薪資單查詢</h1>

      {loading ? (
        <div className="text-gray-400">載入中...</div>
      ) : paystubs.length === 0 ? (
        <div className="text-gray-400">尚無薪資紀錄</div>
      ) : (
        <div className="bg-white rounded shadow overflow-auto">
          <table className="w-full text-sm">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-3 py-2 text-left">薪資期間</th>
                <th className="px-3 py-2 text-right">應發金額</th>
                <th className="px-3 py-2 text-right">應扣金額</th>
                <th className="px-3 py-2 text-right">實領金額</th>
                <th className="px-3 py-2 text-center">狀態</th>
                <th className="px-3 py-2 text-center">操作</th>
              </tr>
            </thead>
            <tbody>
              {paystubs.map((p) => (
                <tr key={p.recordId} className="border-t hover:bg-gray-50">
                  <td className="px-3 py-2">
                    {p.period ? `${p.period.year}年${p.period.month}月` : '—'}
                  </td>
                  <td className="px-3 py-2 text-right">${fmt(p.grossPay)}</td>
                  <td className="px-3 py-2 text-right">${fmt(p.totalDeductions)}</td>
                  <td className="px-3 py-2 text-right font-semibold text-green-600">${fmt(p.netPay)}</td>
                  <td className="px-3 py-2 text-center">
                    <span className={`text-xs px-2 py-0.5 rounded ${p.status === 'CONFIRMED' ? 'bg-green-100 text-green-700' : 'bg-yellow-100 text-yellow-700'}`}>
                      {p.status === 'CONFIRMED' ? '已確認' : '草稿'}
                    </span>
                  </td>
                  <td className="px-3 py-2 text-center">
                    <button onClick={() => navigate(`/ess/paystubs/${p.recordId}`)}
                      className="text-teal-600 hover:underline text-sm">
                      檢視
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

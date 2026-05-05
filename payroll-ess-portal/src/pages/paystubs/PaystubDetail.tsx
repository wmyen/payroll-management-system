import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { essApi, type PaystubDetail } from '../../api/ess';

const fmt = (n: number) => n.toLocaleString('zh-TW', { minimumFractionDigits: 0, maximumFractionDigits: 0 });

export default function PaystubDetail() {
  const { id } = useParams<{ id: string }>();
  const [detail, setDetail] = useState<PaystubDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    if (id) {
      essApi.paystubDetail(Number(id)).then(res => {
        setDetail(res.data.data);
      }).finally(() => setLoading(false));
    }
  }, [id]);

  if (loading) return <div className="text-gray-400">載入中...</div>;
  if (!detail) return <div className="text-gray-400">查無資料</div>;

  const { record, period, items } = detail;
  const periodLabel = period ? `${period.year}年${period.month}月` : '—';

  return (
    <div>
      <div className="flex items-center gap-3 mb-4">
        <button onClick={() => navigate('/ess/paystubs')} className="text-sm text-gray-500 hover:text-gray-700">
          ← 返回列表
        </button>
        <h1 className="text-xl font-bold">{periodLabel} 薪資單</h1>
      </div>

      <div className="bg-white p-6 rounded shadow max-w-2xl">
        <div className="grid grid-cols-2 gap-4 mb-6">
          <div className="text-center p-3 bg-gray-50 rounded">
            <div className="text-sm text-gray-500">應發合計</div>
            <div className="text-xl font-bold text-blue-600">${fmt(record.grossPay)}</div>
          </div>
          <div className="text-center p-3 bg-gray-50 rounded">
            <div className="text-sm text-gray-500">實領金額</div>
            <div className="text-xl font-bold text-green-600">${fmt(record.netPay)}</div>
          </div>
        </div>

        <h3 className="font-semibold mb-2">應發項目</h3>
        <table className="w-full text-sm mb-4">
          <tbody>
            <tr className="border-t"><td className="py-1.5">本薪</td><td className="text-right">${fmt(record.baseSalary)}</td></tr>
            <tr className="border-t"><td className="py-1.5">津貼合計</td><td className="text-right">${fmt(record.totalAllowances)}</td></tr>
            <tr className="border-t"><td className="py-1.5">加班費</td><td className="text-right">${fmt(record.overtimePay)}</td></tr>
            <tr className="border-t"><td className="py-1.5">其他收入</td><td className="text-right">${fmt(record.otherEarnings)}</td></tr>
            {items.filter(i => i.itemType === 'EARNING').map(i => (
              <tr key={i.id} className="border-t"><td className="py-1.5">{i.name}</td><td className="text-right">${fmt(i.amount)}</td></tr>
            ))}
            <tr className="border-t font-semibold bg-blue-50"><td className="py-1.5">應發合計</td><td className="text-right">${fmt(record.grossPay)}</td></tr>
          </tbody>
        </table>

        <h3 className="font-semibold mb-2">應扣項目</h3>
        <table className="w-full text-sm mb-4">
          <tbody>
            <tr className="border-t"><td className="py-1.5">勞保費</td><td className="text-right">${fmt(record.laborInsurance)}</td></tr>
            <tr className="border-t"><td className="py-1.5">健保費</td><td className="text-right">${fmt(record.healthInsurance)}</td></tr>
            <tr className="border-t"><td className="py-1.5">所得稅</td><td className="text-right">${fmt(record.incomeTax)}</td></tr>
            <tr className="border-t"><td className="py-1.5">請假扣薪</td><td className="text-right">${fmt(record.leaveDeduction)}</td></tr>
            <tr className="border-t"><td className="py-1.5">其他扣項</td><td className="text-right">${fmt(record.otherDeductions)}</td></tr>
            {items.filter(i => i.itemType === 'DEDUCTION').map(i => (
              <tr key={i.id} className="border-t"><td className="py-1.5">{i.name}</td><td className="text-right">${fmt(i.amount)}</td></tr>
            ))}
            <tr className="border-t font-semibold bg-red-50"><td className="py-1.5">應扣合計</td><td className="text-right">${fmt(record.totalDeductions)}</td></tr>
          </tbody>
        </table>

        {record.remark && (
          <div className="text-sm text-gray-500 border-t pt-2">備註：{record.remark}</div>
        )}
      </div>
    </div>
  );
}

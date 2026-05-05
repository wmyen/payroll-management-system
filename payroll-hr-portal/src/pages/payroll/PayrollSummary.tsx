import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { payrollApi, type PayrollRecord } from '../../api/payroll';

const fmt = (n: number) => n.toLocaleString('zh-TW', { minimumFractionDigits: 0, maximumFractionDigits: 0 });

export default function PayrollSummary() {
  const [searchParams] = useSearchParams();
  const periodId = Number(searchParams.get('periodId'));
  const [records, setRecords] = useState<PayrollRecord[]>([]);
  const [totals, setTotals] = useState({ gross: 0, deductions: 0, net: 0, employerCost: 0 });

  useEffect(() => {
    if (periodId) fetchRecords();
  }, [periodId]);

  const fetchRecords = async () => {
    const res = await payrollApi.listRecords(periodId);
    const data = res.data.data;
    setRecords(data);
    setTotals({
      gross: data.reduce((s, r) => s + r.grossPay, 0),
      deductions: data.reduce((s, r) => s + r.totalDeductions, 0),
      net: data.reduce((s, r) => s + r.netPay, 0),
      employerCost: data.reduce((s, r) => s + r.totalEmployerCost, 0),
    });
  };

  if (!periodId) {
    return <div className="text-gray-500">請從薪資期間管理選擇一個期間檢視總表</div>;
  }

  return (
    <div>
      <h1 className="text-xl font-bold mb-4">薪資總表</h1>

      {/* Summary cards */}
      <div className="grid grid-cols-4 gap-4 mb-6">
        <div className="bg-white p-4 rounded shadow">
          <div className="text-sm text-gray-500">應稅薪資合計</div>
          <div className="text-xl font-bold">${fmt(totals.gross)}</div>
        </div>
        <div className="bg-white p-4 rounded shadow">
          <div className="text-sm text-gray-500">扣項合計</div>
          <div className="text-xl font-bold text-red-600">${fmt(totals.deductions)}</div>
        </div>
        <div className="bg-white p-4 rounded shadow">
          <div className="text-sm text-gray-500">實發合計</div>
          <div className="text-xl font-bold text-green-600">${fmt(totals.net)}</div>
        </div>
        <div className="bg-white p-4 rounded shadow">
          <div className="text-sm text-gray-500">雇主總成本</div>
          <div className="text-xl font-bold text-blue-600">${fmt(totals.employerCost)}</div>
        </div>
      </div>

      {/* Detail table */}
      <div className="overflow-x-auto">
        <table className="w-full bg-white rounded shadow text-sm">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-3 py-2 text-left">員工ID</th>
              <th className="px-3 py-2 text-right">本薪</th>
              <th className="px-3 py-2 text-right">津貼</th>
              <th className="px-3 py-2 text-right">加班費</th>
              <th className="px-3 py-2 text-right">其他收入</th>
              <th className="px-3 py-2 text-right font-semibold">應稅合計</th>
              <th className="px-3 py-2 text-right">勞保</th>
              <th className="px-3 py-2 text-right">健保</th>
              <th className="px-3 py-2 text-right">所得稅</th>
              <th className="px-3 py-2 text-right">請假扣薪</th>
              <th className="px-3 py-2 text-right">其他扣項</th>
              <th className="px-3 py-2 text-right font-semibold text-red-600">扣項合計</th>
              <th className="px-3 py-2 text-right font-semibold text-green-600">實領</th>
              <th className="px-3 py-2 text-right text-blue-600">雇主成本</th>
              <th className="px-3 py-2 text-center">操作</th>
            </tr>
          </thead>
          <tbody>
            {records.map((r) => (
              <tr key={r.id} className="border-t hover:bg-gray-50">
                <td className="px-3 py-2">{r.employeeId}</td>
                <td className="px-3 py-2 text-right">${fmt(r.baseSalary)}</td>
                <td className="px-3 py-2 text-right">${fmt(r.totalAllowances)}</td>
                <td className="px-3 py-2 text-right">${fmt(r.overtimePay)}</td>
                <td className="px-3 py-2 text-right">${fmt(r.otherEarnings)}</td>
                <td className="px-3 py-2 text-right font-semibold">${fmt(r.grossPay)}</td>
                <td className="px-3 py-2 text-right">${fmt(r.laborInsurance)}</td>
                <td className="px-3 py-2 text-right">${fmt(r.healthInsurance)}</td>
                <td className="px-3 py-2 text-right">${fmt(r.incomeTax)}</td>
                <td className="px-3 py-2 text-right">${fmt(r.leaveDeduction)}</td>
                <td className="px-3 py-2 text-right">${fmt(r.otherDeductions)}</td>
                <td className="px-3 py-2 text-right text-red-600">${fmt(r.totalDeductions)}</td>
                <td className="px-3 py-2 text-right font-semibold text-green-600">${fmt(r.netPay)}</td>
                <td className="px-3 py-2 text-right text-blue-600">${fmt(r.totalEmployerCost)}</td>
                <td className="px-3 py-2 text-center">
                  <a href={`/payroll/records/${r.id}`} className="text-blue-600 hover:underline">明細</a>
                </td>
              </tr>
            ))}
            {records.length === 0 && (
              <tr><td colSpan={15} className="px-4 py-8 text-center text-gray-400">尚無薪資紀錄</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

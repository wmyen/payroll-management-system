import { useState, useEffect } from 'react';
import { payrollApi, type PayrollPeriod, type PayrollRecord } from '../../api/payroll';
import { reportApi } from '../../api/payroll';
import client from '../../api/client';

const fmt = (n: number) => n.toLocaleString('zh-TW', { minimumFractionDigits: 0, maximumFractionDigits: 0 });

interface TransferRow {
  bankAccount: string;
  employeeName: string;
  amount: string;
}

export default function BankTransferPage() {
  const [periods, setPeriods] = useState<PayrollPeriod[]>([]);
  const [selectedPeriod, setSelectedPeriod] = useState<number>(0);
  const [records, setRecords] = useState<TransferRow[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    payrollApi.listPeriods().then(res => {
      const data = res.data.data;
      setPeriods(data);
      if (data.length > 0) setSelectedPeriod(data[0].id);
    });
  }, []);

  useEffect(() => {
    if (selectedPeriod) fetchRecords();
  }, [selectedPeriod]);

  const fetchRecords = async () => {
    setLoading(true);
    try {
      const res = await payrollApi.listRecords(selectedPeriod);
      const payrollRecords: PayrollRecord[] = res.data.data;
      setRecords(payrollRecords.map(r => ({
        bankAccount: '****',
        employeeName: `員工 ${r.employeeId}`,
        amount: fmt(r.netPay),
      })));
    } finally {
      setLoading(false);
    }
  };

  const handleDownload = async () => {
    const token = localStorage.getItem('token');
    const res = await fetch(reportApi.bankTransferUrl(selectedPeriod), {
      headers: { Authorization: `Bearer ${token}` },
    });
    const blob = await res.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `bank-transfer.txt`;
    a.click();
    window.URL.revokeObjectURL(url);
  };

  const totalAmount = records.reduce((s, r) => s + parseInt(r.amount.replace(/,/g, ''), 10) || 0, 0);

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-xl font-bold">銀行轉帳檔</h1>
        <div className="flex gap-2 items-center">
          <select value={selectedPeriod} onChange={(e) => setSelectedPeriod(Number(e.target.value))}
            className="border rounded px-2 py-1 text-sm">
            <option value={0}>選擇期間</option>
            {periods.map(p => (
              <option key={p.id} value={p.id}>{p.year}年{p.month}月</option>
            ))}
          </select>
          {selectedPeriod > 0 && (
            <button onClick={handleDownload} className="bg-green-600 text-white px-3 py-1 rounded text-sm">
              下載轉帳檔
            </button>
          )}
        </div>
      </div>

      {!selectedPeriod ? (
        <div className="text-gray-400">請選擇薪資期間</div>
      ) : loading ? (
        <div className="text-gray-400">載入中...</div>
      ) : (
        <>
          <div className="bg-white p-4 rounded shadow mb-4 text-center">
            <div className="text-sm text-gray-500">轉帳總金額</div>
            <div className="text-2xl font-bold text-green-600">${fmt(totalAmount)}</div>
            <div className="text-sm text-gray-500">共 {records.length} 筆</div>
          </div>

          <div className="bg-white rounded shadow overflow-auto">
            <table className="w-full text-sm">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-3 py-2 text-left">銀行帳號</th>
                  <th className="px-3 py-2 text-left">員工姓名</th>
                  <th className="px-3 py-2 text-right">轉帳金額</th>
                </tr>
              </thead>
              <tbody>
                {records.map((r, i) => (
                  <tr key={i} className="border-t hover:bg-gray-50">
                    <td className="px-3 py-2">{r.bankAccount}</td>
                    <td className="px-3 py-2">{r.employeeName}</td>
                    <td className="px-3 py-2 text-right">${r.amount}</td>
                  </tr>
                ))}
                {records.length === 0 && (
                  <tr><td colSpan={3} className="px-3 py-4 text-center text-gray-400">尚無資料</td></tr>
                )}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  );
}

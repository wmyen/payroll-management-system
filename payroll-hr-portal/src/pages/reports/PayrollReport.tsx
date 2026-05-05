import { useState, useEffect } from 'react';
import { payrollApi, PayrollPeriod } from '../../api/payroll';
import { reportApi, PayrollSummaryReport } from '../../api/payroll';

const fmt = (n: number) => n.toLocaleString('zh-TW', { minimumFractionDigits: 0, maximumFractionDigits: 0 });

export default function PayrollReport() {
  const [periods, setPeriods] = useState<PayrollPeriod[]>([]);
  const [selectedPeriod, setSelectedPeriod] = useState<number>(0);
  const [report, setReport] = useState<PayrollSummaryReport | null>(null);

  useEffect(() => {
    payrollApi.listPeriods().then(res => {
      const data = res.data.data;
      setPeriods(data);
      if (data.length > 0) setSelectedPeriod(data[0].id);
    });
  }, []);

  useEffect(() => {
    if (selectedPeriod) fetchReport();
  }, [selectedPeriod]);

  const fetchReport = async () => {
    const res = await reportApi.payrollSummary(selectedPeriod);
    setReport(res.data.data);
  };

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-xl font-bold">薪資總表報表</h1>
        <div className="flex gap-2 items-center">
          <select value={selectedPeriod} onChange={(e) => setSelectedPeriod(Number(e.target.value))}
            className="border rounded px-2 py-1 text-sm">
            <option value={0}>選擇期間</option>
            {periods.map(p => (
              <option key={p.id} value={p.id}>{p.year}年{p.month}月</option>
            ))}
          </select>
          {selectedPeriod > 0 && (
            <>
              <button onClick={() => fetchReport()} className="bg-blue-600 text-white px-3 py-1 rounded text-sm">查詢</button>
              <a href={reportApi.exportPayrollUrl(selectedPeriod)} className="bg-green-600 text-white px-3 py-1 rounded text-sm">匯出 Excel</a>
            </>
          )}
        </div>
      </div>

      {!report ? (
        <div className="text-gray-400">請選擇薪資期間</div>
      ) : (
        <>
          <div className="grid grid-cols-4 gap-4 mb-4">
            <div className="bg-white p-4 rounded shadow text-center">
              <div className="text-sm text-gray-500">員工人數</div>
              <div className="text-xl font-bold">{report.grandTotal.employeeCount}</div>
            </div>
            <div className="bg-white p-4 rounded shadow text-center">
              <div className="text-sm text-gray-500">應稅合計</div>
              <div className="text-xl font-bold">${fmt(report.grandTotal.totalGrossPay)}</div>
            </div>
            <div className="bg-white p-4 rounded shadow text-center">
              <div className="text-sm text-gray-500">實發合計</div>
              <div className="text-xl font-bold text-green-600">${fmt(report.grandTotal.totalNetPay)}</div>
            </div>
            <div className="bg-white p-4 rounded shadow text-center">
              <div className="text-sm text-gray-500">雇主總成本</div>
              <div className="text-xl font-bold text-blue-600">${fmt(report.grandTotal.totalEmployerCost)}</div>
            </div>
          </div>

          <div className="space-y-4">
            {report.departments.map((dept, i) => (
              <div key={i} className="bg-white rounded shadow">
                <div className="bg-gray-50 px-4 py-2 font-semibold text-sm flex justify-between">
                  <span>{dept.departmentName}</span>
                  <span className="text-gray-500">{dept.employeeCount} 人</span>
                </div>
                <div className="grid grid-cols-4 gap-2 p-4 text-sm">
                  <div>本薪：<span className="font-semibold">${fmt(dept.totalBaseSalary)}</span></div>
                  <div>津貼：${fmt(dept.totalAllowances)}</div>
                  <div>加班費：${fmt(dept.totalOvertimePay)}</div>
                  <div>應稅合計：<span className="font-semibold">${fmt(dept.totalGrossPay)}</span></div>
                  <div>扣項合計：<span className="text-red-600">${fmt(dept.totalDeductions)}</span></div>
                  <div>實發：<span className="text-green-600 font-semibold">${fmt(dept.totalNetPay)}</span></div>
                  <div>雇主成本：<span className="text-blue-600">${fmt(dept.totalEmployerCost)}</span></div>
                </div>
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
}

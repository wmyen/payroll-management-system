import { useState, useEffect } from 'react';
import { reportApi, OvertimeTrendReport } from '../../api/payroll';

const fmt = (n: number) => n.toLocaleString('zh-TW', { minimumFractionDigits: 0, maximumFractionDigits: 0 });

export default function OvertimeTrendReport() {
  const [year, setYear] = useState(new Date().getFullYear());
  const [report, setReport] = useState<OvertimeTrendReport | null>(null);

  useEffect(() => { fetchReport(); }, [year]);

  const fetchReport = async () => {
    const res = await reportApi.overtimeTrend(year);
    setReport(res.data.data);
  };

  const maxPay = report
    ? Math.max(...report.monthlyData.map(m => m.totalOvertimePay), 1)
    : 1;

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-xl font-bold">加班費趨勢分析</h1>
        <input type="number" value={year} onChange={(e) => setYear(parseInt(e.target.value))}
          className="border rounded px-2 py-1 w-24 text-sm" />
      </div>

      {!report || report.monthlyData.every(m => m.totalOvertimePay === 0) ? (
        <div className="text-gray-400">{year} 年查無加班費資料</div>
      ) : (
        <>
          <div className="grid grid-cols-2 gap-4 mb-6">
            <div className="bg-white p-4 rounded shadow text-center">
              <div className="text-sm text-gray-500">全年加班費總計</div>
              <div className="text-xl font-bold text-orange-600">${fmt(report.yearTotal.totalOvertimePay)}</div>
            </div>
            <div className="bg-white p-4 rounded shadow text-center">
              <div className="text-sm text-gray-500">月平均加班費</div>
              <div className="text-xl font-bold">${fmt(report.yearTotal.avgPerMonth)}</div>
            </div>
          </div>

          <div className="bg-white p-4 rounded shadow mb-6">
            <h3 className="font-semibold text-sm text-gray-600 mb-3">月度趨勢（CSS 長條圖）</h3>
            <div className="flex items-end gap-2 h-48">
              {report.monthlyData.map((m) => (
                <div key={m.month} className="flex-1 flex flex-col items-center">
                  <div className="w-full bg-gray-100 rounded-t relative" style={{ height: '160px' }}>
                    <div className="absolute bottom-0 w-full bg-orange-400 rounded-t"
                      style={{ height: `${(m.totalOvertimePay / maxPay) * 100}%` }}>
                      {m.totalOvertimePay > 0 && (
                        <div className="text-xs text-center mt-1">${fmt(m.totalOvertimePay)}</div>
                      )}
                    </div>
                  </div>
                  <div className="text-xs text-gray-500 mt-1">{m.month}月</div>
                </div>
              ))}
            </div>
          </div>

          <div className="bg-white rounded shadow overflow-auto">
            <table className="w-full text-sm">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-3 py-2 text-left">月份</th>
                  <th className="px-3 py-2 text-right">加班費</th>
                  <th className="px-3 py-2 text-right">員工人數</th>
                  <th className="px-3 py-2 text-right">人均加班費</th>
                </tr>
              </thead>
              <tbody>
                {report.monthlyData.filter(m => m.totalOvertimePay > 0).map((m) => (
                  <tr key={m.month} className="border-t hover:bg-gray-50">
                    <td className="px-3 py-2">{m.month}月</td>
                    <td className="px-3 py-2 text-right">${fmt(m.totalOvertimePay)}</td>
                    <td className="px-3 py-2 text-right">{m.employeeCount}</td>
                    <td className="px-3 py-2 text-right">
                      ${fmt(m.employeeCount > 0 ? Math.round(m.totalOvertimePay / m.employeeCount) : 0)}
                    </td>
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

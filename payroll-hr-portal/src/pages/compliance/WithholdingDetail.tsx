import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { complianceApi, type WithholdingDetail as WithholdingDetailData } from '../../api/payroll';

const fmt = (n: number) => n.toLocaleString('zh-TW', { minimumFractionDigits: 0, maximumFractionDigits: 0 });

export default function WithholdingDetailPage() {
  const { id } = useParams();
  const [detail, setDetail] = useState<WithholdingDetailData | null>(null);

  useEffect(() => {
    if (id) fetchDetail();
  }, [id]);

  const fetchDetail = async () => {
    const res = await complianceApi.getWithholding(Number(id));
    setDetail(res.data.data);
  };

  if (!detail) return <div className="text-gray-400">載入中...</div>;

  const { statement, employee } = detail;

  return (
    <div>
      <h1 className="text-xl font-bold mb-4">扣繳憑單明細 — {statement.year} 年度</h1>

      <div className="bg-white p-4 rounded shadow mb-4">
        <h2 className="font-semibold mb-2">員工資訊</h2>
        <div className="grid grid-cols-3 gap-2 text-sm">
          <div>姓名：{employee.name}</div>
          <div>部門：{employee.department?.name || '未指派'}</div>
          <div>計薪月份：{statement.monthCount} 個月</div>
        </div>
        <div className="mt-2 text-sm">
          狀態：<span className={statement.status === 'CONFIRMED' ? 'text-green-600' : 'text-gray-600'}>
            {statement.status === 'CONFIRMED' ? '已確認' : '草稿'}
          </span>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4 mb-4">
        <div className="bg-white p-4 rounded shadow">
          <h3 className="font-semibold mb-2 text-green-700">收入合計</h3>
          <table className="w-full text-sm">
            <tbody>
              <tr className="border-b"><td className="py-1">全年應稅所得</td><td className="text-right">${fmt(statement.totalGross)}</td></tr>
            </tbody>
          </table>
        </div>
        <div className="bg-white p-4 rounded shadow">
          <h3 className="font-semibold mb-2 text-red-700">扣除合計</h3>
          <table className="w-full text-sm">
            <tbody>
              <tr className="border-b"><td className="py-1">勞保費</td><td className="text-right">${fmt(statement.totalLaborInsurance)}</td></tr>
              <tr className="border-b"><td className="py-1">健保費</td><td className="text-right">${fmt(statement.totalHealthInsurance)}</td></tr>
              <tr className="border-b"><td className="py-1">所得稅</td><td className="text-right">${fmt(statement.totalIncomeTax)}</td></tr>
              <tr className="font-semibold text-red-600"><td className="py-1">扣項合計</td>
                <td className="text-right">${fmt(statement.totalLaborInsurance + statement.totalHealthInsurance + statement.totalIncomeTax)}</td></tr>
            </tbody>
          </table>
        </div>
      </div>

      <div className="bg-white p-4 rounded shadow mb-4">
        <div className="grid grid-cols-2 gap-4 text-center">
          <div>
            <div className="text-sm text-gray-500">全年實領</div>
            <div className="text-2xl font-bold text-green-600">${fmt(statement.totalNetPay)}</div>
          </div>
          <div>
            <div className="text-sm text-gray-500">全年雇主成本</div>
            <div className="text-2xl font-bold text-blue-600">${fmt(statement.totalEmployerCost)}</div>
          </div>
        </div>
      </div>
    </div>
  );
}

import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { payrollApi, PayrollRecordDetail, PayrollItem } from '../../api/payroll';

const fmt = (n: number) => n.toLocaleString('zh-TW', { minimumFractionDigits: 0, maximumFractionDigits: 0 });

export default function PayrollRecordDetailPage() {
  const { id } = useParams();
  const [detail, setDetail] = useState<PayrollRecordDetail | null>(null);
  const [showItemForm, setShowItemForm] = useState(false);
  const [itemType, setItemType] = useState('EARNING');
  const [itemName, setItemName] = useState('');
  const [itemAmount, setItemAmount] = useState('');

  useEffect(() => {
    if (id) fetchDetail();
  }, [id]);

  const fetchDetail = async () => {
    const res = await payrollApi.getRecord(Number(id));
    setDetail(res.data.data);
  };

  const handleAddItem = async () => {
    if (!itemName || !itemAmount) return;
    await payrollApi.addItem(Number(id), {
      itemType,
      name: itemName,
      amount: Number(itemAmount),
    });
    setShowItemForm(false);
    setItemName('');
    setItemAmount('');
    fetchDetail();
  };

  const handleDeleteItem = async (itemId: number) => {
    if (!confirm('確認刪除此項目？')) return;
    await payrollApi.deleteItem(itemId);
    fetchDetail();
  };

  if (!detail) return <div className="text-gray-400">載入中...</div>;

  const { record, employee, items } = detail;

  return (
    <div>
      <h1 className="text-xl font-bold mb-4">薪資單明細</h1>

      {/* Employee info */}
      <div className="bg-white p-4 rounded shadow mb-4">
        <h2 className="font-semibold mb-2">員工資訊</h2>
        <div className="grid grid-cols-3 gap-2 text-sm">
          <div>姓名：{employee.name}</div>
          <div>部門：{employee.department?.name || '未指派'}</div>
          <div>狀態：<span className={record.status === 'CONFIRMED' ? 'text-green-600' : 'text-gray-600'}>
            {record.status === 'CONFIRMED' ? '已確認' : '草稿'}</span></div>
        </div>
      </div>

      {/* Earnings & Deductions */}
      <div className="grid grid-cols-2 gap-4 mb-4">
        <div className="bg-white p-4 rounded shadow">
          <h3 className="font-semibold mb-2 text-green-700">收入項</h3>
          <table className="w-full text-sm">
            <tbody>
              <tr className="border-b"><td className="py-1">本薪</td><td className="text-right">${fmt(record.baseSalary)}</td></tr>
              <tr className="border-b"><td className="py-1">津貼合計</td><td className="text-right">${fmt(record.totalAllowances)}</td></tr>
              <tr className="border-b"><td className="py-1">加班費</td><td className="text-right">${fmt(record.overtimePay)}</td></tr>
              <tr className="border-b"><td className="py-1">其他收入</td><td className="text-right">${fmt(record.otherEarnings)}</td></tr>
              {items.filter(i => i.itemType === 'EARNING').map(item => (
                <tr key={item.id} className="border-b">
                  <td className="py-1">{item.name}</td>
                  <td className="text-right">${fmt(item.amount)}
                    {record.status !== 'CONFIRMED' && (
                      <button onClick={() => handleDeleteItem(item.id)} className="ml-2 text-red-400 text-xs">x</button>
                    )}
                  </td>
                </tr>
              ))}
              <tr className="font-semibold"><td className="py-1">應稅合計</td><td className="text-right">${fmt(record.grossPay)}</td></tr>
            </tbody>
          </table>
        </div>

        <div className="bg-white p-4 rounded shadow">
          <h3 className="font-semibold mb-2 text-red-700">扣除項</h3>
          <table className="w-full text-sm">
            <tbody>
              <tr className="border-b"><td className="py-1">勞保費</td><td className="text-right">${fmt(record.laborInsurance)}</td></tr>
              <tr className="border-b"><td className="py-1">健保費</td><td className="text-right">${fmt(record.healthInsurance)}</td></tr>
              <tr className="border-b"><td className="py-1">所得稅</td><td className="text-right">${fmt(record.incomeTax)}</td></tr>
              <tr className="border-b"><td className="py-1">請假扣薪</td><td className="text-right">${fmt(record.leaveDeduction)}</td></tr>
              <tr className="border-b"><td className="py-1">其他扣項</td><td className="text-right">${fmt(record.otherDeductions)}</td></tr>
              {items.filter(i => i.itemType === 'DEDUCTION').map(item => (
                <tr key={item.id} className="border-b">
                  <td className="py-1">{item.name}</td>
                  <td className="text-right">${fmt(item.amount)}
                    {record.status !== 'CONFIRMED' && (
                      <button onClick={() => handleDeleteItem(item.id)} className="ml-2 text-red-400 text-xs">x</button>
                    )}
                  </td>
                </tr>
              ))}
              <tr className="font-semibold text-red-600"><td className="py-1">扣項合計</td><td className="text-right">${fmt(record.totalDeductions)}</td></tr>
            </tbody>
          </table>
        </div>
      </div>

      {/* Result */}
      <div className="bg-white p-4 rounded shadow mb-4">
        <div className="grid grid-cols-3 gap-4 text-center">
          <div>
            <div className="text-sm text-gray-500">實領金額</div>
            <div className="text-2xl font-bold text-green-600">${fmt(record.netPay)}</div>
          </div>
          <div>
            <div className="text-sm text-gray-500">雇主勞退</div>
            <div className="text-lg">${fmt(record.employerPension)}</div>
          </div>
          <div>
            <div className="text-sm text-gray-500">雇主總成本</div>
            <div className="text-lg font-bold text-blue-600">${fmt(record.totalEmployerCost)}</div>
          </div>
        </div>
      </div>

      {/* Custom items */}
      {record.status !== 'CONFIRMED' && (
        <div className="mb-4">
          <button onClick={() => setShowItemForm(!showItemForm)}
            className="bg-blue-600 text-white px-4 py-2 rounded text-sm">
            新增自訂項目
          </button>
          {showItemForm && (
            <div className="bg-white p-4 rounded shadow mt-2 flex gap-3 items-end">
              <div>
                <label className="block text-xs text-gray-500">類型</label>
                <select value={itemType} onChange={(e) => setItemType(e.target.value)} className="border rounded px-2 py-1">
                  <option value="EARNING">收入</option>
                  <option value="DEDUCTION">扣項</option>
                </select>
              </div>
              <div>
                <label className="block text-xs text-gray-500">名稱</label>
                <input value={itemName} onChange={(e) => setItemName(e.target.value)}
                  className="border rounded px-2 py-1" placeholder="項目名稱" />
              </div>
              <div>
                <label className="block text-xs text-gray-500">金額</label>
                <input type="number" value={itemAmount} onChange={(e) => setItemAmount(e.target.value)}
                  className="border rounded px-2 py-1 w-28" />
              </div>
              <button onClick={handleAddItem} className="bg-green-600 text-white px-3 py-1 rounded">確認</button>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

import { useState, useEffect } from 'react';
import { payrollApi, type TaxBracket } from '../../api/payroll';

const fmt = (n: number) => n.toLocaleString('zh-TW', { minimumFractionDigits: 0, maximumFractionDigits: 0 });
const pct = (n: number) => (n * 100).toFixed(1) + '%';

export default function TaxBracketPage() {
  const [year, setYear] = useState(new Date().getFullYear());
  const [brackets, setBrackets] = useState<TaxBracket[]>([]);
  const [editing, setEditing] = useState(false);
  const [editData, setEditData] = useState<{ bracketStart: number; bracketEnd: number | null; rate: number; quickDeduction: number }[]>([]);

  useEffect(() => { fetchBrackets(); }, [year]);

  const fetchBrackets = async () => {
    const res = await payrollApi.getTaxBrackets(year);
    setBrackets(res.data.data);
    setEditing(false);
  };

  const startEdit = () => {
    if (brackets.length === 0) {
      setEditData([
        { bracketStart: 0, bracketEnd: 74000, rate: 0.05, quickDeduction: 0 },
        { bracketStart: 74000, bracketEnd: 154000, rate: 0.12, quickDeduction: 5170 },
        { bracketStart: 154000, bracketEnd: 264000, rate: 0.20, quickDeduction: 17500 },
        { bracketStart: 264000, bracketEnd: 444000, rate: 0.30, quickDeduction: 43900 },
        { bracketStart: 444000, bracketEnd: null, rate: 0.40, quickDeduction: 88300 },
      ]);
    } else {
      setEditData(brackets.map(b => ({
        bracketStart: b.bracketStart,
        bracketEnd: b.bracketEnd,
        rate: b.rate,
        quickDeduction: b.quickDeduction,
      })));
    }
    setEditing(true);
  };

  const handleSave = async () => {
    await payrollApi.createTaxBrackets({
      year,
      brackets: editData.map(d => ({ ...d, bracketEnd: d.bracketEnd ?? undefined })),
    });
    fetchBrackets();
  };

  const addBracket = () => {
    setEditData([...editData, { bracketStart: 0, bracketEnd: null, rate: 0.05, quickDeduction: 0 }]);
  };

  const removeBracket = (idx: number) => {
    setEditData(editData.filter((_, i) => i !== idx));
  };

  const updateBracket = (idx: number, field: string, value: number | null) => {
    const updated = [...editData];
    updated[idx] = { ...updated[idx], [field]: value };
    setEditData(updated);
  };

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-xl font-bold">所得稅級距管理</h1>
        <div className="flex gap-2 items-center">
          <input type="number" value={year} onChange={(e) => setYear(parseInt(e.target.value))}
            className="border rounded px-2 py-1 w-24" />
          {!editing ? (
            <button onClick={startEdit} className="bg-blue-600 text-white px-4 py-2 rounded text-sm">
              {brackets.length === 0 ? '建立稅率表' : '編輯'}
            </button>
          ) : (
            <>
              <button onClick={handleSave} className="bg-green-600 text-white px-4 py-2 rounded text-sm">儲存</button>
              <button onClick={() => setEditing(false)} className="bg-gray-300 text-gray-700 px-4 py-2 rounded text-sm">取消</button>
            </>
          )}
        </div>
      </div>

      {editing ? (
        <div className="bg-white rounded shadow overflow-auto">
          <table className="w-full text-sm">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-3 py-2 text-left">級距起</th>
                <th className="px-3 py-2 text-left">級距迄</th>
                <th className="px-3 py-2 text-left">稅率</th>
                <th className="px-3 py-2 text-left">累進差額</th>
                <th className="px-3 py-2"></th>
              </tr>
            </thead>
            <tbody>
              {editData.map((b, idx) => (
                <tr key={idx} className="border-t">
                  <td className="px-3 py-2">
                    <input type="number" value={b.bracketStart}
                      onChange={(e) => updateBracket(idx, 'bracketStart', parseFloat(e.target.value))}
                      className="border rounded px-2 py-1 w-28" />
                  </td>
                  <td className="px-3 py-2">
                    <input type="number" value={b.bracketEnd ?? ''}
                      onChange={(e) => updateBracket(idx, 'bracketEnd', e.target.value ? parseFloat(e.target.value) : null)}
                      className="border rounded px-2 py-1 w-28" placeholder="無上限" />
                  </td>
                  <td className="px-3 py-2">
                    <input type="number" step="0.01" value={b.rate}
                      onChange={(e) => updateBracket(idx, 'rate', parseFloat(e.target.value))}
                      className="border rounded px-2 py-1 w-20" />
                  </td>
                  <td className="px-3 py-2">
                    <input type="number" value={b.quickDeduction}
                      onChange={(e) => updateBracket(idx, 'quickDeduction', parseFloat(e.target.value))}
                      className="border rounded px-2 py-1 w-28" />
                  </td>
                  <td className="px-3 py-2">
                    <button onClick={() => removeBracket(idx)} className="text-red-400 text-xs">刪除</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <div className="p-3 border-t">
            <button onClick={addBracket} className="text-blue-600 text-sm">+ 新增級距</button>
          </div>
        </div>
      ) : (
        <div className="bg-white rounded shadow overflow-auto">
          <table className="w-full text-sm">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-3 py-2 text-left">級距起</th>
                <th className="px-3 py-2 text-left">級距迄</th>
                <th className="px-3 py-2 text-right">稅率</th>
                <th className="px-3 py-2 text-right">累進差額</th>
              </tr>
            </thead>
            <tbody>
              {brackets.map((b) => (
                <tr key={b.id} className="border-t hover:bg-gray-50">
                  <td className="px-3 py-2">${fmt(b.bracketStart)}</td>
                  <td className="px-3 py-2">{b.bracketEnd ? `$${fmt(b.bracketEnd)}` : '無上限'}</td>
                  <td className="px-3 py-2 text-right">{pct(b.rate)}</td>
                  <td className="px-3 py-2 text-right">${fmt(b.quickDeduction)}</td>
                </tr>
              ))}
              {brackets.length === 0 && (
                <tr><td colSpan={4} className="px-3 py-4 text-center text-gray-400">
                  {year} 年尚無稅率資料，點擊「建立稅率表」新增
                </td></tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

import { useState, useEffect } from 'react';
import { holidayApi, type Holiday } from '../../api/holidays';

const statusColors: Record<string, string> = {
  HOLIDAY: 'bg-green-100 text-green-800',
  MAKEUP_WORKDAY: 'bg-yellow-100 text-yellow-800',
};

const statusLabels: Record<string, string> = {
  HOLIDAY: '國定假日',
  MAKEUP_WORKDAY: '補班日',
};

export default function HolidayList() {
  const [year, setYear] = useState(new Date().getFullYear());
  const [holidays, setHolidays] = useState<Holiday[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [editId, setEditId] = useState<number | null>(null);
  const [form, setForm] = useState({ holidayDate: '', name: '', holidayType: 'HOLIDAY', year });

  useEffect(() => { loadHolidays(); }, [year]);

  const loadHolidays = async () => {
    const res = await holidayApi.list(year);
    setHolidays(res.data.data);
  };

  const handleSubmit = async () => {
    if (editId) {
      await holidayApi.update(editId, { ...form, year });
    } else {
      await holidayApi.create({ ...form, year });
    }
    setShowForm(false);
    setEditId(null);
    setForm({ holidayDate: '', name: '', holidayType: 'HOLIDAY', year });
    loadHolidays();
  };

  const handleEdit = (h: Holiday) => {
    setEditId(h.id);
    setForm({ holidayDate: h.holidayDate, name: h.name, holidayType: h.holidayType, year: h.year });
    setShowForm(true);
  };

  const handleDelete = async (id: number) => {
    if (!confirm('確定刪除此假日？')) return;
    await holidayApi.delete(id);
    loadHolidays();
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold">假日管理</h1>
        <div className="flex gap-2 items-center">
          <button onClick={() => { setYear(year - 1); }} className="px-2 py-1 bg-gray-200 rounded">&lt;</button>
          <span className="font-semibold">{year} 年</span>
          <button onClick={() => { setYear(year + 1); }} className="px-2 py-1 bg-gray-200 rounded">&gt;</button>
          <button onClick={() => { setShowForm(true); setEditId(null); setForm({ holidayDate: '', name: '', holidayType: 'HOLIDAY', year }); }}
            className="px-3 py-1 bg-blue-600 text-white rounded">新增</button>
        </div>
      </div>

      {showForm && (
        <div className="bg-gray-50 p-4 rounded border space-y-2">
          <div className="grid grid-cols-3 gap-2">
            <input type="date" value={form.holidayDate} onChange={e => setForm({ ...form, holidayDate: e.target.value })}
              className="border rounded px-2 py-1" />
            <input placeholder="假日名稱" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })}
              className="border rounded px-2 py-1" />
            <select value={form.holidayType} onChange={e => setForm({ ...form, holidayType: e.target.value })}
              className="border rounded px-2 py-1">
              <option value="HOLIDAY">國定假日</option>
              <option value="MAKEUP_WORKDAY">補班日</option>
            </select>
          </div>
          <div className="flex gap-2">
            <button onClick={handleSubmit} className="px-3 py-1 bg-blue-600 text-white rounded">
              {editId ? '更新' : '新增'}
            </button>
            <button onClick={() => { setShowForm(false); setEditId(null); }} className="px-3 py-1 bg-gray-300 rounded">取消</button>
          </div>
        </div>
      )}

      <table className="w-full text-sm border">
        <thead className="bg-gray-100">
          <tr>
            <th className="border px-3 py-2 text-left">日期</th>
            <th className="border px-3 py-2 text-left">名稱</th>
            <th className="border px-3 py-2 text-left">類型</th>
            <th className="border px-3 py-2 text-left">操作</th>
          </tr>
        </thead>
        <tbody>
          {holidays.map(h => (
            <tr key={h.id} className="hover:bg-gray-50">
              <td className="border px-3 py-2">{h.holidayDate}</td>
              <td className="border px-3 py-2">{h.name}</td>
              <td className="border px-3 py-2">
                <span className={`px-2 py-0.5 rounded text-xs ${statusColors[h.holidayType]}`}>
                  {statusLabels[h.holidayType]}
                </span>
              </td>
              <td className="border px-3 py-2 space-x-1">
                <button onClick={() => handleEdit(h)} className="text-blue-600">編輯</button>
                <button onClick={() => handleDelete(h.id)} className="text-red-600">刪除</button>
              </td>
            </tr>
          ))}
          {holidays.length === 0 && (
            <tr><td colSpan={4} className="border px-3 py-4 text-center text-gray-500">尚無資料</td></tr>
          )}
        </tbody>
      </table>
    </div>
  );
}

import { useState, useEffect } from 'react';
import { useAuthStore } from '../../stores/authStore';
import { overtimeApi, type OvertimeRecord } from '../../api/overtime';

const fmt = (n: number) => n.toLocaleString('zh-TW', { minimumFractionDigits: 0, maximumFractionDigits: 0 });

const typeLabels: Record<string, string> = { WORKDAY: '平日', REST_DAY: '休息日', HOLIDAY: '國定假日' };
const statusLabels: Record<string, { text: string; cls: string }> = {
  PENDING: { text: '待審核', cls: 'bg-yellow-100 text-yellow-700' },
  APPROVED: { text: '已核准', cls: 'bg-green-100 text-green-700' },
  REJECTED: { text: '已駁回', cls: 'bg-red-100 text-red-700' },
};

export default function OvertimePage() {
  const employeeId = useAuthStore((s) => s.employeeId);
  const [records, setRecords] = useState<OvertimeRecord[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ overtimeDate: '', startTime: '', endTime: '', overtimeType: 'WORKDAY' });

  useEffect(() => {
    if (employeeId) loadRecords();
  }, [employeeId]);

  const loadRecords = async () => {
    if (!employeeId) return;
    const res = await overtimeApi.list({ employeeId, size: 50 });
    setRecords(res.data.data.content);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!employeeId) return;
    await overtimeApi.create({
      employeeId,
      overtimeDate: form.overtimeDate,
      startTime: form.startTime,
      endTime: form.endTime,
      overtimeType: form.overtimeType,
    });
    setShowForm(false);
    setForm({ overtimeDate: '', startTime: '', endTime: '', overtimeType: 'WORKDAY' });
    loadRecords();
  };

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-xl font-bold">加班管理</h1>
        <button onClick={() => setShowForm(!showForm)}
          className="bg-teal-600 text-white px-3 py-1 rounded text-sm">
          {showForm ? '取消' : '新增加班'}
        </button>
      </div>

      {showForm && (
        <form onSubmit={handleSubmit} className="bg-white p-4 rounded shadow mb-4 grid grid-cols-2 gap-3">
          <div>
            <label className="block text-sm mb-1">加班日期</label>
            <input type="date" value={form.overtimeDate} required
              onChange={(e) => setForm({ ...form, overtimeDate: e.target.value })}
              className="w-full border rounded px-2 py-1 text-sm" />
          </div>
          <div>
            <label className="block text-sm mb-1">加班類型</label>
            <select value={form.overtimeType}
              onChange={(e) => setForm({ ...form, overtimeType: e.target.value })}
              className="w-full border rounded px-2 py-1 text-sm">
              {Object.entries(typeLabels).map(([k, v]) => (
                <option key={k} value={k}>{v}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-sm mb-1">開始時間</label>
            <input type="time" value={form.startTime} required
              onChange={(e) => setForm({ ...form, startTime: e.target.value })}
              className="w-full border rounded px-2 py-1 text-sm" />
          </div>
          <div>
            <label className="block text-sm mb-1">結束時間</label>
            <input type="time" value={form.endTime} required
              onChange={(e) => setForm({ ...form, endTime: e.target.value })}
              className="w-full border rounded px-2 py-1 text-sm" />
          </div>
          <div className="col-span-2 flex justify-end">
            <button type="submit" className="bg-teal-600 text-white px-4 py-1.5 rounded text-sm">
              提交申請
            </button>
          </div>
        </form>
      )}

      <div className="bg-white rounded shadow overflow-auto">
        <table className="w-full text-sm">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-3 py-2 text-left">日期</th>
              <th className="px-3 py-2 text-left">類型</th>
              <th className="px-3 py-2 text-left">起迄時間</th>
              <th className="px-3 py-2 text-right">時數</th>
              <th className="px-3 py-2 text-right">加班費</th>
              <th className="px-3 py-2 text-center">狀態</th>
            </tr>
          </thead>
          <tbody>
            {records.map((r) => {
              const s = statusLabels[r.status] ?? { text: r.status, cls: '' };
              return (
                <tr key={r.id} className="border-t hover:bg-gray-50">
                  <td className="px-3 py-2">{r.overtimeDate}</td>
                  <td className="px-3 py-2">{typeLabels[r.overtimeType] ?? r.overtimeType}</td>
                  <td className="px-3 py-2">{r.startTime} - {r.endTime}</td>
                  <td className="px-3 py-2 text-right">{r.hours}</td>
                  <td className="px-3 py-2 text-right">{r.overtimePay != null ? `$${fmt(r.overtimePay)}` : '—'}</td>
                  <td className="px-3 py-2 text-center">
                    <span className={`text-xs px-2 py-0.5 rounded ${s.cls}`}>{s.text}</span>
                  </td>
                </tr>
              );
            })}
            {records.length === 0 && (
              <tr><td colSpan={6} className="px-3 py-4 text-center text-gray-400">尚無加班紀錄</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

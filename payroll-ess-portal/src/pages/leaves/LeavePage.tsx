import { useState, useEffect } from 'react';
import { useAuthStore } from '../../stores/authStore';
import { leaveApi, type LeaveRequest, type LeaveBalance } from '../../api/leaves';

const fmt = (n: number) => n.toLocaleString('zh-TW', { minimumFractionDigits: 0, maximumFractionDigits: 0 });

const leaveTypeLabels: Record<string, string> = {
  ANNUAL: '特休', SICK: '病假', PERSONAL: '事假', BEREAVEMENT: '喪假',
  MATERNITY: '產假', PATERNITY: '陪產假', MARRIAGE: '婚假',
};

const statusLabels: Record<string, { text: string; cls: string }> = {
  PENDING: { text: '待審核', cls: 'bg-yellow-100 text-yellow-700' },
  APPROVED: { text: '已核准', cls: 'bg-green-100 text-green-700' },
  REJECTED: { text: '已駁回', cls: 'bg-red-100 text-red-700' },
  CANCELLED: { text: '已取消', cls: 'bg-gray-100 text-gray-500' },
};

export default function LeavePage() {
  const employeeId = useAuthStore((s) => s.employeeId);
  const [leaves, setLeaves] = useState<LeaveRequest[]>([]);
  const [balances, setBalances] = useState<LeaveBalance[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ leaveType: 'ANNUAL', startDate: '', endDate: '', daysCount: 1, reason: '' });

  useEffect(() => {
    if (employeeId) {
      loadData();
    }
  }, [employeeId]);

  const loadData = async () => {
    if (!employeeId) return;
    const [leavesRes, balancesRes] = await Promise.all([
      leaveApi.list({ employeeId, size: 50 }),
      leaveApi.getBalances(employeeId, new Date().getFullYear()),
    ]);
    setLeaves(leavesRes.data.data.content);
    setBalances(balancesRes.data.data);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!employeeId) return;
    await leaveApi.create({
      employeeId, leaveType: form.leaveType,
      startDate: form.startDate, endDate: form.endDate,
      daysCount: form.daysCount, reason: form.reason || undefined,
    });
    setShowForm(false);
    setForm({ leaveType: 'ANNUAL', startDate: '', endDate: '', daysCount: 1, reason: '' });
    loadData();
  };

  const handleCancel = async (id: number) => {
    await leaveApi.cancel(id);
    loadData();
  };

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-xl font-bold">請假管理</h1>
        <button onClick={() => setShowForm(!showForm)}
          className="bg-teal-600 text-white px-3 py-1 rounded text-sm">
          {showForm ? '取消' : '新增請假'}
        </button>
      </div>

      {showForm && (
        <form onSubmit={handleSubmit} className="bg-white p-4 rounded shadow mb-4 grid grid-cols-3 gap-3">
          <div>
            <label className="block text-sm mb-1">假別</label>
            <select value={form.leaveType} onChange={(e) => setForm({ ...form, leaveType: e.target.value })}
              className="w-full border rounded px-2 py-1 text-sm">
              {Object.entries(leaveTypeLabels).map(([k, v]) => (
                <option key={k} value={k}>{v}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-sm mb-1">開始日期</label>
            <input type="date" value={form.startDate} required
              onChange={(e) => setForm({ ...form, startDate: e.target.value })}
              className="w-full border rounded px-2 py-1 text-sm" />
          </div>
          <div>
            <label className="block text-sm mb-1">結束日期</label>
            <input type="date" value={form.endDate} required
              onChange={(e) => setForm({ ...form, endDate: e.target.value })}
              className="w-full border rounded px-2 py-1 text-sm" />
          </div>
          <div>
            <label className="block text-sm mb-1">天數</label>
            <input type="number" value={form.daysCount} min={0.5} step={0.5}
              onChange={(e) => setForm({ ...form, daysCount: parseFloat(e.target.value) })}
              className="w-full border rounded px-2 py-1 text-sm" />
          </div>
          <div className="col-span-2">
            <label className="block text-sm mb-1">事由</label>
            <input type="text" value={form.reason}
              onChange={(e) => setForm({ ...form, reason: e.target.value })}
              className="w-full border rounded px-2 py-1 text-sm" />
          </div>
          <div className="col-span-3 flex justify-end">
            <button type="submit" className="bg-teal-600 text-white px-4 py-1.5 rounded text-sm">
              提交申請
            </button>
          </div>
        </form>
      )}

      {balances.length > 0 && (
        <div className="bg-white p-4 rounded shadow mb-4">
          <h3 className="font-semibold text-sm text-gray-600 mb-2">假別餘額（{new Date().getFullYear()} 年）</h3>
          <div className="flex gap-4">
            {balances.map((b) => (
              <div key={b.id} className="text-center">
                <div className="text-xs text-gray-500">{leaveTypeLabels[b.leaveType] ?? b.leaveType}</div>
                <div className="font-semibold">{fmt(b.totalDays - b.usedDays)} 天</div>
                <div className="text-xs text-gray-400">({fmt(b.usedDays)}/{fmt(b.totalDays)})</div>
              </div>
            ))}
          </div>
        </div>
      )}

      <div className="bg-white rounded shadow overflow-auto">
        <table className="w-full text-sm">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-3 py-2 text-left">假別</th>
              <th className="px-3 py-2 text-left">開始</th>
              <th className="px-3 py-2 text-left">結束</th>
              <th className="px-3 py-2 text-right">天數</th>
              <th className="px-3 py-2 text-center">狀態</th>
              <th className="px-3 py-2 text-center">操作</th>
            </tr>
          </thead>
          <tbody>
            {leaves.map((l) => {
              const s = statusLabels[l.status] ?? { text: l.status, cls: '' };
              return (
                <tr key={l.id} className="border-t hover:bg-gray-50">
                  <td className="px-3 py-2">{leaveTypeLabels[l.leaveType] ?? l.leaveType}</td>
                  <td className="px-3 py-2">{l.startDate}</td>
                  <td className="px-3 py-2">{l.endDate}</td>
                  <td className="px-3 py-2 text-right">{l.daysCount}</td>
                  <td className="px-3 py-2 text-center">
                    <span className={`text-xs px-2 py-0.5 rounded ${s.cls}`}>{s.text}</span>
                  </td>
                  <td className="px-3 py-2 text-center">
                    {l.status === 'PENDING' && (
                      <button onClick={() => handleCancel(l.id)}
                        className="text-red-600 hover:underline text-sm">取消</button>
                    )}
                  </td>
                </tr>
              );
            })}
            {leaves.length === 0 && (
              <tr><td colSpan={6} className="px-3 py-4 text-center text-gray-400">尚無請假紀錄</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

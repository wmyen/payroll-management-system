import { useState, useEffect } from 'react';
import { useAuthStore } from '../stores/authStore';
import { essApi, type UserProfile } from '../api/ess';
import { leaveApi, type LeaveBalance } from '../api/leaves';

const fmt = (n: number) => n.toLocaleString('zh-TW', { minimumFractionDigits: 0, maximumFractionDigits: 0 });

export default function Dashboard() {
  const employeeId = useAuthStore((s) => s.employeeId);
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [balances, setBalances] = useState<LeaveBalance[]>([]);

  useEffect(() => {
    essApi.me().then(res => setProfile(res.data.data));
    if (employeeId) {
      leaveApi.getBalances(employeeId, new Date().getFullYear())
        .then(res => setBalances(res.data.data))
        .catch(() => {});
    }
  }, [employeeId]);

  const profileName = profile?.profile?.name ?? '—';

  return (
    <div>
      <h1 className="text-xl font-bold mb-4">歡迎，{profileName}</h1>

      <div className="grid grid-cols-3 gap-4 mb-6">
        <div className="bg-white p-4 rounded shadow">
          <div className="text-sm text-gray-500">部門</div>
          <div className="text-lg font-semibold">{profile?.profile?.department?.name ?? '—'}</div>
        </div>
        <div className="bg-white p-4 rounded shadow">
          <div className="text-sm text-gray-500">職位級別</div>
          <div className="text-lg font-semibold">{profile?.profile?.jobLevel ?? '—'}</div>
        </div>
        <div className="bg-white p-4 rounded shadow">
          <div className="text-sm text-gray-500">到職日</div>
          <div className="text-lg font-semibold">{profile?.profile?.hireDate ?? '—'}</div>
        </div>
      </div>

      <h2 className="text-lg font-semibold mb-2">假別餘額（{new Date().getFullYear()} 年）</h2>
      {balances.length === 0 ? (
        <div className="text-gray-400">尚無假別餘額資料</div>
      ) : (
        <div className="bg-white rounded shadow overflow-auto">
          <table className="w-full text-sm">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-3 py-2 text-left">假別</th>
                <th className="px-3 py-2 text-right">總天數</th>
                <th className="px-3 py-2 text-right">已用天數</th>
                <th className="px-3 py-2 text-right">剩餘天數</th>
              </tr>
            </thead>
            <tbody>
              {balances.map((b) => (
                <tr key={b.id} className="border-t hover:bg-gray-50">
                  <td className="px-3 py-2">{b.leaveType}</td>
                  <td className="px-3 py-2 text-right">{fmt(b.totalDays)}</td>
                  <td className="px-3 py-2 text-right">{fmt(b.usedDays)}</td>
                  <td className="px-3 py-2 text-right">{fmt(b.totalDays - b.usedDays)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

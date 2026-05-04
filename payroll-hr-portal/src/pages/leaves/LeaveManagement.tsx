import { useState, useEffect } from 'react';
import { leaveApi, LeaveRequest, LeaveBalance } from '../../api/leaves';

const leaveTypeLabels: Record<string, string> = {
  ANNUAL: '特休', SICK: '病假', PERSONAL: '事假', MARRIAGE: '婚假',
  BEREAVEMENT: '喪假', MATERNITY: '產假', PATERNITY: '陪產假', OFFICIAL: '公假',
};
const statusLabels: Record<string, string> = {
  PENDING: '待審核', APPROVED: '已核准', REJECTED: '已駁回', CANCELLED: '已取消',
};
const statusColors: Record<string, string> = {
  PENDING: 'bg-yellow-100 text-yellow-800', APPROVED: 'bg-green-100 text-green-800',
  REJECTED: 'bg-red-100 text-red-800', CANCELLED: 'bg-gray-100 text-gray-800',
};

export default function LeaveManagement() {
  const [leaves, setLeaves] = useState<LeaveRequest[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [statusFilter, setStatusFilter] = useState('');
  const [tab, setTab] = useState<'list' | 'balances'>('list');
  const [balances, setBalances] = useState<LeaveBalance[]>([]);
  const [balanceEmpId, setBalanceEmpId] = useState('');
  const [balanceYear, setBalanceYear] = useState(new Date().getFullYear());

  useEffect(() => { loadLeaves(); }, [page, statusFilter]);

  const loadLeaves = async () => {
    const params: Record<string, unknown> = { page, size: 20 };
    if (statusFilter) params.status = statusFilter;
    const res = await leaveApi.list(params);
    setLeaves(res.data.data.content);
    setTotalPages(res.data.data.totalPages);
  };

  const handleApprove = async (id: number) => {
    await leaveApi.approve(id, 1);
    loadLeaves();
  };

  const handleReject = async (id: number) => {
    const reason = prompt('駁回原因：');
    if (!reason) return;
    await leaveApi.reject(id, 1, reason);
    loadLeaves();
  };

  const loadBalances = async () => {
    if (!balanceEmpId) return;
    const res = await leaveApi.getBalances(Number(balanceEmpId), balanceYear);
    setBalances(res.data.data);
  };

  const handleInitBalances = async () => {
    if (!confirm(`確定初始化 ${balanceYear} 年假別餘額？`)) return;
    await leaveApi.initBalances(balanceYear);
    alert('初始化完成');
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold">請假管理</h1>
        <div className="flex gap-2">
          <button onClick={() => setTab('list')} className={`px-3 py-1 rounded ${tab === 'list' ? 'bg-blue-600 text-white' : 'bg-gray-200'}`}>請假記錄</button>
          <button onClick={() => setTab('balances')} className={`px-3 py-1 rounded ${tab === 'balances' ? 'bg-blue-600 text-white' : 'bg-gray-200'}`}>假別餘額</button>
        </div>
      </div>

      {tab === 'list' && (
        <>
          <div className="flex gap-2 items-center">
            <select value={statusFilter} onChange={e => { setStatusFilter(e.target.value); setPage(0); }}
              className="border rounded px-2 py-1 text-sm">
              <option value="">全部狀態</option>
              <option value="PENDING">待審核</option>
              <option value="APPROVED">已核准</option>
              <option value="REJECTED">已駁回</option>
            </select>
          </div>

          <table className="w-full text-sm border">
            <thead className="bg-gray-100">
              <tr>
                <th className="border px-3 py-2 text-left">員工</th>
                <th className="border px-3 py-2 text-left">假別</th>
                <th className="border px-3 py-2 text-left">開始</th>
                <th className="border px-3 py-2 text-left">結束</th>
                <th className="border px-3 py-2 text-left">天數</th>
                <th className="border px-3 py-2 text-left">狀態</th>
                <th className="border px-3 py-2 text-left">操作</th>
              </tr>
            </thead>
            <tbody>
              {leaves.map(l => (
                <tr key={l.id} className="hover:bg-gray-50">
                  <td className="border px-3 py-2">{l.employee?.name}</td>
                  <td className="border px-3 py-2">{leaveTypeLabels[l.leaveType] ?? l.leaveType}</td>
                  <td className="border px-3 py-2">{l.startDate}</td>
                  <td className="border px-3 py-2">{l.endDate}</td>
                  <td className="border px-3 py-2">{l.daysCount}</td>
                  <td className="border px-3 py-2">
                    <span className={`px-2 py-0.5 rounded text-xs ${statusColors[l.status]}`}>
                      {statusLabels[l.status]}
                    </span>
                  </td>
                  <td className="border px-3 py-2 space-x-1">
                    {l.status === 'PENDING' && (
                      <>
                        <button onClick={() => handleApprove(l.id)} className="text-green-600">核准</button>
                        <button onClick={() => handleReject(l.id)} className="text-red-600">駁回</button>
                      </>
                    )}
                  </td>
                </tr>
              ))}
              {leaves.length === 0 && (
                <tr><td colSpan={7} className="border px-3 py-4 text-center text-gray-500">尚無資料</td></tr>
              )}
            </tbody>
          </table>

          {totalPages > 1 && (
            <div className="flex gap-2 justify-center">
              <button disabled={page === 0} onClick={() => setPage(page - 1)} className="px-3 py-1 bg-gray-200 rounded disabled:opacity-50">上一頁</button>
              <span className="py-1">{page + 1} / {totalPages}</span>
              <button disabled={page >= totalPages - 1} onClick={() => setPage(page + 1)} className="px-3 py-1 bg-gray-200 rounded disabled:opacity-50">下一頁</button>
            </div>
          )}
        </>
      )}

      {tab === 'balances' && (
        <>
          <div className="flex gap-2 items-center">
            <input placeholder="員工ID" value={balanceEmpId} onChange={e => setBalanceEmpId(e.target.value)}
              className="border rounded px-2 py-1 text-sm w-24" />
            <input type="number" value={balanceYear} onChange={e => setBalanceYear(Number(e.target.value))}
              className="border rounded px-2 py-1 text-sm w-20" />
            <button onClick={loadBalances} className="px-3 py-1 bg-blue-600 text-white rounded text-sm">查詢</button>
            <button onClick={handleInitBalances} className="px-3 py-1 bg-green-600 text-white rounded text-sm">初始化年度餘額</button>
          </div>

          <table className="w-full text-sm border">
            <thead className="bg-gray-100">
              <tr>
                <th className="border px-3 py-2 text-left">假別</th>
                <th className="border px-3 py-2 text-left">總天數</th>
                <th className="border px-3 py-2 text-left">已用</th>
                <th className="border px-3 py-2 text-left">剩餘</th>
              </tr>
            </thead>
            <tbody>
              {balances.map(b => (
                <tr key={b.id} className="hover:bg-gray-50">
                  <td className="border px-3 py-2">{leaveTypeLabels[b.leaveType] ?? b.leaveType}</td>
                  <td className="border px-3 py-2">{b.totalDays}</td>
                  <td className="border px-3 py-2">{b.usedDays}</td>
                  <td className="border px-3 py-2">{(b.totalDays - b.usedDays).toFixed(1)}</td>
                </tr>
              ))}
              {balances.length === 0 && (
                <tr><td colSpan={4} className="border px-3 py-4 text-center text-gray-500">請輸入員工ID查詢</td></tr>
              )}
            </tbody>
          </table>
        </>
      )}
    </div>
  );
}

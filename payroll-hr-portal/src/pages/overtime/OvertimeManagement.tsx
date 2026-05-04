import { useState, useEffect } from 'react';
import { overtimeApi, OvertimeRecord } from '../../api/overtime';
import { formatMoney } from '../../utils/formatMoney';

const typeLabels: Record<string, string> = { WORKDAY: '工作日', REST_DAY: '休息日', HOLIDAY: '國定假日' };
const statusLabels: Record<string, string> = { PENDING: '待審核', APPROVED: '已核准', REJECTED: '已駁回' };
const statusColors: Record<string, string> = {
  PENDING: 'bg-yellow-100 text-yellow-800', APPROVED: 'bg-green-100 text-green-800',
  REJECTED: 'bg-red-100 text-red-800',
};

export default function OvertimeManagement() {
  const [records, setRecords] = useState<OvertimeRecord[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [filters, setFilters] = useState({ startDate: '', endDate: '' });

  useEffect(() => { loadData(); }, [page]);

  const loadData = async () => {
    const params: Record<string, unknown> = { page, size: 20, ...filters };
    const res = await overtimeApi.list(params);
    setRecords(res.data.data.content);
    setTotalPages(res.data.data.totalPages);
  };

  const handleApprove = async (id: number) => {
    await overtimeApi.approve(id, 1);
    loadData();
  };

  const handleReject = async (id: number) => {
    await overtimeApi.reject(id, 1);
    loadData();
  };

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-bold">加班管理</h1>

      <div className="flex gap-2 items-center">
        <input type="date" value={filters.startDate} onChange={e => setFilters({ ...filters, startDate: e.target.value })}
          className="border rounded px-2 py-1 text-sm" />
        <span>至</span>
        <input type="date" value={filters.endDate} onChange={e => setFilters({ ...filters, endDate: e.target.value })}
          className="border rounded px-2 py-1 text-sm" />
        <button onClick={() => { setPage(0); loadData(); }} className="px-3 py-1 bg-blue-600 text-white rounded text-sm">查詢</button>
      </div>

      <table className="w-full text-sm border">
        <thead className="bg-gray-100">
          <tr>
            <th className="border px-3 py-2 text-left">日期</th>
            <th className="border px-3 py-2 text-left">員工</th>
            <th className="border px-3 py-2 text-left">部門</th>
            <th className="border px-3 py-2 text-left">起迄</th>
            <th className="border px-3 py-2 text-left">時數</th>
            <th className="border px-3 py-2 text-left">類型</th>
            <th className="border px-3 py-2 text-left">加班費</th>
            <th className="border px-3 py-2 text-left">狀態</th>
            <th className="border px-3 py-2 text-left">操作</th>
          </tr>
        </thead>
        <tbody>
          {records.map(r => (
            <tr key={r.id} className="hover:bg-gray-50">
              <td className="border px-3 py-2">{r.overtimeDate}</td>
              <td className="border px-3 py-2">{r.employee?.name}</td>
              <td className="border px-3 py-2">{r.employee?.department?.name}</td>
              <td className="border px-3 py-2">{r.startTime} - {r.endTime}</td>
              <td className="border px-3 py-2">{r.hours}</td>
              <td className="border px-3 py-2">{typeLabels[r.overtimeType]}</td>
              <td className="border px-3 py-2">{r.overtimePay != null ? formatMoney(r.overtimePay) : '-'}</td>
              <td className="border px-3 py-2">
                <span className={`px-2 py-0.5 rounded text-xs ${statusColors[r.status]}`}>
                  {statusLabels[r.status]}
                </span>
              </td>
              <td className="border px-3 py-2 space-x-1">
                {r.status === 'PENDING' && (
                  <>
                    <button onClick={() => handleApprove(r.id)} className="text-green-600">核准</button>
                    <button onClick={() => handleReject(r.id)} className="text-red-600">駁回</button>
                  </>
                )}
              </td>
            </tr>
          ))}
          {records.length === 0 && (
            <tr><td colSpan={9} className="border px-3 py-4 text-center text-gray-500">尚無資料</td></tr>
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
    </div>
  );
}

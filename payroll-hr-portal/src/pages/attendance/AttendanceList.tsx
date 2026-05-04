import { useState, useEffect, useRef } from 'react';
import { attendanceApi, AttendanceRecord } from '../../api/attendance';

const statusLabels: Record<string, string> = {
  NORMAL: '正常', LATE: '遲到', EARLY_LEAVE: '早退',
  ABSENT: '缺勤', DAY_OFF: '休息日', HOLIDAY: '國定假日',
};
const statusColors: Record<string, string> = {
  NORMAL: 'bg-green-100 text-green-800', LATE: 'bg-yellow-100 text-yellow-800',
  EARLY_LEAVE: 'bg-orange-100 text-orange-800', ABSENT: 'bg-red-100 text-red-800',
  DAY_OFF: 'bg-gray-100 text-gray-800', HOLIDAY: 'bg-blue-100 text-blue-800',
};

export default function AttendanceList() {
  const [records, setRecords] = useState<AttendanceRecord[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [filters, setFilters] = useState({ startDate: '', endDate: '', departmentId: '' });
  const fileRef = useRef<HTMLInputElement>(null);

  useEffect(() => { loadData(); }, [page]);

  const loadData = async () => {
    const res = await attendanceApi.search({
      ...Object.fromEntries(Object.entries(filters).filter(([, v]) => v)),
      page, size: 20,
    });
    setRecords(res.data.data.content);
    setTotalPages(res.data.data.totalPages);
  };

  const handleImport = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    await attendanceApi.importCsv(file);
    loadData();
    if (fileRef.current) fileRef.current.value = '';
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold">出勤記錄</h1>
        <div className="flex gap-2 items-center">
          <input type="file" ref={fileRef} accept=".csv" onChange={handleImport} className="text-sm" />
          <span className="text-xs text-gray-500">CSV 格式：員工ID,日期,上班時間,下班時間</span>
        </div>
      </div>

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
            <th className="border px-3 py-2 text-left">上班</th>
            <th className="border px-3 py-2 text-left">下班</th>
            <th className="border px-3 py-2 text-left">工時</th>
            <th className="border px-3 py-2 text-left">狀態</th>
          </tr>
        </thead>
        <tbody>
          {records.map(r => (
            <tr key={r.id} className="hover:bg-gray-50">
              <td className="border px-3 py-2">{r.recordDate}</td>
              <td className="border px-3 py-2">{r.employee?.name}</td>
              <td className="border px-3 py-2">{r.employee?.department?.name}</td>
              <td className="border px-3 py-2">{r.clockIn ?? '-'}</td>
              <td className="border px-3 py-2">{r.clockOut ?? '-'}</td>
              <td className="border px-3 py-2">{r.workHours ?? '-'}</td>
              <td className="border px-3 py-2">
                <span className={`px-2 py-0.5 rounded text-xs ${statusColors[r.status]}`}>
                  {statusLabels[r.status]}
                </span>
              </td>
            </tr>
          ))}
          {records.length === 0 && (
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
    </div>
  );
}

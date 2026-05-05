import { useState, useEffect } from 'react';
import { complianceApi, type InsuranceRate } from '../../api/payroll';

const pct = (n: number) => (n * 100).toFixed(2) + '%';

export default function InsuranceRatePage() {
  const [rates, setRates] = useState<InsuranceRate[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({
    effectiveDate: '',
    description: '',
    laborRate: 0.11,
    employmentInsuranceRate: 0.01,
    occupationalRate: 0.002,
    employeeLaborShare: 0.20,
    employerLaborShare: 0.70,
    healthRate: 0.0517,
    healthEmployeeShare: 0.30,
    healthEmployerShare: 0.60,
    pensionRate: 0.06,
  });

  useEffect(() => { fetchRates(); }, []);

  const fetchRates = async () => {
    const res = await complianceApi.listInsuranceRates();
    setRates(res.data.data);
  };

  const handleCreate = async () => {
    if (!form.effectiveDate) return;
    await complianceApi.createInsuranceRate(form);
    setShowForm(false);
    fetchRates();
  };

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-xl font-bold">勞健保費率管理</h1>
        <button onClick={() => setShowForm(!showForm)}
          className="bg-blue-600 text-white px-4 py-2 rounded text-sm">
          新增費率版本
        </button>
      </div>

      {showForm && (
        <div className="bg-white p-4 rounded shadow mb-4">
          <h3 className="font-semibold mb-3">新增費率版本</h3>
          <div className="grid grid-cols-4 gap-3">
            <div>
              <label className="block text-xs text-gray-500 mb-1">生效日期</label>
              <input type="date" value={form.effectiveDate}
                onChange={(e) => setForm({ ...form, effectiveDate: e.target.value })}
                className="border rounded px-2 py-1 w-full" />
            </div>
            <div>
              <label className="block text-xs text-gray-500 mb-1">版本說明</label>
              <input value={form.description}
                onChange={(e) => setForm({ ...form, description: e.target.value })}
                className="border rounded px-2 py-1 w-full" placeholder="例：2026年費率" />
            </div>
          </div>
          <div className="mt-3">
            <h4 className="text-sm font-semibold text-gray-600 mb-2">勞保費率</h4>
            <div className="grid grid-cols-5 gap-3">
              <div>
                <label className="block text-xs text-gray-500 mb-1">普通事故</label>
                <input type="number" step="0.0001" value={form.laborRate}
                  onChange={(e) => setForm({ ...form, laborRate: parseFloat(e.target.value) })}
                  className="border rounded px-2 py-1 w-full" />
              </div>
              <div>
                <label className="block text-xs text-gray-500 mb-1">就業保險</label>
                <input type="number" step="0.0001" value={form.employmentInsuranceRate}
                  onChange={(e) => setForm({ ...form, employmentInsuranceRate: parseFloat(e.target.value) })}
                  className="border rounded px-2 py-1 w-full" />
              </div>
              <div>
                <label className="block text-xs text-gray-500 mb-1">職災保險</label>
                <input type="number" step="0.0001" value={form.occupationalRate}
                  onChange={(e) => setForm({ ...form, occupationalRate: parseFloat(e.target.value) })}
                  className="border rounded px-2 py-1 w-full" />
              </div>
              <div>
                <label className="block text-xs text-gray-500 mb-1">員工負擔比例</label>
                <input type="number" step="0.0001" value={form.employeeLaborShare}
                  onChange={(e) => setForm({ ...form, employeeLaborShare: parseFloat(e.target.value) })}
                  className="border rounded px-2 py-1 w-full" />
              </div>
              <div>
                <label className="block text-xs text-gray-500 mb-1">雇主負擔比例</label>
                <input type="number" step="0.0001" value={form.employerLaborShare}
                  onChange={(e) => setForm({ ...form, employerLaborShare: parseFloat(e.target.value) })}
                  className="border rounded px-2 py-1 w-full" />
              </div>
            </div>
          </div>
          <div className="mt-3">
            <h4 className="text-sm font-semibold text-gray-600 mb-2">健保費率</h4>
            <div className="grid grid-cols-3 gap-3">
              <div>
                <label className="block text-xs text-gray-500 mb-1">健保費率</label>
                <input type="number" step="0.0001" value={form.healthRate}
                  onChange={(e) => setForm({ ...form, healthRate: parseFloat(e.target.value) })}
                  className="border rounded px-2 py-1 w-full" />
              </div>
              <div>
                <label className="block text-xs text-gray-500 mb-1">員工負擔比例</label>
                <input type="number" step="0.0001" value={form.healthEmployeeShare}
                  onChange={(e) => setForm({ ...form, healthEmployeeShare: parseFloat(e.target.value) })}
                  className="border rounded px-2 py-1 w-full" />
              </div>
              <div>
                <label className="block text-xs text-gray-500 mb-1">雇主負擔比例</label>
                <input type="number" step="0.0001" value={form.healthEmployerShare}
                  onChange={(e) => setForm({ ...form, healthEmployerShare: parseFloat(e.target.value) })}
                  className="border rounded px-2 py-1 w-full" />
              </div>
            </div>
          </div>
          <div className="mt-3">
            <h4 className="text-sm font-semibold text-gray-600 mb-2">勞退</h4>
            <div className="grid grid-cols-3 gap-3">
              <div>
                <label className="block text-xs text-gray-500 mb-1">雇主提繳率</label>
                <input type="number" step="0.0001" value={form.pensionRate}
                  onChange={(e) => setForm({ ...form, pensionRate: parseFloat(e.target.value) })}
                  className="border rounded px-2 py-1 w-full" />
              </div>
            </div>
          </div>
          <div className="mt-4 flex gap-2">
            <button onClick={handleCreate} className="bg-green-600 text-white px-4 py-2 rounded text-sm">確認建立</button>
            <button onClick={() => setShowForm(false)} className="bg-gray-300 text-gray-700 px-4 py-2 rounded text-sm">取消</button>
          </div>
        </div>
      )}

      <div className="bg-white rounded shadow overflow-auto">
        <table className="w-full text-sm">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-3 py-2 text-left">生效日期</th>
              <th className="px-3 py-2 text-left">說明</th>
              <th className="px-3 py-2 text-right">勞保費率</th>
              <th className="px-3 py-2 text-right">員工勞保</th>
              <th className="px-3 py-2 text-right">雇主勞保</th>
              <th className="px-3 py-2 text-right">健保費率</th>
              <th className="px-3 py-2 text-right">員工健保</th>
              <th className="px-3 py-2 text-right">雇主健保</th>
              <th className="px-3 py-2 text-right">勞退</th>
            </tr>
          </thead>
          <tbody>
            {rates.map((r) => (
              <tr key={r.id} className="border-t hover:bg-gray-50">
                <td className="px-3 py-2">{r.effectiveDate}</td>
                <td className="px-3 py-2">{r.description || '-'}</td>
                <td className="px-3 py-2 text-right">{pct(r.laborRate + r.employmentInsuranceRate)}</td>
                <td className="px-3 py-2 text-right">{pct(r.employeeLaborShare)}</td>
                <td className="px-3 py-2 text-right">{pct(r.employerLaborShare)}</td>
                <td className="px-3 py-2 text-right">{pct(r.healthRate)}</td>
                <td className="px-3 py-2 text-right">{pct(r.healthEmployeeShare)}</td>
                <td className="px-3 py-2 text-right">{pct(r.healthEmployerShare)}</td>
                <td className="px-3 py-2 text-right">{pct(r.pensionRate)}</td>
              </tr>
            ))}
            {rates.length === 0 && (
              <tr><td colSpan={9} className="px-3 py-4 text-center text-gray-400">尚無費率資料</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

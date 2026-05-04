import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { employeeApi } from '../../api/employees';
import { departmentApi } from '../../api/departments';
import type { Department } from '../../types';

export default function EmployeeForm() {
  const { id } = useParams();
  const isEdit = !!id;
  const navigate = useNavigate();
  const [departments, setDepartments] = useState<Department[]>([]);
  const [form, setForm] = useState({
    name: '', idNumber: '', bankAccount: '',
    hireDate: '', departmentId: '', contractType: 'REGULAR',
    jobLevel: '', email: '', phone: '',
  });

  useEffect(() => {
    departmentApi.getTree().then(({ data }) => setDepartments(data.data));
    if (isEdit) {
      employeeApi.getById(Number(id)).then(({ data }) => {
        const emp = data.data;
        setForm({
          name: emp.name, idNumber: emp.idNumber, bankAccount: emp.bankAccount ?? '',
          hireDate: emp.hireDate, departmentId: emp.department?.id?.toString() ?? '',
          contractType: emp.contractType, jobLevel: emp.jobLevel ?? '',
          email: emp.email ?? '', phone: emp.phone ?? '',
        });
      });
    }
  }, [id, isEdit]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const payload = {
      ...form,
      departmentId: form.departmentId ? Number(form.departmentId) : null,
    };
    if (isEdit) {
      await employeeApi.update(Number(id), payload);
    } else {
      await employeeApi.create(payload);
    }
    navigate('/employees');
  };

  return (
    <div className="max-w-2xl">
      <h2 className="text-xl font-semibold mb-4">{isEdit ? '編輯員工' : '新增員工'}</h2>
      <form onSubmit={handleSubmit} className="space-y-4 bg-white p-6 rounded shadow">
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium mb-1">姓名 *</label>
            <input type="text" required value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              className="w-full border rounded px-3 py-2" />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">身分證字號 *</label>
            <input type="text" required value={form.idNumber}
              onChange={(e) => setForm({ ...form, idNumber: e.target.value })}
              className="w-full border rounded px-3 py-2" />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">銀行帳號</label>
            <input type="text" value={form.bankAccount}
              onChange={(e) => setForm({ ...form, bankAccount: e.target.value })}
              className="w-full border rounded px-3 py-2" />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">到職日 *</label>
            <input type="date" required value={form.hireDate}
              onChange={(e) => setForm({ ...form, hireDate: e.target.value })}
              className="w-full border rounded px-3 py-2" />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">部門</label>
            <select value={form.departmentId}
              onChange={(e) => setForm({ ...form, departmentId: e.target.value })}
              className="w-full border rounded px-3 py-2">
              <option value="">無</option>
              {departments.map((d) => (
                <option key={d.id} value={d.id}>{d.name}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">合約類型</label>
            <select value={form.contractType}
              onChange={(e) => setForm({ ...form, contractType: e.target.value })}
              className="w-full border rounded px-3 py-2">
              <option value="REGULAR">正職</option>
              <option value="CONTRACT">約聘</option>
              <option value="PART_TIME">兼職</option>
              <option value="INTERN">實習</option>
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">職位級別</label>
            <input type="text" value={form.jobLevel}
              onChange={(e) => setForm({ ...form, jobLevel: e.target.value })}
              className="w-full border rounded px-3 py-2" />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">Email</label>
            <input type="email" value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
              className="w-full border rounded px-3 py-2" />
          </div>
        </div>
        <div className="flex gap-2 pt-4">
          <button type="submit" className="bg-blue-600 text-white px-6 py-2 rounded hover:bg-blue-700">
            {isEdit ? '更新' : '建立'}
          </button>
          <button type="button" onClick={() => navigate('/employees')}
            className="border px-6 py-2 rounded hover:bg-gray-50">
            取消
          </button>
        </div>
      </form>
    </div>
  );
}

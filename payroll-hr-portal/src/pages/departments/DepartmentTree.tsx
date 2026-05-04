import { useEffect, useState } from 'react';
import { departmentApi } from '../../api/departments';
import type { Department } from '../../types';

export default function DepartmentTree() {
  const [departments, setDepartments] = useState<Department[]>([]);
  const [newName, setNewName] = useState('');
  const [parentId, setParentId] = useState('');

  useEffect(() => {
    departmentApi.getTree().then(({ data }) => setDepartments(data.data));
  }, []);

  const handleCreate = async () => {
    if (!newName.trim()) return;
    await departmentApi.create({ name: newName, parentId: parentId ? Number(parentId) : null });
    setNewName('');
    setParentId('');
    const { data } = await departmentApi.getTree();
    setDepartments(data.data);
  };

  const renderTree = (items: Department[], depth = 0) => (
    <ul className={depth > 0 ? 'ml-6 border-l pl-4' : ''}>
      {items.map((dept) => (
        <li key={dept.id} className="py-1">
          <div className="flex items-center gap-2">
            <span className="font-medium">{dept.name}</span>
            <button
              onClick={async () => {
                if (confirm(`確定刪除「${dept.name}」？`)) {
                  await departmentApi.delete(dept.id);
                  const { data } = await departmentApi.getTree();
                  setDepartments(data.data);
                }
              }}
              className="text-red-500 text-xs hover:underline"
            >
              刪除
            </button>
          </div>
          {dept.children?.length > 0 && renderTree(dept.children, depth + 1)}
        </li>
      ))}
    </ul>
  );

  const flatDepartments = (items: Department[]): { id: number; name: string }[] =>
    items.flatMap((d) => [{ id: d.id, name: d.name }, ...flatDepartments(d.children || [])]);

  return (
    <div>
      <h2 className="text-xl font-semibold mb-4">部門管理</h2>

      <div className="flex gap-2 mb-6">
        <input
          type="text"
          placeholder="新部門名稱"
          value={newName}
          onChange={(e) => setNewName(e.target.value)}
          className="border rounded px-3 py-2 w-48"
        />
        <select value={parentId} onChange={(e) => setParentId(e.target.value)}
          className="border rounded px-3 py-2">
          <option value="">頂層部門</option>
          {flatDepartments(departments).map((d) => (
            <option key={d.id} value={d.id}>{d.name}</option>
          ))}
        </select>
        <button onClick={handleCreate}
          className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">
          新增
        </button>
      </div>

      <div className="bg-white p-4 rounded shadow">
        {departments.length > 0 ? renderTree(departments) : <p className="text-gray-500">尚無部門</p>}
      </div>
    </div>
  );
}

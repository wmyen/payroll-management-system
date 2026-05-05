import { useState, useEffect } from 'react';
import { essApi, type UserProfile } from '../../api/ess';

const contractLabels: Record<string, string> = {
  REGULAR: '正職', CONTRACT: '約聘', PART_TIME: '兼職', INTERN: '實習',
};

export default function ProfilePage() {
  const [profile, setProfile] = useState<UserProfile | null>(null);

  useEffect(() => {
    essApi.me().then(res => setProfile(res.data.data));
  }, []);

  if (!profile) return <div className="text-gray-400">載入中...</div>;

  const p = profile.profile;

  return (
    <div>
      <h1 className="text-xl font-bold mb-4">個人資料</h1>
      {!p ? (
        <div className="text-gray-400">尚無關聯的員工資料</div>
      ) : (
        <div className="bg-white p-6 rounded shadow max-w-lg">
          <div className="space-y-3">
            <div className="flex justify-between border-b pb-2">
              <span className="text-gray-500">姓名</span>
              <span className="font-medium">{p.name}</span>
            </div>
            <div className="flex justify-between border-b pb-2">
              <span className="text-gray-500">部門</span>
              <span className="font-medium">{p.department?.name ?? '—'}</span>
            </div>
            <div className="flex justify-between border-b pb-2">
              <span className="text-gray-500">到職日</span>
              <span className="font-medium">{p.hireDate}</span>
            </div>
            <div className="flex justify-between border-b pb-2">
              <span className="text-gray-500">合約類型</span>
              <span className="font-medium">{contractLabels[p.contractType] ?? p.contractType}</span>
            </div>
            <div className="flex justify-between border-b pb-2">
              <span className="text-gray-500">職位級別</span>
              <span className="font-medium">{p.jobLevel ?? '—'}</span>
            </div>
            <div className="flex justify-between border-b pb-2">
              <span className="text-gray-500">Email</span>
              <span className="font-medium">{p.email ?? '—'}</span>
            </div>
            <div className="flex justify-between border-b pb-2">
              <span className="text-gray-500">電話</span>
              <span className="font-medium">{p.phone ?? '—'}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-500">系統角色</span>
              <span className="font-medium">{profile.role}</span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

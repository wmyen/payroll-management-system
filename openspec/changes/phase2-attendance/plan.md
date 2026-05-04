# Phase 2 執行計畫：考勤與工時管理

## 實作順序

### Wave 1（基礎，可平行）
1. **Holiday 模組** — 國定假日/補班日 CRUD，加班模組的費率判斷依賴此資料
2. **Attendance Record 模組** — 出勤記錄管理 + CSV 匯入 + 工時自動計算

### Wave 2（業務邏輯）
3. **Leave 模組** — 請假申請 + 假別餘額追蹤 + 核准/駁回流程
4. **Overtime 模組** — 加班申請 + 核准時自動計算加班費（依勞基法 §24）

### Wave 3（前端）
5. **HR Portal 頁面** — 5 個新頁面 + Sidebar 擴充 + API client

### Wave 4（驗證）
6. **整合測試** — 啟動前後端驗證全流程

## 架構遵循
- 後端遵循 Phase 1 的 package 結構：`controller/domain/dto/repository/service`
- Entity 繼承 `BaseEntity`（含 id, version, createdAt, updatedAt, createdBy, updatedBy）
- API 回傳 `ApiResponse<T>` 統一格式
- 前端使用 `api/client.ts` 的 axios instance
- 資料表前綴 `att_`

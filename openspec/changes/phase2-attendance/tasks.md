# Phase 2 任務拆分：考勤與工時管理

## Wave 1：基礎資料層（可平行）
- [T1] 後端 - 國定假日模組 (att_holiday CRUD)
- [T2] 後端 - 出勤記錄模組 (att_record，含 CSV 匯入、工時計算、狀態判定)

## Wave 2：業務邏輯層（依賴 Wave 1 的 Holiday）
- [T3] 後端 - 請假管理模組 (att_leave_request + att_leave_balance)
- [T4] 後端 - 加班管理模組 (att_overtime_record，含加班費計算)

## Wave 3：前端 UI（依賴 Wave 1+2 API）
- [T5] 前端 - HR Portal 考勤管理頁面（5 個頁面 + Sidebar 路由）

## Wave 4：測試與驗證
- [T6] 端到端測試與驗證

# Phase 6: 員工自助服務 (ESS) 設計

## 功能範圍
- 當月及歷史薪資單查詢
- 線上請假申請與查詢
- 加班申請與查詢
- 假別餘額查詢
- 個人資料檢視

## 後端 API

### 新增 EssController（/api/v1/ess）
- `GET /ess/me` — 取得登入者資訊（User + Employee profile），從 JWT 解析 username → User.employeeId → Employee
- `GET /ess/paystubs` — 查詢自己的薪資紀錄（所有期間），回傳 List<{period, record}>
- `GET /ess/paystubs/{recordId}` — 單筆薪資單明細（含自訂項目）

### 複用現有 API（前端帶 employeeId）
- `GET /leaves?employeeId=X` — 請假紀錄
- `POST /leaves` — 提交請假
- `GET /leaves/balances?employeeId=X&year=Y` — 假別餘額
- `GET /overtime?employeeId=X` — 加班紀錄
- `POST /overtime` — 提交加班
- `PUT /leaves/{id}/cancel` — 取消請假

### LoginResponse 擴充
- 新增 `employeeId` 和 `role` 欄位到 LoginResponse，讓前端登入後即知 employeeId

## 前端架構（payroll-ess-portal）

### 技術棧（同 HR portal）
- React 19 + Vite 8 + Tailwind CSS 4 + Zustand 5
- axios + react-router-dom 7

### 頁面
1. **Login** — 登入頁（帳號密碼，同 HR portal）
2. **Dashboard** — 首頁：本月薪資摘要、假別餘額快覽、待審核申請
3. **Paystubs** — 薪資單列表（按期間排序）＋ 點進看明細
4. **PaystubDetail** — 單筆薪資單明細（應發/應扣/實領）
5. **Leaves** — 請假紀錄列表 + 新增請假表單 + 取消功能
6. **LeaveBalance** — 假別餘額一覽
7. **Overtime** — 加班紀錄列表 + 新增加班表單
8. **Profile** — 個人資料檢視（唯讀）

### Layout
- 左側 Sidebar：首頁、薪資單、請假管理、加班管理、個人資料
- 上方 Header：系統名稱 + 登出
- 無 RBAC 管理功能（僅查看自己的資料）

### 路由
- `/login` — 登入
- `/` — Dashboard
- `/paystubs` — 薪資單列表
- `/paystubs/:id` — 薪資單明細
- `/leaves` — 請假管理（含餘額）
- `/overtime` — 加班管理
- `/profile` — 個人資料

## 決策
- ESS 複用 HR portal 的後端 API，只新增 3 個 ESS 專用端點
- 前端完全獨立，不抽取共用套件（遵循 design.md 5.2 共用邏輯原則）
- Vite dev server 設定 base: '/ess/' 配合 Nginx 路由

# 技術設計文件 (Design): Payroll Management System

## 1. 系統架構

### 1.1 整體架構

```
┌─────────────────────────────────────────────────────────┐
│                    Nginx (Reverse Proxy)                 │
│                  SSL termination / Routing               │
├────────────────────┬────────────────────────────────────┤
│  payroll-hr-portal │  payroll-ess-portal                │
│  (React + Vite)    │  (React + Vite)                    │
│  HR 管理後台        │  員工自助服務                        │
├────────────────────┴────────────────────────────────────┤
│              payroll-backend (Spring Boot)               │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐   │
│  │ employee │ │attendance│ │ payroll  │ │compliance│   │
│  │ -module  │ │ -module  │ │ -module  │ │ -module  │   │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘   │
│  ┌──────────┐ ┌─────────────────────────────────────┐   │
│  │reporting │ │          shared-kernel              │   │
│  │ -module  │ │ (BigDecimal utils, auth, audit)     │   │
│  └──────────┘ └─────────────────────────────────────┘   │
├─────────────────────────────────────────────────────────┤
│                    MySQL 8.x                            │
│              (單一 DB，table prefix per module)           │
└─────────────────────────────────────────────────────────┘
```

### 1.2 架構決策
- **模組化單體**：單一 Spring Boot 應用，內部以 Maven 多模組劃分領域邊界
- **模組間通訊**：同一進程內的 Domain Event 或直接 Service 呼叫
- **shared-kernel**：BigDecimal 金額工具、認證授權、審計日誌、共用 Value Object（Money、Period）
- **雙前端共用 API**：透過角色權限區分 HR 與 Employee 的存取範圍

### 1.3 技術棧
| 層級 | 技術 |
|------|------|
| 後端框架 | Spring Boot 3.x (Java 17+) |
| 前端框架 | React + Vite + Tailwind CSS + Zustand |
| 資料庫 | MySQL 8.x |
| DB Migration | Flyway |
| 認證 | Spring Security + JWT |
| 容器 | Docker Compose |
| 反向代理 | Nginx |

## 2. 分階段開發計畫

### Phase 1: 人事主檔管理
- 員工個人檔案 CRUD
- 部門/組織架構管理
- 薪資結構設定（底薪、津貼、扣款項目）
- 勞動合約類型/職位級別
- RBAC 角色權限（ADMIN / HR / EMPLOYEE）

### Phase 2: 考勤與工時管理
- 出勤記錄（打卡資料同步）
- 請假管理（特休/病假/事假/曠職）
- 假別餘額追蹤
- 加班費計算（勞基法：平日 1.33x/1.66x、假日 2x）

### Phase 3: 薪資核算引擎（核心）
- 月薪批次處理（應發/應扣自動計算）
- 勞健保費計算（雇主/員工負擔比例）
- 勞退自提（6% 上限）
- 所得稅預扣（累進稅率）
- 月中到職/離職/調薪比例計算
- 薪資條（Paystub）產生

### Phase 4: 稅務與合規管理
- 扣繳稅率表管理
- 勞健保費率版本控制
- 年度扣繳憑單產出
- 法規變更自動適應機制

### Phase 5: 報表分析與支付
- 銀行自動轉帳檔（Autopay）
- 薪資總表 / 部門分析報表
- 人事成本佔比 / 加班費趨勢分析
- 匯出功能（Excel / PDF）

### Phase 6: 員工自助服務
- 當月及歷史薪資單查詢
- 線上請假/加班申請（簽核流程）
- 假別餘額查詢
- 個人資料維護

### 階段依賴關係
```
Phase 1 ──→ Phase 2 ──→ Phase 3 ──→ Phase 4
                                │         │
                                ▼         ▼
                            Phase 5    Phase 6
```
- Phase 1 是基礎，必須先完成
- Phase 6 的薪資單查詢需等 Phase 3，但請假/加班可提前開發

## 3. 資料庫設計

### 3.1 核心實體

**人事主檔 (emp_)**
- `emp_employee` — 員工主檔（id, name, id_number 加密, bank_account 加密, hire_date, leave_date, department_id, contract_type, job_level, status）
- `emp_department` — 部門（id, name, parent_id 樹狀結構）
- `emp_salary_structure` — 薪資結構（employee_id, base_salary, effective_date）
- `emp_allowance` — 津貼項目（salary_structure_id, type, amount）

**考勤 (att_)**
- `att_record` — 出勤記錄（employee_id, record_date, clock_in, clock_out, overtime_hours）
- `att_leave_request` — 請假申請（employee_id, leave_type, start_date, end_date, status, approver_id）
- `att_leave_balance` — 假別餘額（employee_id, leave_type, total_days, used_days）

**薪資核算 (pay_)**
- `pay_payroll_run` — 薪資批次（run_id, period_year, period_month, status, run_date）
- `pay_payroll_detail` — 薪資明細（run_id, employee_id, gross_pay, total_deductions, net_pay）
- `pay_payroll_item` — 薪資項目（detail_id, item_type EARN/DEDUCT, category, amount）

### 3.2 資料安全
- 金額欄位：`DECIMAL(15,2)`，**絕對禁止** FLOAT/DOUBLE
- 敏感欄位加密：身分證字號、銀行帳號使用 AES-256 加密
- 加密金鑰透過環境變數注入
- 審計欄位：所有表統一 `created_at`、`updated_at`、`created_by`、`updated_by`
- 併發控制：樂觀鎖（`version` 欄位）
- 薪資查詢審計日誌

### 3.3 遷移管理
- Flyway 管理 DB migration
- 版本化 SQL 腳本：`payroll-backend/src/main/resources/db/migration/`

## 4. API 設計

### 4.1 統一規範
- 前綴：`/api/v1/`
- 認證：JWT（Access Token 30min, Refresh Token 7 天）
- 回應格式：`{ "code": 200, "data": ..., "message": "success" }`
- 角色授權：`@PreAuthorize` 註解 + Service 層資料範圍過濾

### 4.2 核心端點

**認證**
- `POST /api/v1/auth/login` — 登入取 JWT
- `POST /api/v1/auth/refresh` — 刷新 Token
- `POST /api/v1/auth/logout` — 登出

**人事主檔 (Phase 1)**
- `GET /api/v1/employees` — 員工列表（分頁/篩選）
- `POST /api/v1/employees` — 新增員工
- `GET /api/v1/employees/{id}` — 員工詳情
- `PUT /api/v1/employees/{id}` — 更新員工
- `GET /api/v1/departments` — 部門樹狀結構
- `GET /api/v1/employees/{id}/salary` — 薪資結構

**考勤 (Phase 2)**
- `GET /api/v1/attendance` — 出勤記錄查詢
- `POST /api/v1/attendance/import` — 批次匯入打卡
- `GET /api/v1/leaves` — 請假記錄
- `POST /api/v1/leaves` — 提交請假申請
- `PUT /api/v1/leaves/{id}/approve` — 審核請假
- `GET /api/v1/overtime` — 加班記錄

**薪資核算 (Phase 3)**
- `POST /api/v1/payroll/runs` — 建立薪資批次
- `GET /api/v1/payroll/runs/{id}` — 批次詳情
- `PUT /api/v1/payroll/runs/{id}/confirm` — 確認鎖定
- `GET /api/v1/payroll/runs/{id}/paystubs` — 薪資條列表
- `GET /api/v1/payroll/paystubs/{id}` — 單一薪資條

**稅務合規 (Phase 4)**
- `GET /api/v1/tax/withholding-tables` — 扣繳稅率表
- `PUT /api/v1/tax/withholding-tables` — 更新稅率表
- `GET /api/v1/tax/annual-statements` — 年度扣繳憑單

**報表 (Phase 5)**
- `GET /api/v1/reports/payroll-summary` — 薪資總表
- `GET /api/v1/reports/department-cost` — 部門成本分析
- `GET /api/v1/reports/bank-transfer` — 銀行轉帳檔
- `GET /api/v1/reports/export/{format}` — 匯出（xlsx/pdf）

## 5. 前端架構

### 5.1 雙 Portal 結構

**payroll-hr-portal**（HR 管理後台）
- Pages: Employees, Attendance, Payroll, Compliance, Reports
- Stores: authStore, employeeStore, payrollStore
- 預設路由 `/`

**payroll-ess-portal**（員工自助服務）
- Pages: Paystubs, Leaves, Overtime, Profile
- Stores: authStore, leaveStore, paystubStore
- 路由 `/ess/`

### 5.2 共用邏輯
- Phase 1 不急著抽取共用套件
- 等兩個前端都有一定代碼量後，再抽取 `packages/shared-ui/` 和 `packages/shared-lib/`

### 5.3 金額顯示規範
- 所有財務數字使用共用 formatter
- 格式：`NT$ 1,234,567.00`（千位逗號 + 貨幣符號 + 兩位小數）

## 6. 安全設計

### 6.1 認證授權
- Spring Security + JWT
- 三種角色：ADMIN（系統管理）、HR（HR 人員）、EMPLOYEE（一般員工）
- EMPLOYEE 角色僅能存取自己的資料
- API 層 `@PreAuthorize` + Service 層資料範圍過濾

### 6.2 資料保護
- 敏感欄位 AES-256 加密存儲
- API 回應遮罩：身分證號 `A1234***89`、銀行帳號 `****5678`
- 金鑰透過 `.env` 管理，不進版控
- 薪資查詢記錄審計日誌

## 7. 部署架構

### 7.1 Docker Compose（地端）
- Nginx（:80/:443）— SSL + 路由
- payroll-backend（:8080）— Spring Boot
- payroll-hr-portal（:3000）— HR 前端
- payroll-ess-portal（:3001）— ESS 前端
- MySQL（:3306）— named volume 持久化

### 7.2 Nginx 路由
- `/` → payroll-hr-portal
- `/ess/` → payroll-ess-portal
- `/api/` → payroll-backend:8080

### 7.3 環境配置
- `docker-compose.yml`（正式）
- `docker-compose.dev.yml`（開發覆蓋）
- `.env` 管理 JWT secret、DB 密碼、AES 金鑰

### 7.4 雲端遷移預留
- 配置外部化（Spring Cloud Config ready）
- 無狀態設計（JWT，不上傳檔案到本地磁碟）
- Docker 映像可直接推到 ECR/ACR
- Compose 可轉 Kubernetes manifests

## 8. 風險與緩解

| 風險 | 影響 | 緩解方案 |
|------|------|----------|
| 金額精度誤差 | 薪資計算錯誤 | 全系統強制 BigDecimal，shared-kernel 統一 Money Value Object |
| 勞基法規變更 | 合規風險 | Phase 4 建立費率版本控制機制，可快速更新 |
| 地端單點故障 | 服務中斷 | MySQL 定期備份、Docker 重啟策略、未來可導入 HA |
| 模組邊界模糊 | 技術債累積 | Maven 多模組強制編譯邊界 + Code Review |
| 敏感資料外洩 | 法規與信任風險 | AES-256 加密、API 遮罩、審計日誌、JWT 短效期 |

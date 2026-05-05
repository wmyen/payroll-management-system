# Phase 5: 報表分析與支付 - 技術設計

## 1. 目標

提供 HR 團隊完整的報表分析能力，包含：
- 部門維度的薪資成本分析
- 銀行自動轉帳檔產出（Autopay）
- 加班費趨勢分析
- 匯出功能（Excel）

## 2. 後端設計

### 2.1 新增依賴
- Apache POI (Excel 匯出)

### 2.2 API 端點

| 端點 | 方法 | 說明 |
|------|------|------|
| `/api/v1/reports/payroll-summary` | GET | 薪資總表（依期間，含部門分組） |
| `/api/v1/reports/department-cost` | GET | 部門成本分析（依年度/月份範圍） |
| `/api/v1/reports/overtime-trend` | GET | 加班費趨勢（依年度，按月統計） |
| `/api/v1/reports/bank-transfer` | GET | 銀行轉帳檔（依薪資期間，TXT 格式下載） |
| `/api/v1/reports/export/payroll` | GET | 薪資總表 Excel 匯出 |

### 2.3 ReportController
```
@RestController @RequestMapping("/api/v1/reports")
```

### 2.4 ReportService — 核心查詢邏輯

**payroll-summary**（薪資總表）
- 輸入：periodId
- 聯查 PayrollRecord + Employee + Department
- 回傳：按部門分組的薪資彙總 + 全公司合計
- 回傳結構：
```json
{
  "period": { "year": 2025, "month": 6, ... },
  "departments": [
    {
      "departmentName": "工程部",
      "employeeCount": 5,
      "totalBaseSalary": 250000,
      "totalAllowances": 50000,
      "totalOvertimePay": 30000,
      "totalGrossPay": 330000,
      "totalDeductions": 45000,
      "totalNetPay": 285000,
      "totalEmployerCost": 380000
    }
  ],
  "grandTotal": { /* 全公司合計 */ }
}
```

**department-cost**（部門成本分析）
- 輸入：year, month（可選，不傳則查全年）
- 聚合 PayrollRecord by employee → department
- 回傳：各部門的薪資成本佔比
- 回傳結構：
```json
{
  "year": 2025, "month": null,
  "departments": [
    {
      "departmentName": "工程部",
      "employeeCount": 5,
      "totalNetPay": 285000,
      "totalEmployerCost": 380000,
      "percentage": 35.5
    }
  ],
  "companyTotal": { "totalNetPay": 800000, "totalEmployerCost": 1050000 }
}
```

**overtime-trend**（加班費趨勢）
- 輸入：year
- 從 PayrollRecord 聚合每月加班費
- 回傳：
```json
{
  "year": 2025,
  "monthlyData": [
    { "month": 1, "totalOvertimePay": 150000, "employeeCount": 10 },
    ... { "month": 12, ... }
  ],
  "yearTotal": { "totalOvertimePay": 1800000, "avgPerMonth": 150000 }
}
```

**bank-transfer**（銀行轉帳檔）
- 輸入：periodId
- 聯查 PayrollRecord + Employee（bankAccount）
- 產出 CSV/TXT 格式，欄位：銀行帳號, 員工姓名, 轉帳金額
- 檔名：`bank-transfer-{year}{month}.txt`

**export/payroll**（Excel 匯出）
- 輸入：periodId
- 使用 Apache POI 產生 .xlsx 檔案
- 包含：薪資總表所有欄位 + 部門分組

### 2.5 Repository 新增方法

PayrollRecordRepository:
- `List<PayrollRecord> findByPeriodIdIn(List<Long> periodIds)` — 多期間查詢

### 2.6 資料查詢流程
- ReportService 注入 PayrollRecordRepository, PayrollPeriodRepository, EmployeeRepository, DepartmentRepository
- 查詢時先取得 PayrollRecord 清單，再 batch 查 Employee（含 Department）
- 按 department 分組聚合（Java Stream，避免複雜 JPQL）

## 3. 前端設計

### 3.1 新增頁面

| 頁面 | 路由 | 說明 |
|------|------|------|
| ReportDashboard | `/reports` | 報表首頁，四個入口卡片 |
| PayrollReport | `/reports/payroll` | 薪資總表報表（依期間，含部門分組） |
| DepartmentCostReport | `/reports/department-cost` | 部門成本分析（含圓餅圖佔比） |
| OvertimeTrendReport | `/reports/overtime-trend` | 加班費趨勢（月度長條圖） |
| BankTransferPage | `/reports/bank-transfer` | 銀行轉帳檔產出與下載 |

### 3.2 Sidebar 新增區塊
- 新增「報表分析」區塊（薪資報表、部門成本、加班趨勢、銀行轉帳）

### 3.3 UI 模式
- 報表頁面統一結構：標題 + 篩選條件列 + 匯總卡片 + 資料表格
- 金額格式：共用 fmt() 函數
- 銀行轉帳檔：點擊按鈕觸發下載（blob response）

## 4. 技術決策

| 決策 | 選項 | 原因 |
|------|------|------|
| Excel 庫 | Apache POI | Java 生態標準，功能完整 |
| PDF | 暫不實作 | 複雜度高，Phase 5 先聚焦 Excel |
| 圖表 | 純 CSS 長條圖 | 避免引入重量級 chart 套件，用 CSS bar chart 即可 |
| 銀行轉帳格式 | TXT (CSV) | 台灣銀行業通用格式 |
| 聚合方式 | Java Stream | 避免 JPQL 複雜度，資料量可控 |

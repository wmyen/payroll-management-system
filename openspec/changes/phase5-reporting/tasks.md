# Phase 5: 報表分析與支付 - 任務清單

## T1: 後端 — 新增 Apache POI 依賴
- 在 payroll-backend/pom.xml 加入 Apache POI dependency
- 檔案：payroll-backend/pom.xml

## T2: 後端 — ReportService 薪資總表查詢
- 新增 ReportService.getPayrollSummary(periodId)
- 聯查 PayrollRecord + Employee + Department
- 按部門分組聚合，回傳結構含 departments[] + grandTotal
- 檔案：payroll/.../service/ReportService.java

## T3: 後端 — ReportService 部門成本分析
- 新增 ReportService.getDepartmentCost(year, month?)
- 聚合各部門薪資成本 + 佔比計算
- 檔案：ReportService.java (擴充)

## T4: 後端 — ReportService 加班費趨勢
- 新增 ReportService.getOvertimeTrend(year)
- 按月聚合加班費
- 檔案：ReportService.java (擴充)

## T5: 後端 — ReportService 銀行轉帳檔
- 新增 ReportService.generateBankTransfer(periodId)
- 產出 TXT/CSV 內容（銀行帳號、姓名、金額）
- 檔案：ReportService.java (擴充)

## T6: 後端 — ReportService Excel 匯出
- 新增 ReportService.exportPayrollExcel(periodId)
- 使用 Apache POI 產生 .xlsx
- 檔案：ReportService.java (擴充)

## T7: 後端 — ReportController
- 新增 ReportController (5 個端點)
- 銀行轉帳：回傳 text/plain + Content-Disposition
- Excel 匯出：回傳 application/octet-stream
- 檔案：payroll/.../controller/ReportController.java

## T8: 前端 — API Client 擴充
- 在 payroll.ts 新增 ReportApi 相關 interfaces 和 API 方法
- 檔案：payroll-hr-portal/src/api/payroll.ts

## T9: 前端 — 薪資報表頁面
- PayrollReport 頁面：期間選擇 + 部門分組表格 + 匯出按鈕
- 檔案：payroll-hr-portal/src/pages/reports/PayrollReport.tsx

## T10: 前端 — 部門成本分析頁面
- DepartmentCostReport：年度/月份篩選 + 成本佔比表格 + CSS bar chart
- 檔案：payroll-hr-portal/src/pages/reports/DepartmentCostReport.tsx

## T11: 前端 — 加班費趨勢頁面
- OvertimeTrendReport：年度選擇 + 月度長條圖（CSS）
- 檔案：payroll-hr-portal/src/pages/reports/OvertimeTrendReport.tsx

## T12: 前端 — 銀行轉帳檔頁面
- BankTransferPage：期間選擇 + 預覽表格 + 下載按鈕
- 檔案：payroll-hr-portal/src/pages/reports/BankTransferPage.tsx

## T13: 前端 — Sidebar + App.tsx 路由更新
- Sidebar 新增「報表分析」區塊（4 個連結）
- App.tsx 新增 4 個路由
- 檔案：Sidebar.tsx, App.tsx

## T14: 編譯驗證
- 後端 mvn compile 通過
- 前端 tsc --noEmit 通過

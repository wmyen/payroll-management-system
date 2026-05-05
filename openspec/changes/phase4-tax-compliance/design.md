# 技術設計文件 (Design): Phase 4 - 稅務與合規管理

## 1. 模組概述

Phase 4 建立稅務與合規管理模組，將 Phase 3 中 hardcoded 的勞健保費率改為資料庫版本控制，新增年度扣繳憑單產出功能，並建立費率生效日機制以應對法規變更。

**依賴**：Phase 1 Employee/SalaryStructure, Phase 3 PayrollRecord/PayrollPeriod/TaxBracket

## 2. 現狀分析

### 2.1 已有基礎 (Phase 3)
- TaxBracket：年度所得稅級距 CRUD（pay_tax_bracket），含 bracketStart、bracketEnd、rate、quickDeduction
- PayrollCalculationService：保險費率 hardcoded 為 static final 常數
  - LABOR_RATE=0.11, EMPLOYMENT_INSURANCE_RATE=0.01, OCCUPATIONAL_RATE=0.002
  - TOTAL_EMPLOYEE_LABOR_SHARE=0.20, TOTAL_EMPLOYER_LABOR_SHARE=0.70
  - HEALTH_RATE=0.0517, HEALTH_EMPLOYEE_SHARE=0.30, HEALTH_EMPLOYER_SHARE=0.60
  - PENSION_RATE=0.06
- TaxBracketService：按年度查稅率 + calculateTax()

### 2.2 缺口
- 勞健保費率 hardcoded，無法應對法規調整
- 無年度扣繳憑單（withholding statement）
- 無費率生效日版本控制

## 3. 功能需求

### 3.1 勞健保費率版本控制
- 費率表以生效日（effective_date）管理版本
- 建立新費率版本時指定生效日，計算引擎自動取用對應期間的費率
- HR 可檢視歷史費率版本
- 初始匯入 2025 年現行費率

### 3.2 年度扣繳憑單產出
- 依年度彙整所有員工的薪資紀錄（從 pay_payroll_record 聚合）
- 產出欄位：姓名、身分證號（遮罩）、全年應稅所得、勞保費、健保費、所得稅、實領合計
- 支援批次產出、個別檢視
- 扣繳憑單狀態：DRAFT → CONFIRMED

### 3.3 稅率管理前端
- 所得稅級距管理頁面（CRUD 年度稅率表）
- 勞健保費率管理頁面（版本列表、新增版本）
- 年度扣繳憑單管理頁面

## 4. 資料庫設計

### pay_insurance_rate — 勞健保費率版本
```
id BIGINT PK
effective_date DATE NOT NULL — 生效日期
description VARCHAR(200) — 版本說明（例：「2025 年現行費率」）

-- 勞保費率
labor_rate DECIMAL(5,4) NOT NULL DEFAULT 0.1100 — 普通事故保險費率
employment_insurance_rate DECIMAL(5,4) NOT NULL DEFAULT 0.0100 — 就業保險費率
occupational_rate DECIMAL(5,4) NOT NULL DEFAULT 0.0020 — 職災保險費率
employee_labor_share DECIMAL(5,4) NOT NULL DEFAULT 0.2000 — 員工勞保負擔比例
employer_labor_share DECIMAL(5,4) NOT NULL DEFAULT 0.7000 — 雇主勞保負擔比例

-- 健保費率
health_rate DECIMAL(5,4) NOT NULL DEFAULT 0.0517 — 健保費率
health_employee_share DECIMAL(5,4) NOT NULL DEFAULT 0.3000 — 員工健保負擔比例
health_employer_share DECIMAL(5,4) NOT NULL DEFAULT 0.6000 — 雇主健保負擔比例

-- 勞退
pension_rate DECIMAL(5,4) NOT NULL DEFAULT 0.0600 — 雇主提繳率

-- audit
version BIGINT DEFAULT 0
created_at, updated_at, created_by, updated_by
```

### pay_withholding_statement — 年度扣繳憑單
```
id BIGINT PK
year INT NOT NULL
employee_id BIGINT NOT NULL → emp_employee
total_gross DECIMAL(15,2) NOT NULL DEFAULT 0 — 全年應稅所得
total_labor_insurance DECIMAL(15,2) NOT NULL DEFAULT 0 — 全年勞保費
total_health_insurance DECIMAL(15,2) NOT NULL DEFAULT 0 — 全年健保費
total_income_tax DECIMAL(15,2) NOT NULL DEFAULT 0 — 全年所得稅
total_net_pay DECIMAL(15,2) NOT NULL DEFAULT 0 — 全年實領
total_employer_cost DECIMAL(15,2) NOT NULL DEFAULT 0 — 全年雇主成本
month_count INT NOT NULL DEFAULT 0 — 計薪月份數
status ENUM('DRAFT','CONFIRMED') NOT NULL DEFAULT 'DRAFT'
UNIQUE(year, employee_id)
-- audit
version, created_at, updated_at, created_by, updated_by
```

## 5. API 設計

### 勞健保費率
- GET /api/v1/compliance/insurance-rates — 費率版本列表（依生效日排序）
- POST /api/v1/compliance/insurance-rates — 新增費率版本
- GET /api/v1/compliance/insurance-rates/{id} — 單一版本詳情

### 年度扣繳憑單
- GET /api/v1/compliance/withholding?year=YYYY — 年度憑單列表
- POST /api/v1/compliance/withholding/generate — 批次產出年度憑單（body: { year })
- GET /api/v1/compliance/withholding/{id} — 單一憑單（含員工資訊）
- POST /api/v1/compliance/withholding/{id}/confirm — 確認單筆
- POST /api/v1/compliance/withholding/confirm-all — 批次確認（body: { year })

### 所得稅級距（沿用既有）
- GET /api/v1/payroll/tax-brackets?year — 查詢
- POST /api/v1/payroll/tax-brackets — 批次建立

## 6. 計算邏輯變更

### PayrollCalculationService 改造
- 移除所有 static final 費率常數
- 注入 InsuranceRateService
- calculateEmployeePayroll() 改為：查詢 effective_date ≤ periodEnd 的最新費率版本，動態套用
- 其餘計算邏輯（加班費、請假扣薪、稅）不變

### InsuranceRateService
- findApplicableRate(LocalDate date)：查詢 effective_date ≤ date 的最新一筆
- 提供給 PayrollCalculationService 使用

### WithholdingService
- generateForYear(int year)：聚合該年度所有 CONFIRMED/LOCKED 的 payroll records
- 每位員工一筆 withholdding statement
- 欄位聚合：SUM(base_salary + total_allowances + overtime_pay + other_earnings) 等

## 7. 前端頁面

### Sidebar 新增「合規管理」區塊
- /compliance/insurance-rates — 勞健保費率管理
- /compliance/tax-brackets — 所得稅級距管理
- /compliance/withholding — 年度扣繳憑單

### 勞健保費率頁面
- 版本列表表格（生效日、說明、各項費率、操作）
- 新增版本表單（帶預設值）

### 所得稅級距頁面
- 年度選擇器 + 級距表格
- 批次編輯/儲存

### 年度扣繳憑單頁面
- 年度選擇器 + 批次產出按鈕
- 憑單列表（姓名、全年所得、扣繳稅額、實領）
- 點入可看明細
- 批次確認按鈕

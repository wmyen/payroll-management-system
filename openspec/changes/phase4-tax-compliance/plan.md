# Phase 4: 稅務與合規管理 實作計畫

**Goal:** 將 hardcoded 勞健保費率改為資料庫版本控制，新增年度扣繳憑單產出功能，建立合規管理前端頁面。

---

## Task 1: 資料庫遷移
**Files:**
- Create: `payroll-backend/src/main/resources/db/migration/V6__create_compliance_tables.sql`

- [ ] **Step 1: 建立 pay_insurance_rate 表**
  - 生效日、描述、勞保/健保/勞退各項費率欄位、審計欄位
  - Seed 2025 現行費率一筆（effective_date=2025-01-01）

- [ ] **Step 2: 建立 pay_withholding_statement 表**
  - year、employee_id、各項年度合計金額、status、審計欄位
  - UNIQUE(year, employee_id)

---

## Task 2: 後端 - 勞健保費率管理
**Files:**
- Create: `payroll-backend/src/main/java/com/payroll/payroll/domain/InsuranceRate.java`
- Create: `payroll-backend/src/main/java/com/payroll/payroll/repository/InsuranceRateRepository.java`
- Create: `payroll-backend/src/main/java/com/payroll/payroll/service/InsuranceRateService.java`
- Create: `payroll-backend/src/main/java/com/payroll/payroll/dto/InsuranceRateRequest.java`

- [ ] **Step 1: InsuranceRate entity**
  - 欄位：effectiveDate, description, laborRate, employmentInsuranceRate, occupationalRate, employeeLaborShare, employerLaborShare, healthRate, healthEmployeeShare, healthEmployerShare, pensionRate
  - 繼承 BaseEntity, @Builder

- [ ] **Step 2: InsuranceRateRepository**
  - findTopByEffectiveDateLessThanEqualOrderByEffectiveDateDesc(LocalDate date) — 找適用費率
  - findAllByOrderByEffectiveDateDesc() — 列表

- [ ] **Step 3: InsuranceRateService**
  - getAll() — 全部版本列表
  - getById(id) — 單筆
  - getApplicableRate(LocalDate date) — 查詢生效日 ≤ date 的最新版本
  - create(InsuranceRateRequest) — 新增版本（不允許刪改歷史）

- [ ] **Step 4: InsuranceRateRequest DTO**
  - effectiveDate, description, 各項費率欄位

---

## Task 3: 後端 - 改造 PayrollCalculationService
**Files:**
- Modify: `payroll-backend/src/main/java/com/payroll/payroll/service/PayrollCalculationService.java`

- [ ] **Step 1: 移除 hardcoded 費率常數**
  - 刪除所有 static final BigDecimal 費率常數

- [ ] **Step 2: 注入 InsuranceRateService**
  - calculateEmployeePayroll() 開頭查詢適用費率
  - 以查到的費率取代原本常數進行計算

---

## Task 4: 後端 - 年度扣繳憑單
**Files:**
- Create: `payroll-backend/src/main/java/com/payroll/payroll/domain/WithholdingStatement.java`
- Create: `payroll-backend/src/main/java/com/payroll/payroll/domain/WithholdingStatus.java`
- Create: `payroll-backend/src/main/java/com/payroll/payroll/repository/WithholdingStatementRepository.java`
- Create: `payroll-backend/src/main/java/com/payroll/payroll/service/WithholdingService.java`

- [ ] **Step 1: WithholdingStatus enum**
  - DRAFT, CONFIRMED

- [ ] **Step 2: WithholdingStatement entity**
  - year, employeeId, totalGross, totalLaborInsurance, totalHealthInsurance, totalIncomeTax, totalNetPay, totalEmployerCost, monthCount, status
  - 繼承 BaseEntity, confirm() 方法

- [ ] **Step 3: WithholdingStatementRepository**
  - findByYear(int year) — 年度列表
  - findByYearAndEmployeeId(int year, Long employeeId) — 查重
  - findByYearAndStatus(int year, WithholdingStatus status)

- [ ] **Step 4: WithholdingService**
  - generateForYear(int year)：聚合該年度所有已確認薪資紀錄，每人一筆
  - getByYear(int year) — 列表
  - getById(Long id) — 單筆（含員工資訊）
  - confirm(Long id) — 確認單筆
  - confirmAll(int year) — 批次確認

---

## Task 5: 後端 - 合規管理 Controller
**Files:**
- Create: `payroll-backend/src/main/java/com/payroll/payroll/controller/ComplianceController.java`

- [ ] **Step 1: ComplianceController**
  - GET /api/v1/compliance/insurance-rates — 費率列表
  - POST /api/v1/compliance/insurance-rates — 新增費率
  - GET /api/v1/compliance/withholding — 年度憑單列表（?year=）
  - POST /api/v1/compliance/withholding/generate — 批次產出
  - GET /api/v1/compliance/withholding/{id} — 單筆（含員工資訊）
  - POST /api/v1/compliance/withholding/{id}/confirm — 確認
  - POST /api/v1/compliance/withholding/confirm-all — 批次確認

---

## Task 6: 前端 - API Client
**Files:**
- Modify: `payroll-hr-portal/src/api/payroll.ts`

- [ ] **Step 1: 新增型別與 API 方法**
  - InsuranceRate interface + insuranceRateApi
  - WithholdingStatement interface + withholdingApi

---

## Task 7: 前端 - 勞健保費率管理頁面
**Files:**
- Create: `payroll-hr-portal/src/pages/compliance/InsuranceRatePage.tsx`

- [ ] **Step 1: 費率版本列表**
  - 表格呈現所有版本（生效日、說明、各項費率）
  - 新增版本按鈕 + 表單（帶預設值）

---

## Task 8: 前端 - 所得稅級距管理頁面
**Files:**
- Create: `payroll-hr-portal/src/pages/compliance/TaxBracketPage.tsx`

- [ ] **Step 1: 年度稅率表管理**
  - 年度選擇器
  - 級距表格（起迄金額、稅率、累進差額）
  - 批次儲存按鈕

---

## Task 9: 前端 - 年度扣繳憑單頁面
**Files:**
- Create: `payroll-hr-portal/src/pages/compliance/WithholdingPage.tsx`
- Create: `payroll-hr-portal/src/pages/compliance/WithholdingDetail.tsx`

- [ ] **Step 1: 扣繳憑單列表**
  - 年度選擇器 + 批次產出按鈕
  - 列表表格（員工姓名、全年所得、扣繳稅額、實領、狀態）
  - 批次確認按鈕
  - 點入看明細

- [ ] **Step 2: 扣繳憑單明細**
  - 員工資訊 + 年度各項合計金額

---

## Task 10: 前端 - 路由與導航
**Files:**
- Modify: `payroll-hr-portal/src/App.tsx`
- Modify: `payroll-hr-portal/src/components/layout/Sidebar.tsx`

- [ ] **Step 1: Sidebar 新增「合規管理」區塊**
  - 勞健保費率、所得稅級距、扣繳憑單

- [ ] **Step 2: App.tsx 新增路由**
  - /compliance/insurance-rates
  - /compliance/tax-brackets
  - /compliance/withholding
  - /compliance/withholding/:id

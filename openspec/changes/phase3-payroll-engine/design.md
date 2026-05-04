# 技術設計文件 (Design): Phase 3 - 薪資計算引擎

## 1. 模組概述

Phase 3 建立薪資計算引擎，整合 Phase 1（員工薪資結構）與 Phase 2（加班費、請假扣薪），實現月結薪資自動計算、勞健保費計算、所得稅扣繳、薪資單產出。

**依賴**：Phase 1 Employee/SalaryStructure, Phase 2 OvertimeRecord/LeaveRequest/Holiday

## 2. 計算邏輯

### 2.1 薪資計算流程（每位員工）
1. **本薪** ← SalaryStructure.baseSalary（期間生效日最新一筆）
2. **津貼合計** ← Sum(SalaryStructure.allowances)
3. **加班費** ← Sum(att_overtime_record overtime_pay where period 內已核准)
4. **請假扣薪** ← 計算期間內核准的請假天數 × 日薪
   - 事假：全扣（日薪 = 本薪/30）
   - 病假：半薪（扣日薪 × 0.5）
   - 其他有薪假：不扣
5. **勞保費**（員工）← 月投保薪資 × (11%+1%) × 20% = 投保薪資 × 2.4%
6. **健保費**（員工）← 月投保金額 × 5.17% × 30% = 投保金額 × 1.551%
7. **所得稅** ← (應稅薪資 - 勞保 - 健保) 查級距表
8. **實領** = 本薪 + 津貼 + 加班費 + 其他收入 - 勞保 - 健保 - 所得稅 - 請假扣薪 - 其他扣項

### 2.2 雇主成本
- 雇主勞保 ← 投保薪資 × (11%+1%+0.2%) × 70%
- 雇主健保 ← 投保金額 × 5.17% × 60%
- 雇主勞退 ← 投保薪資 × 6%
- 雇主總成本 = 本薪 + 津貼 + 雇主勞保 + 雇主健保 + 雇主勞退

### 2.3 所得稅月扣繳（年度稅率按月均攤）
| 月應稅淨額 | 稅率 | 累進差額 |
|-----------|------|---------|
| 0 - 74,000 | 5% | 0 |
| 74,001 - 154,000 | 12% | 5,170 |
| 154,001 - 264,000 | 20% | 17,500 |
| 264,001 - 444,000 | 30% | 43,900 |
| 444,001+ | 40% | 88,300 |

## 3. 資料庫設計

### pay_payroll_period — 薪資期間
```
id BIGINT PK
year INT NOT NULL
month INT NOT NULL
start_date DATE NOT NULL
end_date DATE NOT NULL
pay_date DATE NOT NULL
status ENUM('DRAFT','PROCESSING','CONFIRMED','LOCKED') DEFAULT 'DRAFT'
UNIQUE(year, month)
```

### pay_payroll_record — 薪資紀錄
```
id BIGINT PK
period_id BIGINT FK → pay_payroll_period
employee_id BIGINT FK → emp_employee
base_salary, total_allowances, overtime_pay, other_earnings DECIMAL(15,2)
gross_pay DECIMAL(15,2) — 應稅合計
labor_insurance, health_insurance, income_tax DECIMAL(15,2)
leave_deduction, other_deductions, total_deductions DECIMAL(15,2)
net_pay DECIMAL(15,2) — 實領
employer_labor_ins, employer_health_ins, employer_pension DECIMAL(15,2)
total_employer_cost DECIMAL(15,2)
status ENUM('DRAFT','CONFIRMED')
UNIQUE(period_id, employee_id)
```

### pay_payroll_item — 自訂薪資項目
```
id BIGINT PK
payroll_record_id BIGINT FK → pay_payroll_record
item_type ENUM('EARNING','DEDUCTION')
name VARCHAR(100)
amount DECIMAL(15,2)
```

### pay_tax_bracket — 所得稅級距
```
id BIGINT PK
year INT
bracket_start, bracket_end DECIMAL(15,2)
rate DECIMAL(5,4)
quick_deduction DECIMAL(15,2)
```

## 4. API 設計

### 薪資期間
- GET /api/v1/payroll/periods — 期間列表
- POST /api/v1/payroll/periods — 建立期間
- PUT /api/v1/payroll/periods/{id} — 更新
- POST /api/v1/payroll/periods/{id}/calculate — 批次計算
- POST /api/v1/payroll/periods/{id}/confirm — 確認鎖定

### 薪資紀錄
- GET /api/v1/payroll/records — 列表（periodId, departmentId）
- GET /api/v1/payroll/records/{id} — 單筆（含明細）
- PUT /api/v1/payroll/records/{id} — 手動調整
- POST /api/v1/payroll/records/{id}/recalculate — 重算單筆

### 自訂項目
- GET /api/v1/payroll/records/{recordId}/items — 列表
- POST /api/v1/payroll/records/{recordId}/items — 新增
- PUT /api/v1/payroll/items/{id} — 更新
- DELETE /api/v1/payroll/items/{id} — 刪除

### 稅率級距
- GET /api/v1/payroll/tax-brackets?year — 查詢
- POST /api/v1/payroll/tax-brackets — 批次建立

## 5. 前端頁面

- **薪資期間** (/payroll/periods) — 建立/管理計薪期間
- **薪資計算** (/payroll/run) — 選擇期間→批次計算→檢視結果
- **薪資總表** (/payroll/summary) — 依部門/期間彙總
- **薪資單明細** (/payroll/records/:id) — 單一員工薪資單
- Sidebar 新增「薪資管理」區塊

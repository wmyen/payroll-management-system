# 技術設計文件 (Design): Phase 2 - 考勤與工時管理

## 1. 模組概述

Phase 2 建立考勤與工時管理模組，包含出勤記錄、請假管理、假別餘額追蹤、加班管理與加班費計算。所有計算需符合台灣勞動基準法規定。

**依賴 Phase 1**：員工主檔 (Employee)、部門 (Department)、薪資結構 (SalaryStructure)、認證授權 (Auth)。

## 2. 功能需求

### 2.1 出勤記錄管理
- HR 可檢視所有員工的出勤記錄（依日期/部門/員工篩選）
- 支援批次匯入打卡資料（CSV 格式）
- 自動計算實際工作時數（clock_out - clock_in - 午休 1 小時）
- 出勤狀態自動判定：正常、遲到、早退、缺勤

### 2.2 請假管理
- 員工可提交請假申請（HR 代為提交或 ESS 入口）
- 支援半天假（上午/下午）
- 請假簽核流程：待審核 → 核准 / 駁回 / 取消
- HR 可檢視所有請假記錄並篩選

### 2.3 假別餘額追蹤
- 年初自動初始化假別餘額（依到職年資計算特休天數）
- 請假核准時自動扣減餘額
- HR 可手動調整餘額
- 支援查詢歷年度餘額

#### 特休天數（勞基法 §38）
| 年資 | 特休天數 |
|------|----------|
| 6 個月 - 1 年 | 3 天 |
| 1 - 2 年 | 7 天 |
| 3 - 5 年 | 10 天 |
| 5 - 10 年 | 14 天 |
| 10 年以上 | 每年 +1 天，上限 30 天 |

#### 其他假別
| 假別 | 年天數 | 薪資 |
|------|--------|------|
| 事假 | 14 天 | 無薪 |
| 病假 | 30 天 | 半薪 |
| 婚假 | 8 天 | 全薪 |
| 喪假 | 3-8 天 | 全薪（依親等） |
| 產假 | 8 週 | 全薪 |
| 陪產假 | 5 天 | 全薪 |
| 公假 | 依需求 | 全薪 |

### 2.4 加班管理
- 加班申請/記錄（加班日期、起迄時間、加班類型）
- 加班類型：工作日加班、休息日加班、國定假日加班
- 加班費率計算（勞基法 §24）
- 月加班時數上限檢查（46 小時，經同意可至 54 小時）

#### 加班費率
| 類型 | 時數區間 | 費率倍數 |
|------|----------|----------|
| 工作日 | 0-2 小時 | 1.33x |
| 工作日 | 2-4 小時 | 1.66x |
| 休息日 | 0-2 小時 | 1.33x |
| 休息日 | 2-8 小時 | 1.66x |
| 休息日 | 8 小時以上 | 2.66x |
| 國定假日 | 全部 | 2.0x |

時薪 = 月薪 / 30 / 8（即月薪 / 240）

### 2.5 國定假日管理
- 維護年度國定假日與補班日
- 用於判斷加班費率類型
- 支援每年更新

## 3. 資料庫設計

### 3.1 新增資料表（att_ 前綴）

**att_holiday** — 國定假日
```
id BIGINT PK AUTO_INCREMENT
holiday_date DATE NOT NULL
name VARCHAR(100) NOT NULL
holiday_type ENUM('HOLIDAY','MAKEUP_WORKDAY') — 假日或補班日
year INT NOT NULL
UNIQUE(holiday_date)
```

**att_record** — 出勤記錄
```
id BIGINT PK AUTO_INCREMENT
employee_id BIGINT NOT NULL FK → emp_employee
record_date DATE NOT NULL
clock_in TIME
clock_out TIME
work_hours DECIMAL(4,1) — 自動計算
status ENUM('NORMAL','LATE','EARLY_LEAVE','ABSENT','DAY_OFF','HOLIDAY')
remark VARCHAR(200)
version INT DEFAULT 0
created_at, updated_at, created_by, updated_by
UNIQUE(employee_id, record_date)
```

**att_leave_request** — 請假申請
```
id BIGINT PK AUTO_INCREMENT
employee_id BIGINT NOT NULL FK → emp_employee
leave_type ENUM('ANNUAL','SICK','PERSONAL','MARRIAGE','BEREAVEMENT','MATERNITY','PATERNITY','OFFICIAL')
start_date DATE NOT NULL
end_date DATE NOT NULL
start_period ENUM('MORNING','AFTERNOON') — 開始時段（支援半天）
end_period ENUM('MORNING','AFTERNOON') — 結束時段
days_count DECIMAL(4,1) NOT NULL — 請假天數（含半天 = 0.5）
reason VARCHAR(500)
status ENUM('PENDING','APPROVED','REJECTED','CANCELLED') DEFAULT 'PENDING'
approver_id BIGINT FK → auth_user
approved_at DATETIME
rejected_reason VARCHAR(200)
version INT DEFAULT 0
created_at, updated_at, created_by, updated_by
```

**att_leave_balance** — 假別餘額
```
id BIGINT PK AUTO_INCREMENT
employee_id BIGINT NOT NULL FK → emp_employee
leave_type ENUM('ANNUAL','SICK','PERSONAL','MARRIAGE','BEREAVEMENT','MATERNITY','PATERNITY','OFFICIAL')
year INT NOT NULL
total_days DECIMAL(5,1) NOT NULL
used_days DECIMAL(5,1) DEFAULT 0
UNIQUE(employee_id, leave_type, year)
```

**att_overtime_record** — 加班記錄
```
id BIGINT PK AUTO_INCREMENT
employee_id BIGINT NOT NULL FK → emp_employee
overtime_date DATE NOT NULL
start_time TIME NOT NULL
end_time TIME NOT NULL
hours DECIMAL(4,1) NOT NULL
overtime_type ENUM('WORKDAY','REST_DAY','HOLIDAY') NOT NULL
status ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING'
approver_id BIGINT FK → auth_user
overtime_pay DECIMAL(15,2) — 核准後自動計算
version INT DEFAULT 0
created_at, updated_at, created_by, updated_by
```

## 4. API 設計

### 4.1 出勤 API
- `GET /api/v1/attendance` — 出勤記錄查詢（params: employeeId, departmentId, startDate, endDate, page, size）
- `GET /api/v1/attendance/{id}` — 單筆出勤記錄
- `POST /api/v1/attendance/import` — 批次匯入（CSV）

### 4.2 請假 API
- `GET /api/v1/leaves` — 請假記錄列表（params: employeeId, leaveType, status, startDate, endDate, page, size）
- `POST /api/v1/leaves` — 提交請假申請
- `PUT /api/v1/leaves/{id}/approve` — 核准
- `PUT /api/v1/leaves/{id}/reject` — 駁回（body: reason）
- `PUT /api/v1/leaves/{id}/cancel` — 取消
- `GET /api/v1/leaves/balances` — 假別餘額查詢（params: employeeId, year）
- `POST /api/v1/leaves/balances/init` — 初始化年度餘額（params: year）

### 4.3 加班 API
- `GET /api/v1/overtime` — 加班記錄列表（params: employeeId, departmentId, startDate, endDate, page, size）
- `POST /api/v1/overtime` — 提交加班記錄
- `PUT /api/v1/overtime/{id}/approve` — 核准（自動計算加班費）
- `PUT /api/v1/overtime/{id}/reject` — 駁回

### 4.4 國定假日 API
- `GET /api/v1/holidays` — 查詢假日列表（params: year）
- `POST /api/v1/holidays` — 新增假日
- `PUT /api/v1/holidays/{id}` — 更新
- `DELETE /api/v1/holidays/{id}` — 刪除

## 5. 前端頁面

### 5.1 HR Portal 新增頁面
- **出勤記錄** (`/attendance`) — 出勤列表 + 批次匯入
- **請假管理** (`/leaves`) — 請假申請列表 + 核准/駁回
- **加班管理** (`/overtime`) — 加班記錄列表 + 核准
- **假別餘額** (`/leaves/balances`) — 餘額查詢（依員工/年度）
- **假日管理** (`/holidays`) — 國定假日 CRUD

### 5.2 Sidebar 新增項目
出勤管理（含出勤記錄、請假管理、加班管理、假別餘額、假日管理）

## 6. 計算邏輯

### 6.1 工作時數計算
```
work_hours = (clock_out - clock_in) - 1.0 小時（午休）
若 work_hours < 0 則為 0
```

### 6.2 出勤狀態判定
- clock_in > 09:00 → LATE
- clock_out < 18:00 → EARLY_LEAVE
- clock_in IS NULL AND clock_out IS NULL → ABSENT
- 週末且非補班日 → DAY_OFF
- 國定假日 → HOLIDAY
- 其餘 → NORMAL

### 6.3 請假天數計算
- 完整天數 = end_date - start_date + 1（扣除週末，需檢查補班日）
- 半天假 start_period=AFTERNOON 扣 0.5、end_period=MORNING 扣 0.5
- Phase 2 先簡化：不扣除週末，以工作日天數計算

### 6.4 加班費計算
```java
BigDecimal hourlyRate = monthlySalary.divide(BigDecimal.valueOf(240), 2, RoundingMode.HALF_UP);

// 工作日加班
if (hours <= 2) pay = hours * hourlyRate * 1.33;
else pay = 2 * hourlyRate * 1.33 + (hours - 2) * hourlyRate * 1.66;

// 休息日加班
if (hours <= 2) pay = hours * hourlyRate * 1.33;
else if (hours <= 8) pay = 2 * hourlyRate * 1.33 + (hours - 2) * hourlyRate * 1.66;
else pay = 2 * hourlyRate * 1.33 + 6 * hourlyRate * 1.66 + (hours - 8) * hourlyRate * 2.66;

// 國定假日加班
pay = hours * hourlyRate * 2.0;
```

-- Phase 2: Attendance module tables

CREATE TABLE att_holiday (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    holiday_date DATE NOT NULL,
    name VARCHAR(100) NOT NULL,
    holiday_type ENUM('HOLIDAY','MAKEUP_WORKDAY') NOT NULL,
    year INT NOT NULL,
    version BIGINT DEFAULT 0,
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    created_by VARCHAR(255) DEFAULT NULL,
    updated_by VARCHAR(255) DEFAULT NULL,
    UNIQUE KEY uk_holiday_date (holiday_date)
);

CREATE TABLE att_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    record_date DATE NOT NULL,
    clock_in TIME DEFAULT NULL,
    clock_out TIME DEFAULT NULL,
    work_hours DECIMAL(4,1) DEFAULT NULL,
    status ENUM('NORMAL','LATE','EARLY_LEAVE','ABSENT','DAY_OFF','HOLIDAY') NOT NULL DEFAULT 'NORMAL',
    remark VARCHAR(200) DEFAULT NULL,
    version BIGINT DEFAULT 0,
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    created_by VARCHAR(255) DEFAULT NULL,
    updated_by VARCHAR(255) DEFAULT NULL,
    UNIQUE KEY uk_emp_date (employee_id, record_date),
    CONSTRAINT fk_att_record_employee FOREIGN KEY (employee_id) REFERENCES emp_employee(id)
);

CREATE TABLE att_leave_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    leave_type ENUM('ANNUAL','SICK','PERSONAL','MARRIAGE','BEREAVEMENT','MATERNITY','PATERNITY','OFFICIAL') NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    start_period ENUM('MORNING','AFTERNOON') DEFAULT NULL,
    end_period ENUM('MORNING','AFTERNOON') DEFAULT NULL,
    days_count DECIMAL(4,1) NOT NULL,
    reason VARCHAR(500) DEFAULT NULL,
    status ENUM('PENDING','APPROVED','REJECTED','CANCELLED') NOT NULL DEFAULT 'PENDING',
    approver_id BIGINT DEFAULT NULL,
    approved_at DATETIME DEFAULT NULL,
    rejected_reason VARCHAR(200) DEFAULT NULL,
    version BIGINT DEFAULT 0,
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    created_by VARCHAR(255) DEFAULT NULL,
    updated_by VARCHAR(255) DEFAULT NULL,
    CONSTRAINT fk_leave_employee FOREIGN KEY (employee_id) REFERENCES emp_employee(id)
);

CREATE TABLE att_leave_balance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    leave_type ENUM('ANNUAL','SICK','PERSONAL','MARRIAGE','BEREAVEMENT','MATERNITY','PATERNITY','OFFICIAL') NOT NULL,
    year INT NOT NULL,
    total_days DECIMAL(5,1) NOT NULL,
    used_days DECIMAL(5,1) NOT NULL DEFAULT 0,
    version BIGINT DEFAULT 0,
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    created_by VARCHAR(255) DEFAULT NULL,
    updated_by VARCHAR(255) DEFAULT NULL,
    UNIQUE KEY uk_emp_leave_year (employee_id, leave_type, year),
    CONSTRAINT fk_balance_employee FOREIGN KEY (employee_id) REFERENCES emp_employee(id)
);

CREATE TABLE att_overtime_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    overtime_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    hours DECIMAL(4,1) NOT NULL,
    overtime_type ENUM('WORKDAY','REST_DAY','HOLIDAY') NOT NULL,
    status ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    approver_id BIGINT DEFAULT NULL,
    overtime_pay DECIMAL(15,2) DEFAULT NULL,
    version BIGINT DEFAULT 0,
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    created_by VARCHAR(255) DEFAULT NULL,
    updated_by VARCHAR(255) DEFAULT NULL,
    CONSTRAINT fk_overtime_employee FOREIGN KEY (employee_id) REFERENCES emp_employee(id)
);

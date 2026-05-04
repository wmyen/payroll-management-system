-- Phase 3: Payroll calculation engine tables

CREATE TABLE pay_payroll_period (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    year INT NOT NULL,
    month INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    pay_date DATE NOT NULL,
    status ENUM('DRAFT','PROCESSING','CONFIRMED','LOCKED') NOT NULL DEFAULT 'DRAFT',
    version BIGINT DEFAULT 0,
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    created_by VARCHAR(255) DEFAULT NULL,
    updated_by VARCHAR(255) DEFAULT NULL,
    UNIQUE KEY uk_year_month (year, month)
);

CREATE TABLE pay_payroll_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    period_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    base_salary DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    total_allowances DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    overtime_pay DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    other_earnings DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    gross_pay DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    labor_insurance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    health_insurance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    income_tax DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    leave_deduction DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    other_deductions DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    total_deductions DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    net_pay DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    employer_labor_ins DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    employer_health_ins DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    employer_pension DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    total_employer_cost DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    status ENUM('DRAFT','CONFIRMED') NOT NULL DEFAULT 'DRAFT',
    remark VARCHAR(500) DEFAULT NULL,
    version BIGINT DEFAULT 0,
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    created_by VARCHAR(255) DEFAULT NULL,
    updated_by VARCHAR(255) DEFAULT NULL,
    UNIQUE KEY uk_period_employee (period_id, employee_id),
    CONSTRAINT fk_record_period FOREIGN KEY (period_id) REFERENCES pay_payroll_period(id),
    CONSTRAINT fk_record_employee FOREIGN KEY (employee_id) REFERENCES emp_employee(id)
);

CREATE TABLE pay_payroll_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payroll_record_id BIGINT NOT NULL,
    item_type ENUM('EARNING','DEDUCTION') NOT NULL,
    name VARCHAR(100) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    remark VARCHAR(200) DEFAULT NULL,
    version BIGINT DEFAULT 0,
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    created_by VARCHAR(255) DEFAULT NULL,
    updated_by VARCHAR(255) DEFAULT NULL,
    CONSTRAINT fk_item_record FOREIGN KEY (payroll_record_id) REFERENCES pay_payroll_record(id)
);

CREATE TABLE pay_tax_bracket (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    year INT NOT NULL,
    bracket_start DECIMAL(15,2) NOT NULL,
    bracket_end DECIMAL(15,2) DEFAULT NULL,
    rate DECIMAL(5,4) NOT NULL,
    quick_deduction DECIMAL(15,2) NOT NULL DEFAULT 0.0000,
    version BIGINT DEFAULT 0,
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    created_by VARCHAR(255) DEFAULT NULL,
    updated_by VARCHAR(255) DEFAULT NULL
);

-- Seed: 2025 monthly tax brackets (annual brackets divided by 12)
INSERT INTO pay_tax_bracket (year, bracket_start, bracket_end, rate, quick_deduction) VALUES
(2025, 0.00,       74000.00,  0.0500, 0.00),
(2025, 74000.00,   154000.00, 0.1200, 5170.00),
(2025, 154000.00,  264000.00, 0.2000, 17500.00),
(2025, 264000.00,  444000.00, 0.3000, 43900.00),
(2025, 444000.00,  NULL,      0.4000, 88300.00);

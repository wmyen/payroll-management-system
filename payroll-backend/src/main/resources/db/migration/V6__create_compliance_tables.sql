-- Phase 4: Tax & Compliance Management

-- 勞健保費率版本表
CREATE TABLE pay_insurance_rate (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    effective_date DATE NOT NULL,
    description VARCHAR(200) DEFAULT NULL,

    -- 勞保
    labor_rate DECIMAL(5,4) NOT NULL DEFAULT 0.1100,
    employment_insurance_rate DECIMAL(5,4) NOT NULL DEFAULT 0.0100,
    occupational_rate DECIMAL(5,4) NOT NULL DEFAULT 0.0020,
    employee_labor_share DECIMAL(5,4) NOT NULL DEFAULT 0.2000,
    employer_labor_share DECIMAL(5,4) NOT NULL DEFAULT 0.7000,

    -- 健保
    health_rate DECIMAL(5,4) NOT NULL DEFAULT 0.0517,
    health_employee_share DECIMAL(5,4) NOT NULL DEFAULT 0.3000,
    health_employer_share DECIMAL(5,4) NOT NULL DEFAULT 0.6000,

    -- 勞退
    pension_rate DECIMAL(5,4) NOT NULL DEFAULT 0.0600,

    version BIGINT DEFAULT 0,
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    created_by VARCHAR(255) DEFAULT NULL,
    updated_by VARCHAR(255) DEFAULT NULL
);

-- Seed: 2025 年現行費率
INSERT INTO pay_insurance_rate (effective_date, description, labor_rate, employment_insurance_rate, occupational_rate, employee_labor_share, employer_labor_share, health_rate, health_employee_share, health_employer_share, pension_rate)
VALUES ('2025-01-01', '2025年現行費率', 0.1100, 0.0100, 0.0020, 0.2000, 0.7000, 0.0517, 0.3000, 0.6000, 0.0600);

-- 年度扣繳憑單表
CREATE TABLE pay_withholding_statement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    year INT NOT NULL,
    employee_id BIGINT NOT NULL,
    total_gross DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    total_labor_insurance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    total_health_insurance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    total_income_tax DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    total_net_pay DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    total_employer_cost DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    month_count INT NOT NULL DEFAULT 0,
    status ENUM('DRAFT','CONFIRMED') NOT NULL DEFAULT 'DRAFT',
    version BIGINT DEFAULT 0,
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    created_by VARCHAR(255) DEFAULT NULL,
    updated_by VARCHAR(255) DEFAULT NULL,
    UNIQUE KEY uk_year_employee (year, employee_id),
    CONSTRAINT fk_withholding_employee FOREIGN KEY (employee_id) REFERENCES emp_employee(id)
);

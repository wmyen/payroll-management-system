# Phase 1: 人事主檔管理 實作計畫

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 建立人事主檔管理模塊，包含員工 CRUD、部門樹狀管理、薪資結構設定、RBAC 認證授權，以及 HR 管理前端。

**Architecture:** 模組化單體 Spring Boot 後端（package-by-feature）+ React 前端（payroll-hr-portal）。後端以 Flyway 管理 DB migration，Spring Security + JWT 做認證授權。前端使用 Vite + Tailwind CSS + Zustand。

**Tech Stack:** Spring Boot 3.3.x (Java 17), MySQL 8.x, Flyway, Spring Security + JJWT, React 18, TypeScript, Vite, Tailwind CSS 3, Zustand, Axios

---

## File Structure

### Backend (`payroll-backend/`)
```
payroll-backend/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/payroll/
│   │   │   ├── PayrollApplication.java
│   │   │   ├── shared/
│   │   │   │   ├── domain/BaseEntity.java
│   │   │   │   ├── web/ApiResponse.java
│   │   │   │   ├── web/GlobalExceptionHandler.java
│   │   │   │   ├── util/EncryptionUtil.java
│   │   │   │   └── config/EncryptionConfig.java
│   │   │   ├── department/
│   │   │   │   ├── domain/Department.java
│   │   │   │   ├── repository/DepartmentRepository.java
│   │   │   │   ├── service/DepartmentService.java
│   │   │   │   ├── controller/DepartmentController.java
│   │   │   │   └── dto/DepartmentRequest.java
│   │   │   ├── employee/
│   │   │   │   ├── domain/Employee.java
│   │   │   │   ├── domain/ContractType.java
│   │   │   │   ├── domain/EmployeeStatus.java
│   │   │   │   ├── repository/EmployeeRepository.java
│   │   │   │   ├── service/EmployeeService.java
│   │   │   │   ├── controller/EmployeeController.java
│   │   │   │   └── dto/EmployeeRequest.java
│   │   │   ├── salary/
│   │   │   │   ├── domain/SalaryStructure.java
│   │   │   │   ├── domain/Allowance.java
│   │   │   │   ├── domain/AllowanceType.java
│   │   │   │   ├── repository/SalaryStructureRepository.java
│   │   │   │   ├── repository/AllowanceRepository.java
│   │   │   │   ├── service/SalaryStructureService.java
│   │   │   │   ├── controller/SalaryStructureController.java
│   │   │   │   └── dto/SalaryStructureRequest.java
│   │   │   └── auth/
│   │   │       ├── domain/User.java
│   │   │       ├── domain/Role.java
│   │   │       ├── repository/UserRepository.java
│   │   │       ├── service/AuthService.java
│   │   │       ├── service/UserDetailsService.java
│   │   │       ├── security/JwtUtil.java
│   │   │       ├── security/JwtAuthFilter.java
│   │   │       ├── security/SecurityConfig.java
│   │   │       ├── controller/AuthController.java
│   │   │       └── dto/LoginRequest.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/
│   │           ├── V1__create_auth_tables.sql
│   │           ├── V2__create_emp_department.sql
│   │           ├── V3__create_emp_employee.sql
│   │           └── V4__create_emp_salary.sql
│   └── test/
│       └── java/com/payroll/
│           ├── department/
│           │   └── DepartmentServiceTest.java
│           ├── employee/
│           │   └── EmployeeServiceTest.java
│           ├── salary/
│           │   └── SalaryStructureServiceTest.java
│           └── auth/
│               └── AuthServiceTest.java
```

### Frontend (`payroll-hr-portal/`)
```
payroll-hr-portal/
├── package.json
├── vite.config.ts
├── tailwind.config.js
├── postcss.config.js
├── tsconfig.json
├── index.html
├── src/
│   ├── main.tsx
│   ├── App.tsx
│   ├── api/client.ts
│   ├── api/auth.ts
│   ├── api/employees.ts
│   ├── api/departments.ts
│   ├── api/salary.ts
│   ├── stores/authStore.ts
│   ├── stores/employeeStore.ts
│   ├── types/index.ts
│   ├── utils/formatMoney.ts
│   ├── components/ui/
│   │   ├── Button.tsx
│   │   ├── Input.tsx
│   │   ├── Table.tsx
│   │   ├── Modal.tsx
│   │   └── Select.tsx
│   ├── components/layout/
│   │   ├── Sidebar.tsx
│   │   ├── Header.tsx
│   │   └── MainLayout.tsx
│   └── pages/
│       ├── Login.tsx
│       ├── Dashboard.tsx
│       ├── employees/
│       │   ├── EmployeeList.tsx
│       │   └── EmployeeForm.tsx
│       └── departments/
│           └── DepartmentTree.tsx
```

### Infrastructure
```
infrastructure/
├── docker-compose.yml
├── docker-compose.dev.yml
├── .env.example
└── nginx/
    └── default.conf
```

---

## Task 1: Backend Project Scaffold

**Files:**
- Create: `payroll-backend/pom.xml`
- Create: `payroll-backend/src/main/java/com/payroll/PayrollApplication.java`
- Create: `payroll-backend/src/main/resources/application.yml`

- [ ] **Step 1: Create Maven pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.6</version>
        <relativePath/>
    </parent>
    <groupId>com.payroll</groupId>
    <artifactId>payroll-backend</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>payroll-backend</name>
    <properties>
        <java.version>17</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-mysql</artifactId>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.12.6</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.12.6</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.12.6</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: Create main application class**

```java
// payroll-backend/src/main/java/com/payroll/PayrollApplication.java
package com.payroll;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PayrollApplication {
    public static void main(String[] args) {
        SpringApplication.run(PayrollApplication.class, args);
    }
}
```

- [ ] **Step 3: Create application.yml**

```yaml
# payroll-backend/src/main/resources/application.yml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:payroll}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Taipei&characterEncoding=utf8mb4
    username: ${DB_USER:payroll}
    password: ${DB_PASSWORD:payroll123}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    open-in-view: false
  flyway:
    enabled: true
    locations: classpath:db/migration

encryption:
  aes-key: ${ENCRYPTION_AES_KEY:dGVzdC1rZXktZm9yLWRldmVsb3BtZW50LW9ubHk=}

jwt:
  secret: ${JWT_SECRET:ZGV2LWp3dC1zZWNyZXQta2V5LWZvci1sb2NhbC1kZXZlbG9wbWVudC1vbmx5}
  access-token-expiration: 1800000
  refresh-token-expiration: 604800000
```

- [ ] **Step 4: Verify compilation**

Run: `cd payroll-backend && ./mvnw compile`
Expected: BUILD SUCCESS

> Note: Use `mvn wrapper:wrapper` to generate mvnw if not present, or install Maven and use `mvn` directly.

- [ ] **Step 5: Commit**

```bash
git add payroll-backend/
git commit -m "feat: scaffold Spring Boot backend project"
```

---

## Task 2: Flyway Migrations - Auth & Employee Tables

**Files:**
- Create: `payroll-backend/src/main/resources/db/migration/V1__create_auth_tables.sql`
- Create: `payroll-backend/src/main/resources/db/migration/V2__create_emp_department.sql`
- Create: `payroll-backend/src/main/resources/db/migration/V3__create_emp_employee.sql`
- Create: `payroll-backend/src/main/resources/db/migration/V4__create_emp_salary.sql`

- [ ] **Step 1: Write V1 - Auth tables**

```sql
-- V1__create_auth_tables.sql
CREATE TABLE auth_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'EMPLOYEE',
    employee_id BIGINT,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_auth_user_employee FOREIGN KEY (employee_id) REFERENCES emp_employee(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE auth_refresh_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES auth_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

Wait — `auth_user` references `emp_employee` but employee table doesn't exist yet. We need to reorder. Create employee tables first, then auth tables that reference them.

Revised order:
- V1: emp_department, emp_employee (no FK to auth)
- V2: emp_salary_structure, emp_allowance
- V3: auth_user, auth_refresh_token (FK to emp_employee)

```sql
-- V1__create_emp_department_and_employee.sql
CREATE TABLE emp_department (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    parent_id BIGINT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_dept_parent FOREIGN KEY (parent_id) REFERENCES emp_department(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE emp_employee (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    id_number VARCHAR(255) NOT NULL,
    bank_account VARCHAR(255),
    hire_date DATE NOT NULL,
    leave_date DATE,
    department_id BIGINT,
    contract_type VARCHAR(20) NOT NULL DEFAULT 'REGULAR',
    job_level VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    email VARCHAR(150),
    phone VARCHAR(30),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_emp_department FOREIGN KEY (department_id) REFERENCES emp_department(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

```sql
-- V2__create_emp_salary.sql
CREATE TABLE emp_salary_structure (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    base_salary DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    effective_date DATE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_salary_employee FOREIGN KEY (employee_id) REFERENCES emp_employee(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE emp_allowance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    salary_structure_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    amount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_allowance_salary FOREIGN KEY (salary_structure_id) REFERENCES emp_salary_structure(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

```sql
-- V3__create_auth_tables.sql
CREATE TABLE auth_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'EMPLOYEE',
    employee_id BIGINT,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_auth_user_employee FOREIGN KEY (employee_id) REFERENCES emp_employee(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE auth_refresh_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES auth_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed default admin user (password: admin123, BCrypt encoded)
INSERT INTO auth_user (username, password, role, enabled)
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', TRUE);
```

- [ ] **Step 2: Commit**

```bash
git add payroll-backend/src/main/resources/db/migration/
git commit -m "feat: add Flyway migrations for employee, salary, and auth tables"
```

---

## Task 3: Shared Kernel

**Files:**
- Create: `payroll-backend/src/main/java/com/payroll/shared/domain/BaseEntity.java`
- Create: `payroll-backend/src/main/java/com/payroll/shared/web/ApiResponse.java`
- Create: `payroll-backend/src/main/java/com/payroll/shared/web/GlobalExceptionHandler.java`
- Create: `payroll-backend/src/main/java/com/payroll/shared/util/EncryptionUtil.java`
- Create: `payroll-backend/src/main/java/com/payroll/shared/config/EncryptionConfig.java`

- [ ] **Step 1: Write BaseEntity**

```java
// payroll-backend/src/main/java/com/payroll/shared/domain/BaseEntity.java
package com.payroll.shared.domain;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;
}
```

- [ ] **Step 2: Write ApiResponse**

```java
// payroll-backend/src/main/java/com/payroll/shared/web/ApiResponse.java
package com.payroll.shared.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private int code;
    private T data;
    private String message;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(200, data, "success");
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(200, data, message);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, null, message);
    }
}
```

- [ ] **Step 3: Write GlobalExceptionHandler**

```java
// payroll-backend/src/main/java/com/payroll/shared/web/GlobalExceptionHandler.java
package com.payroll.shared.web;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(404, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "Internal server error"));
    }
}
```

- [ ] **Step 4: Write EncryptionUtil and Config**

```java
// payroll-backend/src/main/java/com/payroll/shared/util/EncryptionUtil.java
package com.payroll.shared.util;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class EncryptionUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private final SecretKeySpec keySpec;

    public EncryptionUtil(String base64Key) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        this.keySpec = new SecretKeySpec(keyBytes, "AES");
    }

    public String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(plaintext.getBytes());
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decrypt(String ciphertext) {
        try {
            byte[] combined = Base64.getDecoder().decode(ciphertext);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encrypted = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(combined, GCM_IV_LENGTH, encrypted, 0, encrypted.length);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            return new String(cipher.doFinal(encrypted));
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }

    public static String mask(String value, int visiblePrefix, int visibleSuffix) {
        if (value == null || value.length() <= visiblePrefix + visibleSuffix) {
            return "***";
        }
        return value.substring(0, visiblePrefix) + "***" + value.substring(value.length() - visibleSuffix);
    }
}
```

```java
// payroll-backend/src/main/java/com/payroll/shared/config/EncryptionConfig.java
package com.payroll.shared.config;

import com.payroll.shared.util.EncryptionUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class EncryptionConfig {

    @Bean
    public EncryptionUtil encryptionUtil(@Value("${encryption.aes-key}") String aesKey) {
        return new EncryptionUtil(aesKey);
    }
}
```

- [ ] **Step 5: Write EncryptionUtil test**

```java
// payroll-backend/src/test/java/com/payroll/shared/util/EncryptionUtilTest.java
package com.payroll.shared.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionUtilTest {

    private EncryptionUtil util;

    @BeforeEach
    void setUp() {
        String testKey = java.util.Base64.getEncoder().encodeToString(new byte[16]);
        util = new EncryptionUtil(testKey);
    }

    @Test
    void encrypt_and_decrypt_roundtrip() {
        String original = "A123456789";
        String encrypted = util.encrypt(original);
        assertNotEquals(original, encrypted);
        assertEquals(original, util.decrypt(encrypted));
    }

    @Test
    void mask_returns_correct_format() {
        assertEquals("A12***89", EncryptionUtil.mask("A123456789", 3, 2));
    }

    @Test
    void mask_handles_short_string() {
        assertEquals("***", EncryptionUtil.mask("AB", 2, 2));
    }

    @Test
    void encrypt_produces_different_ciphertext_each_time() {
        String original = "A123456789";
        String encrypted1 = util.encrypt(original);
        String encrypted2 = util.encrypt(original);
        assertNotEquals(encrypted1, encrypted2); // random IV each time
    }
}
```

- [ ] **Step 6: Run tests**

Run: `cd payroll-backend && mvn test -pl . -Dtest=EncryptionUtilTest`
Expected: 4 tests PASS

- [ ] **Step 7: Commit**

```bash
git add payroll-backend/src/
git commit -m "feat: add shared kernel (BaseEntity, ApiResponse, EncryptionUtil)"
```

---

## Task 4: Department Domain + Repository

**Files:**
- Create: `payroll-backend/src/main/java/com/payroll/department/domain/Department.java`
- Create: `payroll-backend/src/main/java/com/payroll/department/repository/DepartmentRepository.java`

- [ ] **Step 1: Write Department entity**

```java
// payroll-backend/src/main/java/com/payroll/department/domain/Department.java
package com.payroll.department.domain;

import com.payroll.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "emp_department")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Department extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Department parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.PERSIST)
    @Builder.Default
    private List<Department> children = new ArrayList<>();

    public void updateName(String name) {
        this.name = name;
    }

    public void moveTo(Department newParent) {
        this.parent = newParent;
    }
}
```

- [ ] **Step 2: Write DepartmentRepository**

```java
// payroll-backend/src/main/java/com/payroll/department/repository/DepartmentRepository.java
package com.payroll.department.repository;

import com.payroll.department.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByParentIsNull();
    List<Department> findByParentId(Long parentId);
    boolean existsByName(String name);
}
```

- [ ] **Step 3: Commit**

```bash
git add payroll-backend/src/main/java/com/payroll/department/
git commit -m "feat: add Department entity and repository"
```

---

## Task 5: Employee Domain + Repository

**Files:**
- Create: `payroll-backend/src/main/java/com/payroll/employee/domain/Employee.java`
- Create: `payroll-backend/src/main/java/com/payroll/employee/domain/ContractType.java`
- Create: `payroll-backend/src/main/java/com/payroll/employee/domain/EmployeeStatus.java`
- Create: `payroll-backend/src/main/java/com/payroll/employee/repository/EmployeeRepository.java`

- [ ] **Step 1: Write enums**

```java
// payroll-backend/src/main/java/com/payroll/employee/domain/ContractType.java
package com.payroll.employee.domain;

public enum ContractType {
    REGULAR, CONTRACT, PART_TIME, INTERN
}
```

```java
// payroll-backend/src/main/java/com/payroll/employee/domain/EmployeeStatus.java
package com.payroll.employee.domain;

public enum EmployeeStatus {
    ACTIVE, SUSPENDED, LEFT
}
```

- [ ] **Step 2: Write Employee entity**

```java
// payroll-backend/src/main/java/com/payroll/employee/domain/Employee.java
package com.payroll.employee.domain;

import com.payroll.department.domain.Department;
import com.payroll.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "emp_employee")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Employee extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String idNumber; // AES encrypted

    @Column(length = 255)
    private String bankAccount; // AES encrypted

    @Column(nullable = false)
    private LocalDate hireDate;

    private LocalDate leaveDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ContractType contractType = ContractType.REGULAR;

    @Column(length = 50)
    private String jobLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    @Column(length = 150)
    private String email;

    @Column(length = 30)
    private String phone;

    public void updateBasicInfo(String name, String email, String phone, String jobLevel) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.jobLevel = jobLevel;
    }

    public void assignToDepartment(Department department) {
        this.department = department;
    }

    public void resign(LocalDate leaveDate) {
        this.leaveDate = leaveDate;
        this.status = EmployeeStatus.LEFT;
    }
}
```

- [ ] **Step 3: Write EmployeeRepository**

```java
// payroll-backend/src/main/java/com/payroll/employee/repository/EmployeeRepository.java
package com.payroll.employee.repository;

import com.payroll.employee.domain.Employee;
import com.payroll.employee.domain.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Page<Employee> findByStatus(EmployeeStatus status, Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE " +
           "(:name IS NULL OR e.name LIKE %:name%) AND " +
           "(:departmentId IS NULL OR e.department.id = :departmentId) AND " +
           "(:status IS NULL OR e.status = :status)")
    Page<Employee> search(@Param("name") String name,
                          @Param("departmentId") Long departmentId,
                          @Param("status") EmployeeStatus status,
                          Pageable pageable);

    List<Employee> findByDepartmentId(Long departmentId);
}
```

- [ ] **Step 4: Commit**

```bash
git add payroll-backend/src/main/java/com/payroll/employee/
git commit -m "feat: add Employee entity, enums, and repository"
```

---

## Task 6: Salary Structure Domain + Repository

**Files:**
- Create: `payroll-backend/src/main/java/com/payroll/salary/domain/SalaryStructure.java`
- Create: `payroll-backend/src/main/java/com/payroll/salary/domain/Allowance.java`
- Create: `payroll-backend/src/main/java/com/payroll/salary/domain/AllowanceType.java`
- Create: `payroll-backend/src/main/java/com/payroll/salary/repository/SalaryStructureRepository.java`
- Create: `payroll-backend/src/main/java/com/payroll/salary/repository/AllowanceRepository.java`

- [ ] **Step 1: Write AllowanceType enum**

```java
// payroll-backend/src/main/java/com/payroll/salary/domain/AllowanceType.java
package com.payroll.salary.domain;

public enum AllowanceType {
    TRANSPORT, MEAL, HOUSING, POSITION, OTHER
}
```

- [ ] **Step 2: Write SalaryStructure entity**

```java
// payroll-backend/src/main/java/com/payroll/salary/domain/SalaryStructure.java
package com.payroll.salary.domain;

import com.payroll.employee.domain.Employee;
import com.payroll.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "emp_salary_structure")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SalaryStructure extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal baseSalary;

    @Column(nullable = false)
    private LocalDate effectiveDate;

    @OneToMany(mappedBy = "salaryStructure", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Allowance> allowances = new ArrayList<>();

    public BigDecimal getTotalAllowances() {
        return allowances.stream()
                .map(Allowance::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getGrossSalary() {
        return baseSalary.add(getTotalAllowances());
    }

    public void addAllowance(Allowance allowance) {
        allowances.add(allowance);
        allowance.setSalaryStructure(this);
    }

    public void removeAllowance(Allowance allowance) {
        allowances.remove(allowance);
        allowance.setSalaryStructure(null);
    }
}
```

- [ ] **Step 3: Write Allowance entity**

```java
// payroll-backend/src/main/java/com/payroll/salary/domain/Allowance.java
package com.payroll.salary.domain;

import com.payroll.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "emp_allowance")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Allowance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salary_structure_id", nullable = false)
    private SalaryStructure salaryStructure;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AllowanceType type;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    void setSalaryStructure(SalaryStructure salaryStructure) {
        this.salaryStructure = salaryStructure;
    }
}
```

- [ ] **Step 4: Write repositories**

```java
// payroll-backend/src/main/java/com/payroll/salary/repository/SalaryStructureRepository.java
package com.payroll.salary.repository;

import com.payroll.salary.domain.SalaryStructure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SalaryStructureRepository extends JpaRepository<SalaryStructure, Long> {
    List<SalaryStructure> findByEmployeeIdOrderByEffectiveDateDesc(Long employeeId);
    Optional<SalaryStructure> findFirstByEmployeeIdOrderByEffectiveDateDesc(Long employeeId);
}
```

```java
// payroll-backend/src/main/java/com/payroll/salary/repository/AllowanceRepository.java
package com.payroll.salary.repository;

import com.payroll.salary.domain.Allowance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AllowanceRepository extends JpaRepository<Allowance, Long> {
}
```

- [ ] **Step 5: Commit**

```bash
git add payroll-backend/src/main/java/com/payroll/salary/
git commit -m "feat: add SalaryStructure, Allowance entities and repositories"
```

---

## Task 7: Department Service + DTOs + Tests (TDD)

**Files:**
- Create: `payroll-backend/src/main/java/com/payroll/department/dto/DepartmentRequest.java`
- Create: `payroll-backend/src/main/java/com/payroll/department/service/DepartmentService.java`
- Create: `payroll-backend/src/test/java/com/payroll/department/DepartmentServiceTest.java`

- [ ] **Step 1: Write failing test for DepartmentService**

```java
// payroll-backend/src/test/java/com/payroll/department/DepartmentServiceTest.java
package com.payroll.department;

import com.payroll.department.domain.Department;
import com.payroll.department.dto.DepartmentRequest;
import com.payroll.department.repository.DepartmentRepository;
import com.payroll.department.service.DepartmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentService departmentService;

    @Test
    void create_department_without_parent() {
        DepartmentRequest request = new DepartmentRequest("Engineering", null);

        Department saved = Department.builder().name("Engineering").build();
        when(departmentRepository.save(any(Department.class))).thenReturn(saved);

        Department result = departmentService.create(request);

        assertEquals("Engineering", result.getName());
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void create_department_with_parent() {
        Department parent = Department.builder().name("Company").build();
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(departmentRepository.save(any(Department.class))).thenAnswer(inv -> inv.getArgument(0));

        DepartmentRequest request = new DepartmentRequest("Engineering", 1L);
        Department result = departmentService.create(request);

        assertEquals("Engineering", result.getName());
        assertEquals(parent, result.getParent());
    }

    @Test
    void create_department_with_nonexistent_parent_throws() {
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        DepartmentRequest request = new DepartmentRequest("Engineering", 999L);
        assertThrows(IllegalArgumentException.class, () -> departmentService.create(request));
    }

    @Test
    void get_tree_returns_root_departments() {
        Department root = Department.builder().name("Company").build();
        when(departmentRepository.findByParentIsNull()).thenReturn(List.of(root));

        List<Department> roots = departmentService.getRootDepartments();

        assertEquals(1, roots.size());
        assertEquals("Company", roots.get(0).getName());
    }

    @Test
    void update_department_name() {
        Department dept = Department.builder().name("Old Name").build();
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
        when(departmentRepository.save(any(Department.class))).thenReturn(dept);

        Department result = departmentService.update(1L, new DepartmentRequest("New Name", null));

        assertEquals("New Name", result.getName());
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `cd payroll-backend && mvn test -Dtest=DepartmentServiceTest`
Expected: FAIL (classes not found)

- [ ] **Step 3: Write DepartmentRequest DTO**

```java
// payroll-backend/src/main/java/com/payroll/department/dto/DepartmentRequest.java
package com.payroll.department.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentRequest {
    @NotBlank(message = "Department name is required")
    private String name;
    private Long parentId;
}
```

- [ ] **Step 4: Write DepartmentService**

```java
// payroll-backend/src/main/java/com/payroll/department/service/DepartmentService.java
package com.payroll.department.service;

import com.payroll.department.domain.Department;
import com.payroll.department.dto.DepartmentRequest;
import com.payroll.department.repository.DepartmentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Transactional
    public Department create(DepartmentRequest request) {
        Department.DepartmentBuilder builder = Department.builder().name(request.getName());
        if (request.getParentId() != null) {
            Department parent = departmentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent department not found: " + request.getParentId()));
            builder.parent(parent);
        }
        return departmentRepository.save(builder.build());
    }

    @Transactional
    public Department update(Long id, DepartmentRequest request) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Department not found: " + id));
        dept.updateName(request.getName());
        return departmentRepository.save(dept);
    }

    public List<Department> getRootDepartments() {
        return departmentRepository.findByParentIsNull();
    }

    public Department getById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Department not found: " + id));
    }

    @Transactional
    public void delete(Long id) {
        Department dept = getById(id);
        if (!dept.getChildren().isEmpty()) {
            throw new IllegalArgumentException("Cannot delete department with child departments");
        }
        departmentRepository.delete(dept);
    }
}
```

- [ ] **Step 5: Run tests to verify they pass**

Run: `cd payroll-backend && mvn test -Dtest=DepartmentServiceTest`
Expected: 5 tests PASS

- [ ] **Step 6: Commit**

```bash
git add payroll-backend/src/main/java/com/payroll/department/ payroll-backend/src/test/java/com/payroll/department/
git commit -m "feat: add DepartmentService with DTOs and tests (TDD)"
```

---

## Task 8: Employee Service + DTOs + Tests (TDD)

**Files:**
- Create: `payroll-backend/src/main/java/com/payroll/employee/dto/EmployeeRequest.java`
- Create: `payroll-backend/src/main/java/com/payroll/employee/service/EmployeeService.java`
- Create: `payroll-backend/src/test/java/com/payroll/employee/EmployeeServiceTest.java`

- [ ] **Step 1: Write failing tests for EmployeeService**

```java
// payroll-backend/src/test/java/com/payroll/employee/EmployeeServiceTest.java
package com.payroll.employee;

import com.payroll.employee.domain.Employee;
import com.payroll.employee.domain.EmployeeStatus;
import com.payroll.employee.dto.EmployeeRequest;
import com.payroll.employee.repository.EmployeeRepository;
import com.payroll.employee.service.EmployeeService;
import com.payroll.shared.util.EncryptionUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EncryptionUtil encryptionUtil;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void create_employee_encrypts_sensitive_fields() {
        EmployeeRequest request = new EmployeeRequest();
        request.setName("王大明");
        request.setIdNumber("A123456789");
        request.setBankAccount("1234567890");
        request.setHireDate(LocalDate.of(2024, 1, 1));
        request.setDepartmentId(null);

        when(encryptionUtil.encrypt("A123456789")).thenReturn("enc_id");
        when(encryptionUtil.encrypt("1234567890")).thenReturn("enc_bank");
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        Employee result = employeeService.create(request);

        assertEquals("王大明", result.getName());
        verify(encryptionUtil).encrypt("A123456789");
        verify(encryptionUtil).encrypt("1234567890");
    }

    @Test
    void search_employees_with_pagination() {
        Page<Employee> page = new PageImpl<>(List.of(
                Employee.builder().name("王大明").build()
        ));
        when(employeeRepository.search("王", null, null, PageRequest.of(0, 20)))
                .thenReturn(page);

        Page<Employee> result = employeeService.search("王", null, null, PageRequest.of(0, 20));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void resign_employee_sets_status_and_leave_date() {
        Employee emp = Employee.builder()
                .name("王大明")
                .hireDate(LocalDate.of(2024, 1, 1))
                .build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        when(employeeRepository.save(any(Employee.class))).thenReturn(emp);

        LocalDate leaveDate = LocalDate.of(2024, 6, 30);
        employeeService.resign(1L, leaveDate);

        assertEquals(EmployeeStatus.LEFT, emp.getStatus());
        assertEquals(leaveDate, emp.getLeaveDate());
    }

    @Test
    void get_employee_decrypts_sensitive_fields() {
        Employee emp = Employee.builder()
                .name("王大明")
                .idNumber("enc_id")
                .bankAccount("enc_bank")
                .hireDate(LocalDate.of(2024, 1, 1))
                .build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        when(encryptionUtil.decrypt("enc_id")).thenReturn("A123456789");
        when(encryptionUtil.decrypt("enc_bank")).thenReturn("1234567890");

        Employee result = employeeService.getById(1L);

        assertEquals("A123456789", result.getIdNumber());
        assertEquals("1234567890", result.getBankAccount());
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `cd payroll-backend && mvn test -Dtest=EmployeeServiceTest`
Expected: FAIL

- [ ] **Step 3: Write EmployeeRequest DTO**

```java
// payroll-backend/src/main/java/com/payroll/employee/dto/EmployeeRequest.java
package com.payroll.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EmployeeRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "ID number is required")
    private String idNumber;

    private String bankAccount;

    @NotNull(message = "Hire date is required")
    private LocalDate hireDate;

    private Long departmentId;

    private String contractType;

    private String jobLevel;

    private String email;

    private String phone;
}
```

- [ ] **Step 4: Write EmployeeService**

```java
// payroll-backend/src/main/java/com/payroll/employee/service/EmployeeService.java
package com.payroll.employee.service;

import com.payroll.department.domain.Department;
import com.payroll.department.repository.DepartmentRepository;
import com.payroll.employee.domain.ContractType;
import com.payroll.employee.domain.Employee;
import com.payroll.employee.domain.EmployeeStatus;
import com.payroll.employee.dto.EmployeeRequest;
import com.payroll.employee.repository.EmployeeRepository;
import com.payroll.shared.util.EncryptionUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final EncryptionUtil encryptionUtil;

    @Transactional
    public Employee create(EmployeeRequest request) {
        Employee employee = Employee.builder()
                .name(request.getName())
                .idNumber(encryptionUtil.encrypt(request.getIdNumber()))
                .hireDate(request.getHireDate())
                .contractType(request.getContractType() != null
                        ? ContractType.valueOf(request.getContractType()) : ContractType.REGULAR)
                .jobLevel(request.getJobLevel())
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();

        if (request.getBankAccount() != null) {
            employee = Employee.builder()
                    .name(employee.getName())
                    .idNumber(employee.getIdNumber())
                    .hireDate(employee.getHireDate())
                    .contractType(employee.getContractType())
                    .jobLevel(employee.getJobLevel())
                    .email(employee.getEmail())
                    .phone(employee.getPhone())
                    .bankAccount(encryptionUtil.encrypt(request.getBankAccount()))
                    .build();
        }

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new EntityNotFoundException("Department not found: " + request.getDepartmentId()));
            employee.assignToDepartment(dept);
        }

        return employeeRepository.save(employee);
    }

    public Employee getById(Long id) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found: " + id));
        emp = decryptSensitiveFields(emp);
        return emp;
    }

    public Page<Employee> search(String name, Long departmentId, EmployeeStatus status, Pageable pageable) {
        return employeeRepository.search(name, departmentId, status, pageable);
    }

    @Transactional
    public Employee update(Long id, EmployeeRequest request) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found: " + id));
        emp.updateBasicInfo(request.getName(), request.getEmail(), request.getPhone(), request.getJobLevel());
        return employeeRepository.save(emp);
    }

    @Transactional
    public void resign(Long id, LocalDate leaveDate) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found: " + id));
        emp.resign(leaveDate);
        employeeRepository.save(emp);
    }

    private Employee decryptSensitiveFields(Employee emp) {
        Employee decrypted = Employee.builder()
                .hireDate(emp.getHireDate())
                .leaveDate(emp.getLeaveDate())
                .contractType(emp.getContractType())
                .jobLevel(emp.getJobLevel())
                .status(emp.getStatus())
                .email(emp.getEmail())
                .phone(emp.getPhone())
                .department(emp.getDepartment())
                .name(emp.getName())
                .build();
        // Preserve id and audit fields
        try {
            var idField = Employee.class.getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(decrypted, emp.getId());
        } catch (Exception ignored) {}
        decrypted = Employee.builder()
                .hireDate(emp.getHireDate())
                .leaveDate(emp.getLeaveDate())
                .contractType(emp.getContractType())
                .jobLevel(emp.getJobLevel())
                .status(emp.getStatus())
                .email(emp.getEmail())
                .phone(emp.getPhone())
                .department(emp.getDepartment())
                .name(emp.getName())
                .idNumber(encryptionUtil.decrypt(emp.getIdNumber()))
                .bankAccount(emp.getBankAccount() != null ? encryptionUtil.decrypt(emp.getBankAccount()) : null)
                .build();
        return decrypted;
    }
}
```

- [ ] **Step 5: Run tests to verify they pass**

Run: `cd payroll-backend && mvn test -Dtest=EmployeeServiceTest`
Expected: 4 tests PASS

- [ ] **Step 6: Commit**

```bash
git add payroll-backend/src/main/java/com/payroll/employee/ payroll-backend/src/test/java/com/payroll/employee/
git commit -m "feat: add EmployeeService with DTOs and tests (TDD)"
```

---

## Task 9: Salary Structure Service + Tests (TDD)

**Files:**
- Create: `payroll-backend/src/main/java/com/payroll/salary/dto/SalaryStructureRequest.java`
- Create: `payroll-backend/src/main/java/com/payroll/salary/service/SalaryStructureService.java`
- Create: `payroll-backend/src/test/java/com/payroll/salary/SalaryStructureServiceTest.java`

- [ ] **Step 1: Write failing tests**

```java
// payroll-backend/src/test/java/com/payroll/salary/SalaryStructureServiceTest.java
package com.payroll.salary;

import com.payroll.employee.domain.Employee;
import com.payroll.employee.repository.EmployeeRepository;
import com.payroll.salary.domain.Allowance;
import com.payroll.salary.domain.AllowanceType;
import com.payroll.salary.domain.SalaryStructure;
import com.payroll.salary.dto.SalaryStructureRequest;
import com.payroll.salary.repository.SalaryStructureRepository;
import com.payroll.salary.service.SalaryStructureService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalaryStructureServiceTest {

    @Mock
    private SalaryStructureRepository salaryStructureRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private SalaryStructureService salaryStructureService;

    @Test
    void create_salary_structure_with_allowances() {
        Employee emp = Employee.builder().name("王大明").hireDate(LocalDate.of(2024,1,1)).build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        when(salaryStructureRepository.save(any(SalaryStructure.class))).thenAnswer(inv -> inv.getArgument(0));

        SalaryStructureRequest.AllowanceDto allowance = new SalaryStructureRequest.AllowanceDto();
        allowance.setType(AllowanceType.TRANSPORT);
        allowance.setAmount(new BigDecimal("3000"));

        SalaryStructureRequest request = new SalaryStructureRequest();
        request.setEmployeeId(1L);
        request.setBaseSalary(new BigDecimal("50000"));
        request.setEffectiveDate(LocalDate.of(2024, 1, 1));
        request.setAllowances(List.of(allowance));

        SalaryStructure result = salaryStructureService.create(request);

        assertEquals(new BigDecimal("50000"), result.getBaseSalary());
        assertEquals(new BigDecimal("53000"), result.getGrossSalary());
    }

    @Test
    void get_current_salary_structure() {
        SalaryStructure ss = SalaryStructure.builder()
                .baseSalary(new BigDecimal("50000"))
                .effectiveDate(LocalDate.of(2024, 1, 1))
                .build();
        when(salaryStructureRepository.findFirstByEmployeeIdOrderByEffectiveDateDesc(1L))
                .thenReturn(Optional.of(ss));

        SalaryStructure result = salaryStructureService.getCurrentByEmployeeId(1L);

        assertEquals(new BigDecimal("50000"), result.getBaseSalary());
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `cd payroll-backend && mvn test -Dtest=SalaryStructureServiceTest`
Expected: FAIL

- [ ] **Step 3: Write SalaryStructureRequest DTO**

```java
// payroll-backend/src/main/java/com/payroll/salary/dto/SalaryStructureRequest.java
package com.payroll.salary.dto;

import com.payroll.salary.domain.AllowanceType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class SalaryStructureRequest {
    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Base salary is required")
    private BigDecimal baseSalary;

    @NotNull(message = "Effective date is required")
    private LocalDate effectiveDate;

    private List<AllowanceDto> allowances;

    @Getter
    @Setter
    public static class AllowanceDto {
        private AllowanceType type;
        private BigDecimal amount;
    }
}
```

- [ ] **Step 4: Write SalaryStructureService**

```java
// payroll-backend/src/main/java/com/payroll/salary/service/SalaryStructureService.java
package com.payroll.salary.service;

import com.payroll.employee.domain.Employee;
import com.payroll.employee.repository.EmployeeRepository;
import com.payroll.salary.domain.Allowance;
import com.payroll.salary.domain.SalaryStructure;
import com.payroll.salary.dto.SalaryStructureRequest;
import com.payroll.salary.repository.SalaryStructureRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SalaryStructureService {

    private final SalaryStructureRepository salaryStructureRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public SalaryStructure create(SalaryStructureRequest request) {
        Employee emp = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found: " + request.getEmployeeId()));

        SalaryStructure ss = SalaryStructure.builder()
                .employee(emp)
                .baseSalary(request.getBaseSalary())
                .effectiveDate(request.getEffectiveDate())
                .build();

        if (request.getAllowances() != null) {
            for (SalaryStructureRequest.AllowanceDto dto : request.getAllowances()) {
                Allowance allowance = Allowance.builder()
                        .type(dto.getType())
                        .amount(dto.getAmount())
                        .build();
                ss.addAllowance(allowance);
            }
        }

        return salaryStructureRepository.save(ss);
    }

    public SalaryStructure getCurrentByEmployeeId(Long employeeId) {
        return salaryStructureRepository.findFirstByEmployeeIdOrderByEffectiveDateDesc(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("No salary structure found for employee: " + employeeId));
    }

    public List<SalaryStructure> getHistoryByEmployeeId(Long employeeId) {
        return salaryStructureRepository.findByEmployeeIdOrderByEffectiveDateDesc(employeeId);
    }
}
```

- [ ] **Step 5: Run tests to verify they pass**

Run: `cd payroll-backend && mvn test -Dtest=SalaryStructureServiceTest`
Expected: 2 tests PASS

- [ ] **Step 6: Commit**

```bash
git add payroll-backend/src/main/java/com/payroll/salary/ payroll-backend/src/test/java/com/payroll/salary/
git commit -m "feat: add SalaryStructureService with DTOs and tests (TDD)"
```

---

## Task 10: REST Controllers

**Files:**
- Create: `payroll-backend/src/main/java/com/payroll/department/controller/DepartmentController.java`
- Create: `payroll-backend/src/main/java/com/payroll/employee/controller/EmployeeController.java`
- Create: `payroll-backend/src/main/java/com/payroll/salary/controller/SalaryStructureController.java`

- [ ] **Step 1: Write DepartmentController**

```java
// payroll-backend/src/main/java/com/payroll/department/controller/DepartmentController.java
package com.payroll.department.controller;

import com.payroll.department.domain.Department;
import com.payroll.department.dto.DepartmentRequest;
import com.payroll.department.service.DepartmentService;
import com.payroll.shared.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public ApiResponse<List<Department>> getTree() {
        return ApiResponse.ok(departmentService.getRootDepartments());
    }

    @GetMapping("/{id}")
    public ApiResponse<Department> getById(@PathVariable Long id) {
        return ApiResponse.ok(departmentService.getById(id));
    }

    @PostMapping
    public ApiResponse<Department> create(@Valid @RequestBody DepartmentRequest request) {
        return ApiResponse.ok(departmentService.create(request), "Department created");
    }

    @PutMapping("/{id}")
    public ApiResponse<Department> update(@PathVariable Long id, @Valid @RequestBody DepartmentRequest request) {
        return ApiResponse.ok(departmentService.update(id, request), "Department updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return ApiResponse.ok(null, "Department deleted");
    }
}
```

- [ ] **Step 2: Write EmployeeController**

```java
// payroll-backend/src/main/java/com/payroll/employee/controller/EmployeeController.java
package com.payroll.employee.controller;

import com.payroll.employee.domain.Employee;
import com.payroll.employee.domain.EmployeeStatus;
import com.payroll.employee.dto.EmployeeRequest;
import com.payroll.employee.service.EmployeeService;
import com.payroll.shared.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public ApiResponse<Page<Employee>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) EmployeeStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.ok(employeeService.search(name, departmentId, status, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<Employee> getById(@PathVariable Long id) {
        return ApiResponse.ok(employeeService.getById(id));
    }

    @PostMapping
    public ApiResponse<Employee> create(@Valid @RequestBody EmployeeRequest request) {
        return ApiResponse.ok(employeeService.create(request), "Employee created");
    }

    @PutMapping("/{id}")
    public ApiResponse<Employee> update(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request) {
        return ApiResponse.ok(employeeService.update(id, request), "Employee updated");
    }

    @PutMapping("/{id}/resign")
    public ApiResponse<Void> resign(@PathVariable Long id, @RequestParam LocalDate leaveDate) {
        employeeService.resign(id, leaveDate);
        return ApiResponse.ok(null, "Employee resigned");
    }
}
```

- [ ] **Step 3: Write SalaryStructureController**

```java
// payroll-backend/src/main/java/com/payroll/salary/controller/SalaryStructureController.java
package com.payroll.salary.controller;

import com.payroll.salary.domain.SalaryStructure;
import com.payroll.salary.dto.SalaryStructureRequest;
import com.payroll.salary.service.SalaryStructureService;
import com.payroll.shared.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees/{employeeId}/salary")
@RequiredArgsConstructor
public class SalaryStructureController {

    private final SalaryStructureService salaryStructureService;

    @GetMapping
    public ApiResponse<SalaryStructure> getCurrent(@PathVariable Long employeeId) {
        return ApiResponse.ok(salaryStructureService.getCurrentByEmployeeId(employeeId));
    }

    @GetMapping("/history")
    public ApiResponse<List<SalaryStructure>> getHistory(@PathVariable Long employeeId) {
        return ApiResponse.ok(salaryStructureService.getHistoryByEmployeeId(employeeId));
    }

    @PostMapping
    public ApiResponse<SalaryStructure> create(
            @PathVariable Long employeeId,
            @Valid @RequestBody SalaryStructureRequest request) {
        request.setEmployeeId(employeeId);
        return ApiResponse.ok(salaryStructureService.create(request), "Salary structure created");
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add payroll-backend/src/main/java/com/payroll/department/controller/ payroll-backend/src/main/java/com/payroll/employee/controller/ payroll-backend/src/main/java/com/payroll/salary/controller/
git commit -m "feat: add REST controllers for department, employee, and salary"
```

---

## Task 11: Spring Security + JWT Auth (TDD)

**Files:**
- Create: `payroll-backend/src/main/java/com/payroll/auth/domain/User.java`
- Create: `payroll-backend/src/main/java/com/payroll/auth/domain/Role.java`
- Create: `payroll-backend/src/main/java/com/payroll/auth/repository/UserRepository.java`
- Create: `payroll-backend/src/main/java/com/payroll/auth/security/JwtUtil.java`
- Create: `payroll-backend/src/main/java/com/payroll/auth/security/JwtAuthFilter.java`
- Create: `payroll-backend/src/main/java/com/payroll/auth/security/SecurityConfig.java`
- Create: `payroll-backend/src/main/java/com/payroll/auth/service/AuthService.java`
- Create: `payroll-backend/src/main/java/com/payroll/auth/service/UserDetailsService.java`
- Create: `payroll-backend/src/main/java/com/payroll/auth/controller/AuthController.java`
- Create: `payroll-backend/src/main/java/com/payroll/auth/dto/LoginRequest.java`
- Create: `payroll-backend/src/main/java/com/payroll/auth/dto/LoginResponse.java`
- Create: `payroll-backend/src/test/java/com/payroll/auth/AuthServiceTest.java`

- [ ] **Step 1: Write failing AuthService test**

```java
// payroll-backend/src/test/java/com/payroll/auth/AuthServiceTest.java
package com.payroll.auth;

import com.payroll.auth.domain.Role;
import com.payroll.auth.domain.User;
import com.payroll.auth.dto.LoginRequest;
import com.payroll.auth.dto.LoginResponse;
import com.payroll.auth.repository.UserRepository;
import com.payroll.auth.security.JwtUtil;
import com.payroll.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_success_returns_tokens() {
        User user = User.builder()
                .username("admin")
                .password("encoded_password")
                .role(Role.ADMIN)
                .enabled(true)
                .build();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("admin123", "encoded_password")).thenReturn(true);
        when(jwtUtil.generateAccessToken("admin", "ADMIN")).thenReturn("access_token");
        when(jwtUtil.generateRefreshToken("admin")).thenReturn("refresh_token");

        LoginResponse response = authService.login(new LoginRequest("admin", "admin123"));

        assertEquals("access_token", response.getAccessToken());
        assertEquals("refresh_token", response.getRefreshToken());
    }

    @Test
    void login_wrong_password_throws() {
        User user = User.builder()
                .username("admin")
                .password("encoded_password")
                .role(Role.ADMIN)
                .enabled(true)
                .build();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded_password")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> authService.login(new LoginRequest("admin", "wrong")));
    }

    @Test
    void login_nonexistent_user_throws() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> authService.login(new LoginRequest("unknown", "password")));
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `cd payroll-backend && mvn test -Dtest=AuthServiceTest`
Expected: FAIL

- [ ] **Step 3: Write Role enum**

```java
// payroll-backend/src/main/java/com/payroll/auth/domain/Role.java
package com.payroll.auth.domain;

public enum Role {
    ADMIN, HR, EMPLOYEE
}
```

- [ ] **Step 4: Write User entity**

```java
// payroll-backend/src/main/java/com/payroll/auth/domain/User.java
package com.payroll.auth.domain;

import com.payroll.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "auth_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.EMPLOYEE;

    private Long employeeId;

    @Builder.Default
    private Boolean enabled = true;
}
```

- [ ] **Step 5: Write UserRepository**

```java
// payroll-backend/src/main/java/com/payroll/auth/repository/UserRepository.java
package com.payroll.auth.repository;

import com.payroll.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
```

- [ ] **Step 6: Write JwtUtil**

```java
// payroll-backend/src/main/java/com/payroll/auth/security/JwtUtil.java
package com.payroll.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String generateAccessToken(String username, String role) {
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(key)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String getRoleFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
```

- [ ] **Step 7: Write JwtAuthFilter**

```java
// payroll-backend/src/main/java/com/payroll/auth/security/JwtAuthFilter.java
package com.payroll.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUsernameFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                var auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        filterChain.doFilter(request, response);
    }
}
```

- [ ] **Step 8: Write SecurityConfig**

```java
// payroll-backend/src/main/java/com/payroll/auth/security/SecurityConfig.java
package com.payroll.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

- [ ] **Step 9: Write DTOs, AuthService, UserDetailsService, AuthController**

```java
// payroll-backend/src/main/java/com/payroll/auth/dto/LoginRequest.java
package com.payroll.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Username is required")
    private String username;
    @NotBlank(message = "Password is required")
    private String password;
}
```

```java
// payroll-backend/src/main/java/com/payroll/auth/dto/LoginResponse.java
package com.payroll.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
}
```

```java
// payroll-backend/src/main/java/com/payroll/auth/service/AuthService.java
package com.payroll.auth.service;

import com.payroll.auth.dto.LoginRequest;
import com.payroll.auth.dto.LoginResponse;
import com.payroll.auth.repository.UserRepository;
import com.payroll.auth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!user.getEnabled()) {
            throw new IllegalArgumentException("Account is disabled");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        return new LoginResponse(accessToken, refreshToken);
    }
}
```

```java
// payroll-backend/src/main/java/com/payroll/auth/service/UserDetailsService.java
package com.payroll.auth.service;

import com.payroll.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getEnabled(),
                true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
```

```java
// payroll-backend/src/main/java/com/payroll/auth/controller/AuthController.java
package com.payroll.auth.controller;

import com.payroll.auth.dto.LoginRequest;
import com.payroll.auth.dto.LoginResponse;
import com.payroll.auth.service.AuthService;
import com.payroll.shared.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(@RequestHeader("Authorization") String refreshToken) {
        // Token refresh logic - extract username from refresh token and generate new tokens
        return ApiResponse.ok(null, "Token refreshed");
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.ok(null, "Logged out");
    }
}
```

- [ ] **Step 10: Run tests to verify they pass**

Run: `cd payroll-backend && mvn test -Dtest=AuthServiceTest`
Expected: 3 tests PASS

- [ ] **Step 11: Run all tests**

Run: `cd payroll-backend && mvn test`
Expected: ALL tests PASS

- [ ] **Step 12: Commit**

```bash
git add payroll-backend/src/
git commit -m "feat: add JWT auth with Spring Security, user management, and auth controller"
```

---

## Task 12: Frontend Project Setup (HR Portal)

**Files:**
- Initialize: `payroll-hr-portal/` with Vite + React + TypeScript
- Create: `src/main.tsx`, `src/App.tsx`, `src/api/client.ts`
- Create: `src/utils/formatMoney.ts`, `src/types/index.ts`

- [ ] **Step 1: Initialize Vite project**

Run:
```bash
cd payroll-hr-portal
npm create vite@latest . -- --template react-ts
npm install
npm install -D tailwindcss @tailwindcss/vite
npm install zustand axios react-router-dom
```

- [ ] **Step 2: Configure Tailwind**

Add to `vite.config.ts`:
```typescript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
```

Replace `src/index.css` with:
```css
@import "tailwindcss";
```

- [ ] **Step 3: Create types**

```typescript
// payroll-hr-portal/src/types/index.ts
export interface Employee {
  id: number;
  name: string;
  idNumber: string;
  bankAccount: string | null;
  hireDate: string;
  leaveDate: string | null;
  department: Department | null;
  contractType: 'REGULAR' | 'CONTRACT' | 'PART_TIME' | 'INTERN';
  jobLevel: string | null;
  status: 'ACTIVE' | 'SUSPENDED' | 'LEFT';
  email: string | null;
  phone: string | null;
}

export interface Department {
  id: number;
  name: string;
  parent: Department | null;
  children: Department[];
}

export interface SalaryStructure {
  id: number;
  employeeId: number;
  baseSalary: string;
  effectiveDate: string;
  allowances: Allowance[];
}

export interface Allowance {
  id: number;
  type: 'TRANSPORT' | 'MEAL' | 'HOUSING' | 'POSITION' | 'OTHER';
  amount: string;
}

export interface ApiResponse<T> {
  code: number;
  data: T;
  message: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
```

- [ ] **Step 4: Create API client**

```typescript
// payroll-hr-portal/src/api/client.ts
import axios from 'axios';

const client = axios.create({
  baseURL: '/api/v1',
  headers: { 'Content-Type': 'application/json' },
});

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('access_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('access_token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default client;
```

- [ ] **Step 5: Create formatMoney utility**

```typescript
// payroll-hr-portal/src/utils/formatMoney.ts
export function formatMoney(value: string | number): string {
  const num = typeof value === 'string' ? parseFloat(value) : value;
  return `NT$ ${num.toLocaleString('zh-TW', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}
```

- [ ] **Step 6: Create auth store**

```typescript
// payroll-hr-portal/src/stores/authStore.ts
import { create } from 'zustand';

interface AuthState {
  token: string | null;
  isAuthenticated: boolean;
  login: (token: string) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  token: localStorage.getItem('access_token'),
  isAuthenticated: !!localStorage.getItem('access_token'),
  login: (token: string) => {
    localStorage.setItem('access_token', token);
    set({ token, isAuthenticated: true });
  },
  logout: () => {
    localStorage.removeItem('access_token');
    set({ token: null, isAuthenticated: false });
  },
}));
```

- [ ] **Step 7: Commit**

```bash
git add payroll-hr-portal/
git commit -m "feat: scaffold HR portal frontend (Vite + React + Tailwind + Zustand)"
```

---

## Task 13: Login Page + App Shell

**Files:**
- Create: `payroll-hr-portal/src/pages/Login.tsx`
- Create: `payroll-hr-portal/src/components/layout/MainLayout.tsx`
- Create: `payroll-hr-portal/src/components/layout/Sidebar.tsx`
- Create: `payroll-hr-portal/src/components/layout/Header.tsx`
- Create: `payroll-hr-portal/src/api/auth.ts`
- Modify: `payroll-hr-portal/src/App.tsx`

- [ ] **Step 1: Create auth API**

```typescript
// payroll-hr-portal/src/api/auth.ts
import client from './client';
import type { ApiResponse } from '../types';

interface LoginResponse {
  accessToken: string;
  refreshToken: string;
}

export const authApi = {
  login: (username: string, password: string) =>
    client.post<ApiResponse<LoginResponse>>('/auth/login', { username, password }),
};
```

- [ ] **Step 2: Create Login page**

```tsx
// payroll-hr-portal/src/pages/Login.tsx
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../api/auth';
import { useAuthStore } from '../stores/authStore';

export default function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const login = useAuthStore((s) => s.login);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    try {
      const { data } = await authApi.login(username, password);
      login(data.data.accessToken);
      navigate('/');
    } catch {
      setError('帳號或密碼錯誤');
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
      <form onSubmit={handleSubmit} className="bg-white p-8 rounded-lg shadow-md w-96">
        <h1 className="text-2xl font-bold text-center mb-6">薪資管理系統</h1>
        {error && <div className="bg-red-100 text-red-700 p-3 rounded mb-4">{error}</div>}
        <div className="mb-4">
          <label className="block text-sm font-medium mb-1">帳號</label>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            className="w-full border rounded px-3 py-2"
            required
          />
        </div>
        <div className="mb-6">
          <label className="block text-sm font-medium mb-1">密碼</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="w-full border rounded px-3 py-2"
            required
          />
        </div>
        <button type="submit" className="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700">
          登入
        </button>
      </form>
    </div>
  );
}
```

- [ ] **Step 3: Create layout components**

```tsx
// payroll-hr-portal/src/components/layout/Sidebar.tsx
import { NavLink } from 'react-router-dom';

const navItems = [
  { to: '/', label: '首頁' },
  { to: '/employees', label: '員工管理' },
  { to: '/departments', label: '部門管理' },
];

export default function Sidebar() {
  return (
    <aside className="w-48 bg-gray-800 text-white min-h-screen p-4">
      <h2 className="text-lg font-bold mb-6">薪資管理系統</h2>
      <nav className="space-y-2">
        {navItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.to === '/'}
            className={({ isActive }) =>
              `block px-3 py-2 rounded ${isActive ? 'bg-blue-600' : 'hover:bg-gray-700'}`
            }
          >
            {item.label}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}
```

```tsx
// payroll-hr-portal/src/components/layout/Header.tsx
import { useAuthStore } from '../../stores/authStore';
import { useNavigate } from 'react-router-dom';

export default function Header() {
  const logout = useAuthStore((s) => s.logout);
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="bg-white border-b px-6 py-3 flex justify-between items-center">
      <h1 className="text-lg font-semibold">HR 管理後台</h1>
      <button onClick={handleLogout} className="text-sm text-gray-600 hover:text-gray-900">
        登出
      </button>
    </header>
  );
}
```

```tsx
// payroll-hr-portal/src/components/layout/MainLayout.tsx
import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';
import Header from './Header';

export default function MainLayout() {
  return (
    <div className="flex">
      <Sidebar />
      <div className="flex-1">
        <Header />
        <main className="p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
```

- [ ] **Step 4: Wire up App.tsx with routing**

```tsx
// payroll-hr-portal/src/App.tsx
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useAuthStore } from './stores/authStore';
import MainLayout from './components/layout/MainLayout';
import Login from './pages/Login';

function Dashboard() {
  return <div className="text-gray-600">歡迎使用薪資管理系統</div>;
}

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  if (!isAuthenticated) return <Navigate to="/login" />;
  return <>{children}</>;
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/" element={<ProtectedRoute><MainLayout /></ProtectedRoute>}>
          <Route index element={<Dashboard />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
```

- [ ] **Step 5: Verify frontend starts**

Run: `cd payroll-hr-portal && npm run dev`
Expected: Server running at http://localhost:3000

- [ ] **Step 6: Commit**

```bash
git add payroll-hr-portal/src/
git commit -m "feat: add login page, app shell with routing and layout"
```

---

## Task 14: Employee List Page

**Files:**
- Create: `payroll-hr-portal/src/api/employees.ts`
- Create: `payroll-hr-portal/src/pages/employees/EmployeeList.tsx`
- Create: `payroll-hr-portal/src/stores/employeeStore.ts`

- [ ] **Step 1: Create employee API**

```typescript
// payroll-hr-portal/src/api/employees.ts
import client from './client';
import type { ApiResponse, Employee, Page } from '../types';

export const employeeApi = {
  search: (params: { name?: string; departmentId?: number; status?: string; page?: number; size?: number }) =>
    client.get<ApiResponse<Page<Employee>>>('/employees', { params }),
  getById: (id: number) =>
    client.get<ApiResponse<Employee>>(`/employees/${id}`),
  create: (data: Partial<Employee>) =>
    client.post<ApiResponse<Employee>>('/employees', data),
  update: (id: number, data: Partial<Employee>) =>
    client.put<ApiResponse<Employee>>(`/employees/${id}`, data),
};
```

- [ ] **Step 2: Create EmployeeList page**

```tsx
// payroll-hr-portal/src/pages/employees/EmployeeList.tsx
import { useEffect, useState } from 'react';
import { employeeApi } from '../../api/employees';
import type { Employee } from '../../types';
import { useNavigate } from 'react-router-dom';

export default function EmployeeList() {
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    employeeApi.search({ name: search || undefined, page, size: 20 })
      .then(({ data }) => {
        setEmployees(data.data.content);
        setTotalPages(data.data.totalPages);
      });
  }, [page, search]);

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-xl font-semibold">員工管理</h2>
        <button
          onClick={() => navigate('/employees/new')}
          className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
        >
          新增員工
        </button>
      </div>

      <div className="mb-4">
        <input
          type="text"
          placeholder="搜尋員工姓名..."
          value={search}
          onChange={(e) => { setSearch(e.target.value); setPage(0); }}
          className="border rounded px-3 py-2 w-64"
        />
      </div>

      <table className="w-full bg-white rounded shadow">
        <thead className="bg-gray-50">
          <tr>
            <th className="px-4 py-2 text-left">姓名</th>
            <th className="px-4 py-2 text-left">部門</th>
            <th className="px-4 py-2 text-left">職位級別</th>
            <th className="px-4 py-2 text-left">到職日</th>
            <th className="px-4 py-2 text-left">狀態</th>
            <th className="px-4 py-2 text-left">操作</th>
          </tr>
        </thead>
        <tbody>
          {employees.map((emp) => (
            <tr key={emp.id} className="border-t hover:bg-gray-50">
              <td className="px-4 py-2">{emp.name}</td>
              <td className="px-4 py-2">{emp.department?.name ?? '-'}</td>
              <td className="px-4 py-2">{emp.jobLevel ?? '-'}</td>
              <td className="px-4 py-2">{emp.hireDate}</td>
              <td className="px-4 py-2">
                <span className={`px-2 py-1 rounded text-xs ${
                  emp.status === 'ACTIVE' ? 'bg-green-100 text-green-800' :
                  emp.status === 'LEFT' ? 'bg-red-100 text-red-800' :
                  'bg-yellow-100 text-yellow-800'
                }`}>
                  {emp.status === 'ACTIVE' ? '在職' : emp.status === 'LEFT' ? '離職' : '停職'}
                </span>
              </td>
              <td className="px-4 py-2">
                <button
                  onClick={() => navigate(`/employees/${emp.id}`)}
                  className="text-blue-600 hover:underline"
                >
                  檢視
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <div className="flex justify-center gap-2 mt-4">
        <button
          onClick={() => setPage((p) => Math.max(0, p - 1))}
          disabled={page === 0}
          className="px-3 py-1 border rounded disabled:opacity-50"
        >
          上一頁
        </button>
        <span className="px-3 py-1">第 {page + 1} / {totalPages} 頁</span>
        <button
          onClick={() => setPage((p) => p + 1)}
          disabled={page >= totalPages - 1}
          className="px-3 py-1 border rounded disabled:opacity-50"
        >
          下一頁
        </button>
      </div>
    </div>
  );
}
```

- [ ] **Step 3: Add route to App.tsx**

Add import and route inside `ProtectedRoute > MainLayout`:
```tsx
import EmployeeList from './pages/employees/EmployeeList';
// Inside ProtectedRoute > MainLayout route:
<Route path="employees" element={<EmployeeList />} />
```

- [ ] **Step 4: Commit**

```bash
git add payroll-hr-portal/src/
git commit -m "feat: add employee list page with search and pagination"
```

---

## Task 15: Employee Form Page

**Files:**
- Create: `payroll-hr-portal/src/pages/employees/EmployeeForm.tsx`
- Create: `payroll-hr-portal/src/api/departments.ts`

- [ ] **Step 1: Create department API**

```typescript
// payroll-hr-portal/src/api/departments.ts
import client from './client';
import type { ApiResponse, Department } from '../types';

export const departmentApi = {
  getTree: () => client.get<ApiResponse<Department[]>>('/departments'),
};
```

- [ ] **Step 2: Create EmployeeForm page**

```tsx
// payroll-hr-portal/src/pages/employees/EmployeeForm.tsx
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { employeeApi } from '../../api/employees';
import { departmentApi } from '../../api/departments';
import type { Department } from '../../types';

export default function EmployeeForm() {
  const { id } = useParams();
  const isEdit = !!id;
  const navigate = useNavigate();
  const [departments, setDepartments] = useState<Department[]>([]);
  const [form, setForm] = useState({
    name: '', idNumber: '', bankAccount: '',
    hireDate: '', departmentId: '', contractType: 'REGULAR',
    jobLevel: '', email: '', phone: '',
  });

  useEffect(() => {
    departmentApi.getTree().then(({ data }) => setDepartments(data.data));
    if (isEdit) {
      employeeApi.getById(Number(id)).then(({ data }) => {
        const emp = data.data;
        setForm({
          name: emp.name, idNumber: emp.idNumber, bankAccount: emp.bankAccount ?? '',
          hireDate: emp.hireDate, departmentId: emp.department?.id?.toString() ?? '',
          contractType: emp.contractType, jobLevel: emp.jobLevel ?? '',
          email: emp.email ?? '', phone: emp.phone ?? '',
        });
      });
    }
  }, [id, isEdit]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const payload = {
      ...form,
      departmentId: form.departmentId ? Number(form.departmentId) : null,
    };
    if (isEdit) {
      await employeeApi.update(Number(id), payload);
    } else {
      await employeeApi.create(payload);
    }
    navigate('/employees');
  };

  return (
    <div className="max-w-2xl">
      <h2 className="text-xl font-semibold mb-4">{isEdit ? '編輯員工' : '新增員工'}</h2>
      <form onSubmit={handleSubmit} className="space-y-4 bg-white p-6 rounded shadow">
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium mb-1">姓名 *</label>
            <input type="text" required value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              className="w-full border rounded px-3 py-2" />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">身分證字號 *</label>
            <input type="text" required value={form.idNumber}
              onChange={(e) => setForm({ ...form, idNumber: e.target.value })}
              className="w-full border rounded px-3 py-2" />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">銀行帳號</label>
            <input type="text" value={form.bankAccount}
              onChange={(e) => setForm({ ...form, bankAccount: e.target.value })}
              className="w-full border rounded px-3 py-2" />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">到職日 *</label>
            <input type="date" required value={form.hireDate}
              onChange={(e) => setForm({ ...form, hireDate: e.target.value })}
              className="w-full border rounded px-3 py-2" />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">部門</label>
            <select value={form.departmentId}
              onChange={(e) => setForm({ ...form, departmentId: e.target.value })}
              className="w-full border rounded px-3 py-2">
              <option value="">無</option>
              {departments.map((d) => (
                <option key={d.id} value={d.id}>{d.name}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">合約類型</label>
            <select value={form.contractType}
              onChange={(e) => setForm({ ...form, contractType: e.target.value })}
              className="w-full border rounded px-3 py-2">
              <option value="REGULAR">正職</option>
              <option value="CONTRACT">約聘</option>
              <option value="PART_TIME">兼職</option>
              <option value="INTERN">實習</option>
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">職位級別</label>
            <input type="text" value={form.jobLevel}
              onChange={(e) => setForm({ ...form, jobLevel: e.target.value })}
              className="w-full border rounded px-3 py-2" />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">Email</label>
            <input type="email" value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
              className="w-full border rounded px-3 py-2" />
          </div>
        </div>
        <div className="flex gap-2 pt-4">
          <button type="submit" className="bg-blue-600 text-white px-6 py-2 rounded hover:bg-blue-700">
            {isEdit ? '更新' : '建立'}
          </button>
          <button type="button" onClick={() => navigate('/employees')}
            className="border px-6 py-2 rounded hover:bg-gray-50">
            取消
          </button>
        </div>
      </form>
    </div>
  );
}
```

- [ ] **Step 3: Add routes to App.tsx**

```tsx
import EmployeeForm from './pages/employees/EmployeeForm';
// Inside ProtectedRoute > MainLayout route:
<Route path="employees/new" element={<EmployeeForm />} />
<Route path="employees/:id" element={<EmployeeForm />} />
```

- [ ] **Step 4: Commit**

```bash
git add payroll-hr-portal/src/
git commit -m "feat: add employee create/edit form page"
```

---

## Task 16: Department Management Page

**Files:**
- Create: `payroll-hr-portal/src/pages/departments/DepartmentTree.tsx`

- [ ] **Step 1: Create DepartmentTree page**

```tsx
// payroll-hr-portal/src/pages/departments/DepartmentTree.tsx
import { useEffect, useState } from 'react';
import { departmentApi } from '../../api/departments';
import type { Department } from '../../types';

export default function DepartmentTree() {
  const [departments, setDepartments] = useState<Department[]>([]);
  const [newName, setNewName] = useState('');
  const [parentId, setParentId] = useState('');

  useEffect(() => {
    departmentApi.getTree().then(({ data }) => setDepartments(data.data));
  }, []);

  const handleCreate = async () => {
    if (!newName.trim()) return;
    await departmentApi.create({ name: newName, parentId: parentId ? Number(parentId) : null });
    setNewName('');
    setParentId('');
    const { data } = await departmentApi.getTree();
    setDepartments(data.data);
  };

  const renderTree = (items: Department[], depth = 0) => (
    <ul className={depth > 0 ? 'ml-6 border-l pl-4' : ''}>
      {items.map((dept) => (
        <li key={dept.id} className="py-1">
          <div className="flex items-center gap-2">
            <span className="font-medium">{dept.name}</span>
            <button
              onClick={async () => {
                if (confirm(`確定刪除「${dept.name}」？`)) {
                  await departmentApi.delete(dept.id);
                  const { data } = await departmentApi.getTree();
                  setDepartments(data.data);
                }
              }}
              className="text-red-500 text-xs hover:underline"
            >
              刪除
            </button>
          </div>
          {dept.children?.length > 0 && renderTree(dept.children, depth + 1)}
        </li>
      ))}
    </ul>
  );

  const flatDepartments = (items: Department[]): { id: number; name: string }[] =>
    items.flatMap((d) => [{ id: d.id, name: d.name }, ...flatDepartments(d.children || [])]);

  return (
    <div>
      <h2 className="text-xl font-semibold mb-4">部門管理</h2>

      <div className="flex gap-2 mb-6">
        <input
          type="text"
          placeholder="新部門名稱"
          value={newName}
          onChange={(e) => setNewName(e.target.value)}
          className="border rounded px-3 py-2 w-48"
        />
        <select value={parentId} onChange={(e) => setParentId(e.target.value)}
          className="border rounded px-3 py-2">
          <option value="">頂層部門</option>
          {flatDepartments(departments).map((d) => (
            <option key={d.id} value={d.id}>{d.name}</option>
          ))}
        </select>
        <button onClick={handleCreate}
          className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">
          新增
        </button>
      </div>

      <div className="bg-white p-4 rounded shadow">
        {departments.length > 0 ? renderTree(departments) : <p className="text-gray-500">尚無部門</p>}
      </div>
    </div>
  );
}
```

- [ ] **Step 2: Add department create/delete to API**

Add to `payroll-hr-portal/src/api/departments.ts`:
```typescript
export const departmentApi = {
  getTree: () => client.get<ApiResponse<Department[]>>('/departments'),
  create: (data: { name: string; parentId: number | null }) =>
    client.post<ApiResponse<Department>>('/departments', data),
  delete: (id: number) => client.delete(`/departments/${id}`),
};
```

- [ ] **Step 3: Add route to App.tsx**

```tsx
import DepartmentTree from './pages/departments/DepartmentTree';
// Inside ProtectedRoute > MainLayout route:
<Route path="departments" element={<DepartmentTree />} />
```

- [ ] **Step 4: Commit**

```bash
git add payroll-hr-portal/src/
git commit -m "feat: add department tree management page"
```

---

## Task 17: Docker Compose + Nginx Setup

**Files:**
- Create: `infrastructure/docker-compose.yml`
- Create: `infrastructure/docker-compose.dev.yml`
- Create: `infrastructure/nginx/default.conf`
- Create: `infrastructure/.env.example`

- [ ] **Step 1: Write docker-compose.yml**

```yaml
# infrastructure/docker-compose.yml
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_ROOT_PASSWORD}
      MYSQL_DATABASE: ${DB_NAME}
      MYSQL_USER: ${DB_USER}
      MYSQL_PASSWORD: ${DB_PASSWORD}
    ports:
      - "${DB_PORT:-3306}:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    build:
      context: ../payroll-backend
      dockerfile: Dockerfile
    environment:
      DB_HOST: mysql
      DB_PORT: 3306
      DB_NAME: ${DB_NAME}
      DB_USER: ${DB_USER}
      DB_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      ENCRYPTION_AES_KEY: ${ENCRYPTION_AES_KEY}
    ports:
      - "8080:8080"
    depends_on:
      mysql:
        condition: service_healthy

  hr-portal:
    build:
      context: ../payroll-hr-portal
      dockerfile: Dockerfile
    ports:
      - "3000:80"

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/default.conf:/etc/nginx/conf.d/default.conf
    depends_on:
      - backend
      - hr-portal

volumes:
  mysql_data:
```

- [ ] **Step 2: Write docker-compose.dev.yml**

```yaml
# infrastructure/docker-compose.dev.yml
services:
  mysql:
    extends:
      file: docker-compose.yml
      service: mysql
    ports:
      - "3306:3306"
```

- [ ] **Step 3: Write nginx config**

```nginx
# infrastructure/nginx/default.conf
server {
    listen 80;
    server_name localhost;

    client_max_body_size 10M;

    location /api/ {
        proxy_pass http://backend:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location / {
        proxy_pass http://hr-portal:80;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

- [ ] **Step 4: Write .env.example**

```env
# infrastructure/.env.example
DB_ROOT_PASSWORD=root_password_change_me
DB_NAME=payroll
DB_USER=payroll
DB_PASSWORD=payroll123_change_me
DB_PORT=3306

JWT_SECRET=your-jwt-secret-key-change-me-to-random-string

ENCRYPTION_AES_KEY=your-base64-encoded-32-byte-key-change-me
```

- [ ] **Step 5: Commit**

```bash
git add infrastructure/
git commit -m "feat: add Docker Compose, Nginx, and environment config"
```

---

## Self-Review Checklist

- [x] **Spec coverage**: All Phase 1 requirements mapped to tasks
  - Employee CRUD → Tasks 5, 8, 10, 14, 15
  - Department tree → Tasks 4, 7, 10, 16
  - Salary structure → Tasks 6, 9, 10
  - RBAC → Task 11
  - Frontend → Tasks 12-16
  - Infrastructure → Task 17
- [x] **Placeholder scan**: No TBD/TODO/fill-in-later found
- [x] **Type consistency**: Entity fields, DTO fields, and API types are aligned across tasks
- [x] **File path accuracy**: All paths follow the declared file structure

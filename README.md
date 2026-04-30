# payroll-management-system

企業薪資管理系統，包含 HR 管理後台、員工自助服務入口、Spring Boot 計算引擎。

## Quick Start

1. **Read CLAUDE.md first** - Contains essential rules for Claude Code
2. Follow the pre-task compliance checklist before starting any work
3. Use proper module structure under `src/main/[language]/`
4. Commit after every completed task

## Project Structure

```
payroll-management-system/
├── CLAUDE.md                # Essential rules for Claude Code
├── README.md                # Project documentation
├── .gitignore               # Git ignore patterns
├── payroll-backend/         # Spring Boot 後端微服務 (API 閘道與計算引擎)
│   └── src/
├── payroll-hr-portal/       # React 前端 (HR 管理與結算儀表板)
│   └── src/
├── payroll-ess-portal/      # React 前端 (員工自助服務與請假)
│   └── src/
├── infrastructure/          # 部署與基礎設施配置
│   └── nginx/
├── docs/                    # 全局專案文件
└── openspec/                # SDD 規格與計畫存放區
```

## Tech Stack

- **Backend**: Spring Boot (Java 17+)
- **Frontend**: React + Vite + Tailwind CSS + Zustand
- **Infrastructure**: Docker Compose, Nginx

## Development Guidelines

- **Always search first** before creating new files
- **Extend existing** functionality rather than duplicating
- **Use Task agents** for operations >30 seconds
- **Single source of truth** for all functionality

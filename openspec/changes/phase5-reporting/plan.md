# Phase 5: 報表分析與支付 - 實作計畫

## 執行順序

### 第一批：後端基礎（並行準備）
1. **T1**: Apache POI 依賴
2. **T2-T5**: ReportService 四個查詢方法（payroll-summary, department-cost, overtime-trend, bank-transfer）
3. **T6**: Excel 匯出
4. **T7**: ReportController

### 第二批：前端
5. **T8**: API Client
6. **T9-T12**: 四個報表頁面
7. **T13**: Sidebar + 路由

### 第三批：驗證
8. **T14**: 編譯驗證

## 依賴關係
```
T1 → T6 (POI)
T2+T3+T4+T5 → T7 (Service 先行)
T7 → T8 (API 端點確定後才能寫前端 API)
T8 → T9+T10+T11+T12 (API Client 先行)
T9+T10+T11+T12 → T13 (頁面先行才能設路由)
T13 → T14
```

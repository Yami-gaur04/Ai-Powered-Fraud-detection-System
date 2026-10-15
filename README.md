# FraudShield — AI-Powered Fraud Detection & Security Intelligence System

An enterprise-grade fraud detection, security intelligence, and financial safety platform built with **Spring Boot 3**, **Java 17**, **Thymeleaf**, **Raw JDBC & JPA dual data layers**, **Jakarta Servlets & Filters**, and **Multithreaded Ingestion Pipelines**.

---

## 🌟 Key Highlights & Architecture

### 1. Dual-Persona Web Experience (Fully Aligned UI & Backend)
- **Client/User Portal (`/dashboard`, `/dashboard/{tab}`, `/user/dashboard`)**:
  - **Overview**: Real-time spending safety score (0–100 SVG circular ring gauge), dynamic risk insights, 30-day spending vs. flagged anomaly trend (Chart.js), quick protection actions, and AI assistant query copilot (`POST /dashboard/ai-query`).
  - **My Transactions (`/dashboard/transactions`)**: Searchable, filterable audit ledger with instant transaction status chips (`APPROVED`, `FLAGGED`, `BLOCKED`), risk badges, transaction drawer, and instant dispute triggers ("This was me" / "This wasn't me").
  - **Submit / Simulate Transaction (`/user/transactions/new`)**: Live simulation form with real-time risk gauge preview, validation, and detailed receipt breakdown (`/user/transaction-result`).
  - **Fraud & Security Alerts (`/dashboard/alerts`)**: Live polling alert center (`GET /user/alerts/poll`) for held/blocked transactions with one-click authorization or fraud dispute actions.
  - **Security & Protection (`/dashboard/security`)**: Real-time 2FA toggle, daily spending velocity limit controls, and risk sensitivity threshold sliders (`POST /dashboard/security/update`).
  - **Account Settings (`/dashboard/settings`)**: Profile updates, password rotation (`POST /dashboard/settings/password`), and full transaction audit log CSV export (`GET /dashboard/export?format=csv`).
- **Admin Command Center (`/admin/dashboard`)**:
  - **System Metrics**: Total volume, flagged rates, model accuracy, and real-time rule engine status cards with container-query auto-scaling.
  - **Interactive Analytics**: Monthly fraud volume bar chart, case resolution donut chart with centered count, anomaly timelines, and ML model performance benchmarks via `/api/charts/*`.
  - **Reports & Case Investigation (`/admin/reports`)**: Paginated case queue with search/filter/sort, case detail view (`/admin/reports/detail?id=`), and review actions (`CONFIRMED_FRAUD` / `FALSE_POSITIVE`).
  - **Algorithms (`/admin/algorithms`)**: Track model versions, review feedback loops, and trigger automated weight tuning (`POST /admin/algorithms/update`).
  - **Configuration (`/admin/config`)**: Real-time threshold adjustment (amount limit, velocity weights, location jump penalties) with server-side validation.

### 2. Dual Data Access: Raw JDBC DAO & Spring Data JPA
- **Centralised Connection Pool (`DataSourceProvider`)**:
  - Manages pooled **HikariCP** connections for raw JDBC operations without tightly coupling DAOs to Spring.
  - Connected to a persistent embedded H2 database in MySQL-compatibility mode (`jdbc:h2:file:./data/frauddb;MODE=MySQL;NON_KEYWORDS=USER;AUTO_SERVER=TRUE`).
  - Interactive database console available at [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:file:./data/frauddb`, User: `sa`).
- **Explicit JDBC Layer (`JdbcTransactionDao`, `JdbcUserDao`, `JdbcFraudCaseDao`)**:
  - Uses `PreparedStatement` with parameterized SQL for SQL injection prevention.
  - Explicit transaction control (`conn.setAutoCommit(false)`, `conn.commit()`, `conn.rollback()`).
  - Column-by-column `ResultSet` mapping and `Statement.RETURN_GENERATED_KEYS`.
  - Try-with-resources resource cleanup ensuring zero connection leaks.
- **Reproducible DDL & DML**: Self-contained [schema.sql](file:///src/main/resources/schema.sql) and [data.sql](file:///src/main/resources/data.sql).

### 3. Native Jakarta Servlets & Servlet Filters
- Registered alongside Spring Boot MVC using `ServletRegistrationBean`, `FilterRegistrationBean`, and `ServletListenerRegistrationBean`.
- **Servlets (`/servlet/**`)**:
  - `TransactionServlet` (`/servlet/transactions`)
  - `FraudCaseServlet` (`/servlet/fraud-cases`)
  - `DashboardStatsServlet` (`/servlet/dashboard-stats`)
  - `ConfigServlet` (`/servlet/config`)
  - `LoginServlet` (`/servlet/login`)
  - `LogoutServlet` (`/servlet/logout`)
- **Filters**:
  - `CharacterEncodingFilter` (`/*`, UTF-8 enforcement)
  - `RequestLoggingFilter` (`/servlet/*`, timing and request auditing)
  - `AuthenticationFilter` (`/servlet/*`, session & principal validation)
  - `AuthorizationFilter` (`/servlet/*`, administrative role enforcement)
- **Lifecycle Listener**: `AppServletContextListener` hooks into application initialization and destruction.

### 4. Advanced Concurrency, Multithreading & Collections
- **Thread Pool (`FraudScanService`)**: Fixed thread pool of 4 named worker threads for asynchronous transaction scanning (`scanAsync()`) returning `Future<Result>`.
- **Scheduled Background Tasks (`StatisticsRefreshTask`)**: `ScheduledExecutorService` executing recurring statistics caching every 60 seconds with resilient error handling.
- **Producer-Consumer Pipeline (`TransactionProcessingPipeline`)**: Bounded `ArrayBlockingQueue<Transaction>`, `ReentrantLock`, and worker threads for non-blocking ingestion.
- **Lock-Free Atomic Counters (`FraudMetricsCounter`)**: Uses `AtomicLong` and `AtomicInteger` for real-time transaction throughput and fraud totals.
- **Thread-Safe Cache (`ThreadSafeCache<K, V>`)**: Backed by `ConcurrentHashMap` with atomic `computeIfAbsent()` caching.
- **Diverse Collections**: Employs `HashSet` for $O(1)$ device/location lookups, `ArrayDeque` for velocity FIFO time windows, and `TreeMap` for chronological spending timelines.

### 5. UI Architecture, Responsiveness & Overflow-Proofing
- **Global Overflow-Proofing**: Border-box reset, `min-width: 0` on flex/grid children, `overflow-wrap: anywhere` on text, single-line ellipsis for badges, and horizontally scrolling table wrappers (`fs-table-wrap`) with sticky headers.
- **Adaptive KPI Metrics**: Container queries (`clamp(2rem, 11cqi, 3.5rem)`), tabular numerals, Indian currency formatting (`₹`, lakhs/crores compact notation), and runtime auto-fit script (`window.fitKpiNumbers`).
- **Unified Adaptive Sidebar**: 264px expanded / 76px collapsed rail with circular hairline toggle button, keyboard shortcut `[`, and mobile off-canvas drawer.
- **High-DPI Retina Charts**: High-DPI canvas scaling (`devicePixelRatio >= 2`), dark/light theme awareness, and responsive resize observers.

---

## 🚀 Quick Start & How to Run

### Prerequisites
- Java 17+
- Maven 3.9+ (or use included `mvnw.cmd` / `./mvnw`)

### Running Locally
1. Start the Spring Boot server:
   ```bash
   .\mvnw.cmd spring-boot:run
   ```
2. Open your browser and navigate to:
   [http://localhost:8080](http://localhost:8080)

3. (Optional) Access the H2 Database Console:
   - URL: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
   - JDBC URL: `jdbc:h2:file:./data/frauddb`
   - User Name: `sa`
   - Password: *(leave blank)*

### Running Automated Test Suite
To execute the comprehensive unit and integration test suite:
```bash
.\mvnw.cmd test
```
All 14 tests across rules, raw JDBC DAOs, service layer, and multithreading concurrency execute cleanly with **0 failures and 0 errors**.

### Running Dev-Only UI Overflow Detector
To inspect all User and Admin pages across responsive viewports (`390px`, `768px`, `1280px`, `1440px`, `1920px`, `2560px`) for document scroll, card boundary overflows, and button label wrapping:
```bash
# Using npm
npm run ui-overflow-check

# Or directly with Node / scripts:
node scripts/ui-overflow-check.js
# On Windows:
.\scripts\ui-overflow-check.cmd
# On Linux/macOS:
./scripts/ui-overflow-check
```
Ensures 0 horizontal scrollbars, 0 card overflows, and strictly single-line button labels across all breakpoints.

---

## 🔑 Demo Credentials

| Role | Email | Password | Default Redirect |
|---|---|---|---|
| **User (Demo)** | `demo@fraudshield.io` | `Demo@123` | `/dashboard/overview` |
| **System Admin** | `admin@fraud.com` | `Admin@123` | `/admin/dashboard` |

*(Note: Both accounts can also be auto-filled directly from the quick-demo buttons on the `/login` screen.)*

---

## 📡 Endpoints Summary

### Web Application Routes (Thymeleaf UI)
- `GET /login` — Secure login screen with quick demo auto-fill
- `GET /register` — User registration portal
- `GET /dashboard`, `GET /dashboard/{tab}` — Unified user portal (`overview`, `transactions`, `alerts`, `security`, `settings`)
- `GET /user/transactions/new` — Transaction simulator & real-time risk tester
- `POST /user/transactions/new` — Process simulated transaction
- `POST /dashboard/transactions/confirm` — User verifies flagged transaction ("This was me")
- `POST /dashboard/transactions/report` — User disputes unauthorized transaction ("This wasn't me")
- `POST /dashboard/security/update` — Update 2FA, daily limit, and risk sensitivity threshold
- `POST /dashboard/settings/profile` — Update user profile details
- `POST /dashboard/settings/password` — Rotate user account password
- `GET /dashboard/export?format=csv` — Export personal transactions to CSV
- `POST /dashboard/ai-query` — AI assistant query copilot
- `GET /user/alerts/poll` — Live polling alert badge & notification feed
- `GET /admin/dashboard` — Admin analytics & KPI monitoring
- `GET /admin/reports` — Fraud investigation reports & case queue
- `GET /admin/reports/detail?id={id}` — Single fraud case review view
- `POST /admin/reports/review` — Review fraud case (`CONFIRMED_FRAUD` / `FALSE_POSITIVE`)
- `GET /admin/reports/export?format=csv` — Export fraud cases to CSV
- `GET /admin/algorithms` — Model accuracy & version history
- `POST /admin/algorithms/update` — Trigger automated retraining & model activation
- `GET /admin/config` — Real-time detection configuration settings
- `POST /admin/config` — Update detection thresholds and scoring weights

### Chart.js Visualisation Endpoints (`/api/charts/**`)
- `GET /api/charts/user-trend?days=30` — Live 30-day user spending vs flagged anomaly trend
- `GET /api/charts/fraud-trend?months=6` — 6-month system-wide fraud case timeline
- `GET /api/charts/monthly-cases?months=6` — Monthly detected case volume bar chart
- `GET /api/charts/status-distribution` — Resolution status distribution (Donut chart)
- `GET /api/charts/algorithm-performance` — ML version accuracy comparison benchmarks

### Jakarta Servlet Endpoints (`/servlet/**`)
- `GET /servlet/transactions` — Authenticated user's transaction history
- `GET /servlet/fraud-cases?status=OPEN` — Administrative fraud cases list
- `GET /servlet/dashboard-stats` — System detection report statistics
- `GET /servlet/config` — Current detection thresholds and rule weights
- `POST /servlet/login` — Servlet-based JSON authentication establishing `HttpSession`
- `POST /servlet/logout` — Servlet-based session termination

### REST API Endpoints (`/api/**`)
- `POST /api/auth/register` — Public registration
- `POST /api/auth/login` — Public JWT authentication
- `POST /api/transactions` — Submit transaction (scored in real-time)
- `GET /api/transactions` — User transactions list
- `GET /api/transactions/alerts` — User fraud alerts

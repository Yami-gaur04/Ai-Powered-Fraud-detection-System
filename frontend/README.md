# FraudShield AI: React frontend

React 18 + Vite + React Router + Recharts. Talks to the Spring Boot backend in this project.

## Run
```bash
cd frontend
npm install
npm run dev          # http://localhost:5173
```
Backend must be running on http://localhost:8080 (CORS for localhost is already enabled in `SecurityConfig`).
To use another URL, edit `.env`:  `VITE_API_URL=http://localhost:8080/api`

Production build: `npm run build` (output in `dist/`).

## Sign in
* Admin: `admin@fraud.com` / `Admin@123` (created automatically by the backend). In dev mode the login page has a "fill default admin login" link.
* User: click **Create account**.

## What each role gets
| Spec (project #70) | Where |
|---|---|
| Admin: System Configuration (form for detection settings) | `/admin/config` thresholds, weights, limits, live score-zone preview and a "try your settings" simulator |
| Admin: Detection Monitoring | `/admin/cases` filter, search, sort, review (confirm fraud or false positive), CSV export |
| Admin: Detection Reports (graphical) | `/admin/reports` donut, bar, gauge and histogram charts |
| Admin: Algorithm Management (updating algorithms) | `/admin/algorithms` run an update from reviewed cases, version history |
| User: Transaction Monitoring | `/user/transactions` create a transaction, instant risk verdict, history table |
| User: Transaction Alerts | `/user/alerts` + bell notifications |
| Dashboards | `/admin` and `/user` (separate, role-protected) |

Extras: dark mode, keyboard shortcuts (press `?`), global search (Ctrl/Cmd+K), live bell with 30s polling, session expiry handling, CSV export, loading skeletons and error states, responsive layout.

## Folder structure
```
src/
  api/         client.js (fetch + JWT + errors), endpoints.js (one function per backend endpoint)
  context/     Auth, Theme, Toast, Search providers
  hooks/       useFetch (polling), useTable (sort + pagination), useUserData, useAdminData
  components/  ui/ (Badge, Card, Modal, ...), charts/, layout/ (AppShell, NotificationBell), modals/
  pages/       Login, SettingsPage, user/*, admin/*
  config/      nav.js, constants.js
  utils/       format.js, csv.js, device.js
  styles/      index.css (light + dark theme variables)
```

## Backend endpoints used
`POST /auth/login|register` · `GET|POST /transactions`, `GET /transactions/alerts` ·
`GET|PUT /admin/config` · `GET /admin/fraud-cases[?status=]`, `PUT /admin/fraud-cases/{id}/review` ·
`GET /admin/reports` · `GET /admin/algorithms`, `POST /admin/algorithms/update`

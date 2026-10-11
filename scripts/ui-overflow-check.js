#!/usr/bin/env node
/**
 * FraudShield Dev-Only UI Overflow Detector
 *
 * Inspects all pages for both User and Admin roles across responsive viewports:
 * [390, 768, 1280, 1440, 1920, 2560]
 *
 * Checks:
 * (a) Any element whose bounding box exceeds its parent card (.fs-card, .fs-kpi-card, .fs-kpi-strip)
 * (b) Any document-level horizontal scroll (scrollWidth > clientWidth)
 * (c) Any button, pill, or badge label that wraps onto two lines
 */

const { spawn } = require('child_process');
const http = require('http');
const fs = require('fs');
const path = require('path');

const VIEWPORTS = [
  { name: '390px (Mobile iPhone)', width: 390, height: 844 },
  { name: '768px (Tablet iPad)', width: 768, height: 1024 },
  { name: '1280px (Standard Laptop)', width: 1280, height: 800 },
  { name: '1440px (Desktop)', width: 1440, height: 900 },
  { name: '1920px (FHD Large)', width: 1920, height: 1080 },
  { name: '2560px (QHD / 4K)', width: 2560, height: 1440 }
];

const PAGES_USER = [
  '/dashboard/overview',
  '/dashboard/transactions',
  '/dashboard/alerts',
  '/dashboard/security',
  '/dashboard/settings'
];

const PAGES_ADMIN = [
  '/admin/dashboard',
  '/admin/reports',
  '/admin/algorithms',
  '/admin/config'
];

const BASE_URL = process.env.BASE_URL || 'http://localhost:8080';

// Find Chrome or Edge executable on Windows/Mac/Linux
function findBrowserExecutable() {
  const candidates = [
    'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe',
    'C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe',
    'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe',
    'C:\\Program Files\\Microsoft\\Edge\\Application\\msedge.exe',
    '/usr/bin/google-chrome',
    '/usr/bin/chromium-browser',
    '/usr/bin/chromium',
    '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome'
  ];
  for (const p of candidates) {
    if (fs.existsSync(p)) return p;
  }
  return null;
}

// Simple CDP Client over WebSocket
class CdpClient {
  constructor(wsUrl) {
    this.wsUrl = wsUrl;
    this.ws = null;
    this.msgId = 1;
    this.pending = new Map();
  }

  async connect() {
    return new Promise((resolve, reject) => {
      this.ws = new WebSocket(this.wsUrl);
      this.ws.onopen = () => resolve();
      this.ws.onerror = (e) => reject(e);
      this.ws.onmessage = (event) => {
        try {
          const res = JSON.parse(event.data);
          if (res.id && this.pending.has(res.id)) {
            const { resolve, reject } = this.pending.get(res.id);
            this.pending.delete(res.id);
            if (res.error) reject(new Error(res.error.message));
            else resolve(res.result);
          }
        } catch (err) {
          console.error('CDP message parse error:', err);
        }
      };
    });
  }

  send(method, params = {}) {
    return new Promise((resolve, reject) => {
      const id = this.msgId++;
      this.pending.set(id, { resolve, reject });
      this.ws.send(JSON.stringify({ id, method, params }));
    });
  }

  async close() {
    if (this.ws) {
      this.ws.close();
    }
  }
}

function sleep(ms) {
  return new Promise((r) => setTimeout(r, ms));
}

// In-page overflow inspection function
const inspectOverflowScript = `(() => {
  const issues = [];

  // (a) Document-level horizontal scroll
  const docScrollWidth = document.documentElement.scrollWidth;
  const docClientWidth = document.documentElement.clientWidth;
  if (docScrollWidth > docClientWidth + 1) {
    issues.push({
      type: 'DOCUMENT_HORIZONTAL_OVERFLOW',
      detail: \`Page has horizontal overflow: scrollWidth (\${docScrollWidth}px) > clientWidth (\${docClientWidth}px)\`
    });
  }

  // (b) Card bounding box overflow
  const cards = document.querySelectorAll('.fs-card, .fs-kpi-card, .fs-kpi-strip, .card');
  cards.forEach((card) => {
    const cardRect = card.getBoundingClientRect();
    if (cardRect.width === 0 || cardRect.height === 0) return;

    const children = card.querySelectorAll('*');
    children.forEach((child) => {
      // Ignore if element is inside an authorized horizontally scrolling container
      if (child.closest('.fs-table-wrap, .overflow-x-auto, [style*="overflow-x"]')) return;

      const style = window.getComputedStyle(child);
      if (style.position === 'absolute' || style.position === 'fixed' || style.display === 'none') return;
      if (child.classList.contains('fs-table-wrap') || child.classList.contains('overflow-x-auto')) return;

      const rect = child.getBoundingClientRect();
      // Tolerance of 3px for sub-pixel anti-aliasing
      if (rect.right > cardRect.right + 3 && child.scrollWidth > child.clientWidth) {
        issues.push({
          type: 'CARD_CHILD_OVERFLOW',
          selector: child.tagName.toLowerCase() + (child.className ? '.' + String(child.className).trim().split(/\\s+/).slice(0, 2).join('.') : ''),
          detail: \`Child (\${Math.round(rect.right)}px) overflows card (\${Math.round(cardRect.right)}px): "\${(child.innerText || '').slice(0, 30)}"\`
        });
      }
    });
  });

  // (c) Button, pill or badge wrapping onto two lines
  const interactivePills = document.querySelectorAll('.btn-primary, .btn-secondary, .badge-pill, .pill-button');
  interactivePills.forEach((el) => {
    const text = (el.innerText || '').trim();
    if (!text || el.offsetParent === null) return;

    const style = window.getComputedStyle(el);
    if (style.whiteSpace === 'nowrap') return;

    const range = document.createRange();
    range.selectNodeContents(el);
    const rects = Array.from(range.getClientRects()).filter((r) => r.width > 0 && r.height > 0);
    if (rects.length > 1) {
      const firstTop = Math.round(rects[0].top);
      const isMultiLine = rects.some((r) => Math.abs(r.top - firstTop) > 6);
      if (isMultiLine) {
        issues.push({
          type: 'BUTTON_LABEL_WRAPPED',
          text: text.slice(0, 35),
          detail: \`Button label wrapped onto multiple lines for text "\${text.slice(0, 30)}"\`
        });
      }
    }
  });

  return issues;
})()`;

async function main() {
  console.log('='.repeat(70));
  console.log('  FraudShield Dev-Only UI Overflow Detector');
  console.log('='.repeat(70));

  const browserPath = findBrowserExecutable();
  if (!browserPath) {
    console.error('Error: No Chrome or Edge executable found.');
    process.exit(1);
  }
  console.log(`Using Browser: ${browserPath}`);

  const port = 9333;
  const chromeProc = spawn(browserPath, [
    '--headless=new',
    `--remote-debugging-port=${port}`,
    '--disable-gpu',
    '--no-first-run',
    '--no-default-browser-check',
    '--disable-background-networking',
    '--disable-extensions'
  ]);

  let cdpUrl = null;
  // Poll for debugger url
  for (let i = 0; i < 20; i++) {
    await sleep(250);
    try {
      const res = await fetch(`http://127.0.0.1:${port}/json/version`);
      if (res.ok) {
        const data = await res.json();
        cdpUrl = data.webSocketDebuggerUrl;
        break;
      }
    } catch (_) {}
  }

  if (!cdpUrl) {
    console.error('Failed to connect to headless Chrome CDP.');
    chromeProc.kill();
    process.exit(1);
  }

  const cdp = new CdpClient(cdpUrl);
  await cdp.connect();
  console.log('Connected to CDP.\n');

  // Create new target/tab
  const { targetId } = await cdp.send('Target.createTarget', { url: 'about:blank' });
  let tabWs = null;
  const listRes = await fetch(`http://127.0.0.1:${port}/json/list`);
  const listData = await listRes.json();
  const targetInfo = listData.find((t) => t.id === targetId) || listData[0];
  tabWs = targetInfo?.webSocketDebuggerUrl;

  if (!tabWs) {
    throw new Error('Failed to locate page webSocketDebuggerUrl');
  }

  const tabCdp = new CdpClient(tabWs);
  await tabCdp.connect();

  await tabCdp.send('Page.enable');
  await tabCdp.send('DOM.enable');
  await tabCdp.send('Runtime.enable');

  let totalIssues = 0;

  async function loginAndCheck(roleName, credentials, pages) {
    console.log(`\n>>> Testing Role: ${roleName.toUpperCase()}`);
    console.log('-'.repeat(70));

    for (const vp of VIEWPORTS) {
      console.log(`\n  Viewport: ${vp.name} (${vp.width}x${vp.height})`);

      await tabCdp.send('Emulation.setDeviceMetricsOverride', {
        width: vp.width,
        height: vp.height,
        deviceScaleFactor: 1,
        mobile: vp.width <= 768
      });

      // Login
      await tabCdp.send('Page.navigate', { url: `${BASE_URL}/login` });
      await sleep(600);

      const loginCode = `(() => {
        const u = document.querySelector('input[name="username"]');
        const p = document.querySelector('input[name="password"]');
        const form = document.querySelector('form');
        if (u && p && form) {
          u.value = '${credentials.username}';
          p.value = '${credentials.password}';
          form.submit();
          return true;
        }
        return false;
      })()`;

      await tabCdp.send('Runtime.evaluate', { expression: loginCode });
      await sleep(800);

      // Visit each page
      for (const pagePath of pages) {
        const fullUrl = `${BASE_URL}${pagePath}`;
        await tabCdp.send('Page.navigate', { url: fullUrl });
        await sleep(500);

        // Run overflow check
        const evalRes = await tabCdp.send('Runtime.evaluate', {
          expression: inspectOverflowScript,
          returnByValue: true
        });

        const issues = evalRes.result?.value || [];
        if (issues.length === 0) {
          console.log(`    [PASS] ${pagePath}`);
        } else {
          console.log(`    [FAIL] ${pagePath} (${issues.length} issue(s) detected)`);
          issues.forEach((issue) => {
            console.log(`      * [${issue.type}] ${issue.detail}`);
            totalIssues++;
          });
        }
      }
    }
  }

  try {
    // 1. Check Demo User Pages
    await loginAndCheck('Demo User', { username: 'demo@fraudshield.io', password: 'Demo@123' }, PAGES_USER);

    // 2. Check Admin Pages
    await loginAndCheck('Admin Console', { username: 'admin@fraud.com', password: 'Admin@123' }, PAGES_ADMIN);

  } finally {
    await tabCdp.close();
    await cdp.close();
    chromeProc.kill();
  }

  console.log('\n' + '='.repeat(70));
  if (totalIssues === 0) {
    console.log('  SUCCESS: 0 overflow or label-wrapping issues found across all viewports!');
    console.log('='.repeat(70) + '\n');
    process.exit(0);
  } else {
    console.log(`  COMPLETED WITH ISSUES: Found ${totalIssues} issue(s). Please review above.`);
    console.log('='.repeat(70) + '\n');
    process.exit(1);
  }
}

main().catch((err) => {
  console.error('Fatal error running overflow check:', err);
  process.exit(1);
});

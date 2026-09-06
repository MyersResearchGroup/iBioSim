import { api } from '../api.js';
import { t } from '../i18n.js';
import { escapeHtml } from '../ui.js';

const charts = {};

export async function renderDashboard(lang) {
  document.getElementById('page-title').textContent = t('dashboard', lang);
  const content = document.getElementById('page-content');
  content.innerHTML = `<div class="text-slate-400">${t('loading', lang)}</div>`;

  const summary = await api.get('/dashboard/summary');
  const { kpis, byCategory, byLevel, bySector, trend, overdueMitigations } = summary;

  const kpiCard = (label, value, color) => `
    <div class="bg-[var(--surface)] border border-[var(--border)] rounded-xl p-5">
      <div class="text-sm text-slate-400">${label}</div>
      <div class="text-3xl font-bold mt-1" style="color:${color}">${value}</div>
    </div>`;

  content.innerHTML = `
    <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4 mb-6">
      ${kpiCard(t('total_risks', lang), kpis.total_risks, '#0ea5e9')}
      ${kpiCard(t('open_risks', lang), kpis.open_risks, '#f59e0b')}
      ${kpiCard('Mitigating', kpis.mitigating_risks, '#8b5cf6')}
      ${kpiCard('Closed', kpis.closed_risks, '#10b981')}
      ${kpiCard(t('top_risks', lang), kpis.top_risks, '#ef4444')}
      ${kpiCard(t('critical_high', lang), kpis.critical_or_high_risks, '#dc2626')}
    </div>
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
      <div class="bg-[var(--surface)] border border-[var(--border)] rounded-xl p-5">
        <h3 class="font-semibold mb-3">${t('risks_by_category', lang)}</h3>
        <canvas id="chart-category" height="220"></canvas>
      </div>
      <div class="bg-[var(--surface)] border border-[var(--border)] rounded-xl p-5">
        <h3 class="font-semibold mb-3">${t('risks_by_level', lang)}</h3>
        <canvas id="chart-level" height="220"></canvas>
      </div>
    </div>
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
      <div class="bg-[var(--surface)] border border-[var(--border)] rounded-xl p-5">
        <h3 class="font-semibold mb-3">Risk Identification Trend</h3>
        <canvas id="chart-trend" height="220"></canvas>
      </div>
      <div class="bg-[var(--surface)] border border-[var(--border)] rounded-xl p-5">
        <h3 class="font-semibold mb-3">Top Sectors by Risk Count</h3>
        <canvas id="chart-sector" height="220"></canvas>
      </div>
    </div>
    <div class="bg-[var(--surface)] border border-[var(--border)] rounded-xl p-5">
      <h3 class="font-semibold mb-3">Overdue Mitigation Plans</h3>
      ${overdueMitigations.length ? `
        <table class="min-w-full text-sm">
          <thead><tr class="text-start text-slate-400"><th class="py-1">Risk</th><th>Plan</th><th>Due</th></tr></thead>
          <tbody>${overdueMitigations.map((m) => `
            <tr class="border-t border-[var(--border)]">
              <td class="py-1">${escapeHtml(m.risk_code)}</td>
              <td>${escapeHtml(m.description)}</td>
              <td>${new Date(m.due_date).toLocaleDateString()}</td>
            </tr>`).join('')}</tbody>
        </table>` : `<p class="text-slate-400 text-sm">${t('no_data', lang)}</p>`}
    </div>
  `;

  const palette = ['#0ea5e9', '#8b5cf6', '#f59e0b', '#10b981', '#ef4444', '#6366f1', '#14b8a6', '#f43f5e', '#84cc16', '#a855f7'];

  destroyCharts();
  charts.category = new Chart(document.getElementById('chart-category'), {
    type: 'bar',
    data: {
      labels: byCategory.map((r) => (lang === 'ar' ? r.name_ar : (r.name_en || r.name_ar))),
      datasets: [{ label: 'Risks', data: byCategory.map((r) => r.count), backgroundColor: palette[0] }]
    },
    options: { responsive: true, plugins: { legend: { display: false } }, indexAxis: 'y' }
  });

  charts.level = new Chart(document.getElementById('chart-level'), {
    type: 'doughnut',
    data: {
      labels: byLevel.map((r) => r.level),
      datasets: [{ data: byLevel.map((r) => r.count), backgroundColor: palette }]
    },
    options: { responsive: true }
  });

  charts.trend = new Chart(document.getElementById('chart-trend'), {
    type: 'line',
    data: {
      labels: trend.map((r) => r.month),
      datasets: [{ label: 'New Risks', data: trend.map((r) => r.count), borderColor: palette[0], tension: 0.3, fill: false }]
    },
    options: { responsive: true }
  });

  charts.sector = new Chart(document.getElementById('chart-sector'), {
    type: 'bar',
    data: {
      labels: bySector.map((r) => r.name_ar),
      datasets: [{ label: 'Risks', data: bySector.map((r) => r.count), backgroundColor: palette[2] }]
    },
    options: { responsive: true, plugins: { legend: { display: false } } }
  });
}

function destroyCharts() {
  Object.values(charts).forEach((c) => c?.destroy());
}

import { api } from '../api.js';
import { t } from '../i18n.js';
import { renderDataTable } from '../components/dataTable.js';
import { escapeHtml, toast } from '../ui.js';
import { navigate } from '../router.js';
import { getCurrentUser } from '../api.js';

const STATUS_COLORS = { OPEN: 'bg-amber-100 text-amber-800', MITIGATING: 'bg-sky-100 text-sky-800', MONITORING: 'bg-violet-100 text-violet-800', CLOSED: 'bg-emerald-100 text-emerald-800' };
const LEVEL_COLORS = { 'مستوى 1': 'bg-red-100 text-red-800', 'مستوى 2': 'bg-orange-100 text-orange-800', 'مستوى 3': 'bg-yellow-100 text-yellow-800', 'مستوى 4': 'bg-green-100 text-green-800' };

export async function renderRisks(lang) {
  document.getElementById('page-title').textContent = t('risk_register', lang);
  const content = document.getElementById('page-content');
  const user = getCurrentUser();
  const canWrite = ['ADMIN', 'MANAGER', 'RISK_OWNER'].includes(user?.role_code);

  const [categories] = await Promise.all([api.get('/lookups/categories')]);

  content.innerHTML = `
    <div class="flex justify-between items-center mb-4">
      <div class="flex gap-2">
        <a href="${window.APP_CONFIG.API_BASE_URL}/risks/export/excel" target="_blank" class="px-3 py-2 text-sm rounded-lg border border-[var(--border)]">${t('export_excel', lang)}</a>
        <a href="${window.APP_CONFIG.API_BASE_URL}/risks/export/pdf" target="_blank" class="px-3 py-2 text-sm rounded-lg border border-[var(--border)]">${t('export_pdf', lang)}</a>
      </div>
      ${canWrite ? `<button id="new-risk-btn" class="px-4 py-2 text-sm rounded-lg bg-sky-600 text-white">+ ${t('new_risk', lang)}</button>` : ''}
    </div>
    <div id="dt-container"></div>
  `;

  const filtersHtml = `
    <select data-filter-key="status" class="border border-[var(--border)] rounded-lg px-2 py-2 text-sm bg-[var(--surface)]">
      <option value="">${t('status', lang)}: ${t('all', lang)}</option>
      ${['OPEN', 'MITIGATING', 'MONITORING', 'CLOSED'].map((s) => `<option value="${s}">${s}</option>`).join('')}
    </select>
    <select data-filter-key="categoryId" class="border border-[var(--border)] rounded-lg px-2 py-2 text-sm bg-[var(--surface)]">
      <option value="">${t('category', lang)}: ${t('all', lang)}</option>
      ${categories.map((c) => `<option value="${c.id}">${escapeHtml(lang === 'ar' ? c.name_ar : (c.name_en || c.name_ar))}</option>`).join('')}
    </select>
  `;

  const table = renderDataTable(document.getElementById('dt-container'), {
    t: (k) => t(k, lang),
    filtersHtml,
    columns: [
      { key: 'risk_code', label: t('risk_code', lang), sortable: true },
      { key: 'risk_statement', label: t('risk_statement', lang), render: (r) => `<span class="line-clamp-2">${escapeHtml(r.risk_statement)}</span>` },
      { key: 'category_name_ar', label: t('category', lang), render: (r) => escapeHtml(lang === 'ar' ? r.category_name_ar : (r.category_name_en || r.category_name_ar || '-')) },
      { key: 'status', label: t('status', lang), render: (r) => `<span class="px-2 py-1 rounded-full text-xs font-medium ${STATUS_COLORS[r.status] || ''}">${r.status}</span>` },
      { key: 'residual_risk_level', label: t('residual_level', lang), sortable: true, render: (r) => r.residual_risk_level ? `<span class="px-2 py-1 rounded-full text-xs font-medium ${LEVEL_COLORS[r.residual_risk_level] || ''}">${r.residual_risk_level}</span>` : '-' },
      { key: 'identification_date', label: t('identification_date', lang), sortable: true, render: (r) => new Date(r.identification_date).toLocaleDateString() }
    ],
    fetchPage: async (state) => {
      const params = new URLSearchParams({
        page: state.page, pageSize: state.pageSize,
        ...(state.sortBy ? { sortBy: state.sortBy, sortDir: state.sortDir } : {}),
        ...(state.search ? { search: state.search } : {}),
        ...Object.fromEntries(Object.entries(state.extraFilters).filter(([, v]) => v))
      });
      return api.get(`/risks?${params.toString()}`);
    },
    onRowAction: (cell, row) => {
      cell.innerHTML = `
        <button data-action="view" class="text-sky-600 text-xs font-medium me-2">${t('view', lang)}</button>
        ${canWrite ? `<button data-action="delete" class="text-rose-600 text-xs font-medium">${t('delete', lang)}</button>` : ''}
      `;
      cell.querySelector('[data-action="view"]').addEventListener('click', () => navigate(`/risks/${row.id}`));
      cell.querySelector('[data-action="delete"]')?.addEventListener('click', async () => {
        if (!confirm(t('delete', lang) + '?')) return;
        try {
          await api.del(`/risks/${row.id}`);
          toast('Deleted', 'success');
          table.reload();
        } catch (err) { toast(err.payload?.message || t('error_occurred', lang), 'error'); }
      });
    }
  });

  document.getElementById('new-risk-btn')?.addEventListener('click', () => navigate('/risks/new'));
}

import { api } from '../api.js';
import { t } from '../i18n.js';
import { renderDataTable } from '../components/dataTable.js';
import { escapeHtml } from '../ui.js';

export async function renderAuditLog(lang) {
  document.getElementById('page-title').textContent = t('audit_log', lang);
  const content = document.getElementById('page-content');
  content.innerHTML = `<div id="dt-container"></div>`;

  renderDataTable(document.getElementById('dt-container'), {
    t: (k) => t(k, lang),
    columns: [
      { key: 'created_at', label: 'Time', render: (r) => new Date(r.created_at).toLocaleString() },
      { key: 'user_name', label: 'User', render: (r) => escapeHtml(r.user_name || 'system') },
      { key: 'action', label: 'Action' },
      { key: 'entity_type', label: 'Entity' },
      { key: 'entity_id', label: 'Entity ID', render: (r) => escapeHtml(String(r.entity_id || '-')) }
    ],
    fetchPage: async (state) => {
      const params = new URLSearchParams({ page: state.page, pageSize: state.pageSize });
      return api.get(`/audit-logs?${params.toString()}`);
    }
  });
}

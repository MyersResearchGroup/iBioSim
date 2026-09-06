import { api } from '../api.js';
import { t } from '../i18n.js';
import { renderDataTable } from '../components/dataTable.js';
import { escapeHtml, toast, openModal, closeModal } from '../ui.js';

export async function renderUsers(lang) {
  document.getElementById('page-title').textContent = t('users', lang);
  const content = document.getElementById('page-content');
  content.innerHTML = `<div id="dt-container"></div>`;

  const roles = await api.get('/lookups/roles');

  const table = renderDataTable(document.getElementById('dt-container'), {
    t: (k) => t(k, lang),
    columns: [
      { key: 'full_name', label: t('full_name', lang) },
      { key: 'email', label: t('email', lang) },
      { key: 'role_name_ar', label: t('role', lang), render: (r) => escapeHtml(lang === 'ar' ? r.role_name_ar : r.role_name_en) },
      { key: 'is_active', label: t('status', lang), render: (r) => r.is_active ? '✅' : '⛔' }
    ],
    fetchPage: async (state) => {
      const params = new URLSearchParams({ page: state.page, pageSize: state.pageSize, ...(state.search ? { search: state.search } : {}) });
      return api.get(`/users?${params.toString()}`);
    },
    onRowAction: (cell, row) => {
      cell.innerHTML = `<button data-action="edit" class="text-sky-600 text-xs font-medium me-2">${t('edit', lang)}</button>
        <button data-action="toggle" class="text-amber-600 text-xs font-medium">${row.is_active ? 'Deactivate' : 'Activate'}</button>`;

      cell.querySelector('[data-action="edit"]').addEventListener('click', () => openEditModal(row, roles, lang, table));
      cell.querySelector('[data-action="toggle"]').addEventListener('click', async () => {
        try {
          await api.patch(`/users/${row.id}`, { is_active: !row.is_active });
          table.reload();
        } catch (err) { toast(err.payload?.message || t('error_occurred', lang), 'error'); }
      });
    }
  });
}

function openEditModal(row, roles, lang, table) {
  openModal(`
    <h3 class="font-semibold text-lg mb-4">${escapeHtml(row.full_name)}</h3>
    <div class="space-y-3">
      <div>
        <label class="text-sm font-medium">${t('role', lang)}</label>
        <select id="edit-role" class="mt-1 w-full border border-[var(--border)] rounded-lg px-3 py-2 bg-transparent">
          ${roles.map((r) => `<option value="${r.id}" ${r.code === row.role_code ? 'selected' : ''}>${escapeHtml(r.name_ar)}</option>`).join('')}
        </select>
      </div>
      <div>
        <label class="text-sm font-medium">${t('job_title', lang)}</label>
        <input id="edit-job-title" value="${escapeHtml(row.job_title || '')}" class="mt-1 w-full border border-[var(--border)] rounded-lg px-3 py-2 bg-transparent" />
      </div>
    </div>
    <div class="flex gap-2 mt-5">
      <button id="edit-save" class="flex-1 bg-sky-600 text-white rounded-lg py-2">${t('save', lang)}</button>
      <button id="edit-cancel" class="flex-1 border border-[var(--border)] rounded-lg py-2">${t('cancel', lang)}</button>
    </div>
  `);

  document.getElementById('edit-cancel').addEventListener('click', closeModal);
  document.getElementById('edit-save').addEventListener('click', async () => {
    try {
      await api.patch(`/users/${row.id}`, {
        role_id: Number(document.getElementById('edit-role').value),
        job_title: document.getElementById('edit-job-title').value
      });
      closeModal();
      toast('Saved', 'success');
      table.reload();
    } catch (err) { toast(err.payload?.message || t('error_occurred', lang), 'error'); }
  });
}

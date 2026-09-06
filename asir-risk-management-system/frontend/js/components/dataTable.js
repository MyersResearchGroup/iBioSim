import { escapeHtml } from '../ui.js';

/**
 * Renders a searchable/sortable/paginated table into `container`.
 * `fetchPage(state)` must return { data, total, page, pageSize }.
 * `columns`: [{ key, label, sortable, render(row) }]
 */
export function renderDataTable(container, { columns, fetchPage, filtersHtml = '', onRowAction, t }) {
  const state = { page: 1, pageSize: 10, search: '', sortBy: null, sortDir: 'DESC', extraFilters: {} };

  container.innerHTML = `
    <div class="flex flex-wrap items-center gap-3 mb-4">
      <input id="dt-search" type="text" placeholder="${t('search')}"
             class="border border-[var(--border)] rounded-lg px-3 py-2 text-sm w-64 bg-[var(--surface)]" />
      <div id="dt-filters" class="flex flex-wrap gap-2">${filtersHtml}</div>
    </div>
    <div class="overflow-x-auto rounded-xl border border-[var(--border)]">
      <table class="min-w-full text-sm">
        <thead class="bg-slate-100 dark:bg-slate-800">
          <tr>${columns.map((c) => `<th data-key="${c.key}" class="px-4 py-3 text-start font-semibold ${c.sortable ? 'cursor-pointer select-none' : ''}">${c.label}${c.sortable ? ' <span class=\"sort-indicator text-xs\"></span>' : ''}</th>`).join('')}
          <th class="px-4 py-3"></th></tr>
        </thead>
        <tbody id="dt-body"></tbody>
      </table>
    </div>
    <div class="flex items-center justify-between mt-4 text-sm">
      <div id="dt-summary"></div>
      <div class="flex items-center gap-2">
        <button id="dt-prev" class="px-3 py-1.5 rounded-lg border border-[var(--border)]">${t('previous')}</button>
        <span id="dt-page-info"></span>
        <button id="dt-next" class="px-3 py-1.5 rounded-lg border border-[var(--border)]">${t('next')}</button>
      </div>
    </div>
  `;

  const searchInput = container.querySelector('#dt-search');
  let searchTimer;
  searchInput.addEventListener('input', () => {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(() => { state.search = searchInput.value; state.page = 1; load(); }, 350);
  });

  container.querySelectorAll('thead th[data-key]').forEach((th) => {
    th.addEventListener('click', () => {
      const key = th.dataset.key;
      if (state.sortBy === key) state.sortDir = state.sortDir === 'ASC' ? 'DESC' : 'ASC';
      else { state.sortBy = key; state.sortDir = 'ASC'; }
      load();
    });
  });

  container.querySelector('#dt-prev').addEventListener('click', () => { if (state.page > 1) { state.page -= 1; load(); } });
  container.querySelector('#dt-next').addEventListener('click', () => { state.page += 1; load(); });

  container.querySelector('#dt-filters').addEventListener('change', (e) => {
    if (e.target.dataset.filterKey) {
      state.extraFilters[e.target.dataset.filterKey] = e.target.value || undefined;
      state.page = 1;
      load();
    }
  });

  async function load() {
    const body = container.querySelector('#dt-body');
    body.innerHTML = `<tr><td colspan="${columns.length + 1}" class="px-4 py-6 text-center text-slate-400">${t('loading')}</td></tr>`;

    const result = await fetchPage(state);
    const { data, total, page, pageSize } = result;

    if (!data.length) {
      body.innerHTML = `<tr><td colspan="${columns.length + 1}" class="px-4 py-6 text-center text-slate-400">${t('no_data')}</td></tr>`;
    } else {
      body.innerHTML = data.map((row) => `
        <tr class="border-t border-[var(--border)] hover:bg-slate-50 dark:hover:bg-slate-800/50">
          ${columns.map((c) => `<td class="px-4 py-3">${c.render ? c.render(row) : escapeHtml(row[c.key] ?? '')}</td>`).join('')}
          <td class="px-4 py-3 text-end whitespace-nowrap" data-row-actions></td>
        </tr>`).join('');

      [...body.querySelectorAll('tr')].forEach((tr, idx) => {
        const cell = tr.querySelector('[data-row-actions]');
        if (onRowAction) onRowAction(cell, data[idx]);
      });
    }

    const totalPages = Math.max(1, Math.ceil(total / pageSize));
    container.querySelector('#dt-summary').textContent = `${total} ${t('results')}`;
    container.querySelector('#dt-page-info').textContent = `${t('page')} ${page} ${t('of')} ${totalPages}`;
    container.querySelector('#dt-prev').disabled = page <= 1;
    container.querySelector('#dt-next').disabled = page >= totalPages;
  }

  load();
  return { reload: load, state };
}

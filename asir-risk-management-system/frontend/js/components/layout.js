import { t, applyDocumentDirection } from '../i18n.js';
import { getCurrentUser, clearSession } from '../api.js';
import { navigate } from '../router.js';
import { api } from '../api.js';

const NAV_ITEMS = [
  { path: '/dashboard', key: 'dashboard', icon: 'M3 13h8V3H3v10zm0 8h8v-6H3v6zm10 0h8V11h-8v10zm0-18v6h8V3h-8z', roles: null },
  { path: '/risks', key: 'risk_register', icon: 'M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2', roles: null },
  { path: '/users', key: 'users', icon: 'M17 20h5v-2a4 4 0 00-3-3.87M9 20H4v-2a4 4 0 013-3.87m6-4a4 4 0 10-4-4 4 4 0 004 4z', roles: ['ADMIN', 'MANAGER'] },
  { path: '/audit-log', key: 'audit_log', icon: 'M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2', roles: ['ADMIN', 'MANAGER'] },
  { path: '/settings', key: 'settings', icon: 'M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z', roles: null }
];

export function renderShell(activePath, lang) {
  const user = getCurrentUser();
  applyDocumentDirection(lang);

  const navHtml = NAV_ITEMS
    .filter((item) => !item.roles || item.roles.includes(user?.role_code))
    .map((item) => `
      <a href="#${item.path}" data-path="${item.path}"
         class="nav-link flex items-center gap-3 px-4 py-2.5 rounded-lg text-sm font-medium transition-colors ${activePath.startsWith(item.path) ? 'bg-sky-600 text-white' : 'text-slate-300 hover:bg-slate-700/60'}">
        <svg class="w-5 h-5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="${item.icon}"/></svg>
        <span>${t(item.key, lang)}</span>
      </a>`).join('');

  document.getElementById('app').innerHTML = `
    <div class="flex h-screen overflow-hidden bg-[var(--bg)] text-[var(--text)]">
      <aside class="w-64 shrink-0 bg-slate-900 text-white flex flex-col rtl:border-l ltr:border-r border-slate-800">
        <div class="p-5 border-b border-slate-800 flex items-center gap-3">
          <div class="w-9 h-9 rounded-full bg-sky-500 flex items-center justify-center font-bold">A</div>
          <div class="text-sm font-semibold leading-tight">${t('app_name', lang)}</div>
        </div>
        <nav class="flex-1 p-3 space-y-1 overflow-y-auto">${navHtml}</nav>
        <div class="p-4 border-t border-slate-800 text-xs text-slate-400">
          ${user ? `${user.full_name} · ${user.role_name_ar || user.role_code}` : ''}
        </div>
      </aside>
      <div class="flex-1 flex flex-col overflow-hidden">
        <header class="h-16 shrink-0 bg-[var(--surface)] border-b border-[var(--border)] flex items-center justify-between px-6">
          <div class="font-semibold" id="page-title"></div>
          <div class="flex items-center gap-3">
            <button id="notif-btn" class="relative p-2 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800">
              🔔<span id="notif-badge" class="hidden absolute -top-1 -right-1 bg-rose-600 text-white text-[10px] rounded-full px-1.5"></span>
            </button>
            <button id="lang-toggle" class="text-sm px-3 py-1.5 rounded-lg border border-[var(--border)]">${lang === 'ar' ? 'EN' : 'ع'}</button>
            <button id="logout-btn" class="text-sm px-3 py-1.5 rounded-lg bg-rose-600 text-white">${t('logout', lang)}</button>
          </div>
        </header>
        <main id="page-content" class="flex-1 overflow-y-auto p-6"></main>
      </div>
    </div>
    <div id="toast-container" class="fixed bottom-4 right-4 rtl:right-auto rtl:left-4 z-[60] w-80"></div>
    <div id="modal-root"></div>
  `;

  document.getElementById('logout-btn').addEventListener('click', async () => {
    try { await api.post('/auth/logout', { refreshToken: localStorage.getItem('erms_refresh_token') }); } catch { /* noop */ }
    clearSession();
    navigate('/login');
  });

  document.getElementById('lang-toggle').addEventListener('click', () => {
    const newLang = lang === 'ar' ? 'en' : 'ar';
    localStorage.setItem('lang', newLang);
    window.location.reload();
  });

  loadNotifications();
}

async function loadNotifications() {
  try {
    const { unreadCount } = await api.get('/notifications');
    const badge = document.getElementById('notif-badge');
    if (badge && unreadCount > 0) {
      badge.textContent = unreadCount > 9 ? '9+' : unreadCount;
      badge.classList.remove('hidden');
    }
  } catch { /* silent - notification bell is non-critical */ }
}

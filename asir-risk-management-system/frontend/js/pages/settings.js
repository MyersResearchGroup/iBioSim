import { api, getCurrentUser, setSession } from '../api.js';
import { t } from '../i18n.js';
import { escapeHtml, toast } from '../ui.js';

export async function renderSettings(lang) {
  document.getElementById('page-title').textContent = t('settings', lang);
  const content = document.getElementById('page-content');
  const user = await api.get('/users/me');
  setSession({ user });

  content.innerHTML = `
    <div class="max-w-xl space-y-6">
      <div class="bg-[var(--surface)] border border-[var(--border)] rounded-xl p-5">
        <h3 class="font-semibold mb-3">${t('profile', lang)}</h3>
        <div class="space-y-2 text-sm">
          <div class="flex justify-between"><span>${t('full_name', lang)}</span><b>${escapeHtml(user.full_name)}</b></div>
          <div class="flex justify-between"><span>${t('email', lang)}</span><b>${escapeHtml(user.email)}</b></div>
          <div class="flex justify-between"><span>${t('job_title', lang)}</span><b>${escapeHtml(user.job_title || '-')}</b></div>
          <div class="flex justify-between"><span>${t('role', lang)}</span><b>${escapeHtml(lang === 'ar' ? user.role_name_ar : user.role_name_en)}</b></div>
        </div>
      </div>

      <div class="bg-[var(--surface)] border border-[var(--border)] rounded-xl p-5">
        <h3 class="font-semibold mb-3">${t('change_password', lang)}</h3>
        <form id="pwd-form" class="space-y-3">
          <input required type="password" id="current-password" placeholder="${t('current_password', lang)}" class="w-full border border-[var(--border)] rounded-lg px-3 py-2 bg-transparent" />
          <input required type="password" id="new-password" minlength="8" placeholder="${t('new_password', lang)}" class="w-full border border-[var(--border)] rounded-lg px-3 py-2 bg-transparent" />
          <button class="bg-sky-600 text-white rounded-lg px-4 py-2 text-sm">${t('save', lang)}</button>
        </form>
      </div>
    </div>
  `;

  document.getElementById('pwd-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
      await api.post('/auth/change-password', {
        currentPassword: document.getElementById('current-password').value,
        newPassword: document.getElementById('new-password').value
      });
      toast('Password changed', 'success');
      e.target.reset();
    } catch (err) {
      toast(err.payload?.message || t('error_occurred', lang), 'error');
    }
  });
}

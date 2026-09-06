import { api, setSession } from '../api.js';
import { t, applyDocumentDirection } from '../i18n.js';
import { navigate } from '../router.js';
import { toast } from '../ui.js';

export function renderLogin() {
  const lang = localStorage.getItem('lang') || 'ar';
  applyDocumentDirection(lang);

  document.getElementById('app').innerHTML = `
    <div class="min-h-screen flex items-center justify-center bg-slate-950 px-4">
      <div class="w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl p-8 text-white">
        <div class="flex flex-col items-center mb-6">
          <div class="w-14 h-14 rounded-full bg-sky-500 flex items-center justify-center text-2xl font-bold mb-3">A</div>
          <h1 class="text-lg font-semibold text-center">${t('app_name', lang)}</h1>
        </div>
        <form id="login-form" class="space-y-4">
          <div>
            <label class="text-sm text-slate-300">${t('email', lang)}</label>
            <input required type="email" id="email" class="mt-1 w-full rounded-lg bg-slate-800 border border-slate-700 px-3 py-2" />
          </div>
          <div>
            <label class="text-sm text-slate-300">${t('password', lang)}</label>
            <input required type="password" id="password" class="mt-1 w-full rounded-lg bg-slate-800 border border-slate-700 px-3 py-2" />
          </div>
          <button class="w-full bg-sky-600 hover:bg-sky-500 transition-colors rounded-lg py-2.5 font-semibold">${t('login_button', lang)}</button>
        </form>
        <div class="text-center mt-4">
          <button id="lang-switch" class="text-sm text-slate-400 underline">${lang === 'ar' ? 'English' : 'العربية'}</button>
        </div>
        <p class="text-xs text-slate-500 mt-6 text-center">Demo: admin@asirhealth.sa / ChangeMe123!</p>
      </div>
    </div>
    <div id="toast-container" class="fixed bottom-4 right-4 rtl:right-auto rtl:left-4 z-[60] w-80"></div>
  `;

  document.getElementById('lang-switch').addEventListener('click', () => {
    localStorage.setItem('lang', lang === 'ar' ? 'en' : 'ar');
    renderLogin();
  });

  document.getElementById('login-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    try {
      const result = await api.post('/auth/login', { email, password });
      setSession(result);
      navigate('/dashboard');
    } catch (err) {
      toast(err.payload?.message || t('invalid_credentials', lang), 'error');
    }
  });
}

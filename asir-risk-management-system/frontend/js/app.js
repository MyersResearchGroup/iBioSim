import { registerRoute, startRouter, navigate } from './router.js';
import { getAccessToken, getCurrentUser } from './api.js';
import { renderShell } from './components/layout.js';
import { mountAiAssistant } from './components/aiAssistant.js';
import { renderLogin } from './pages/login.js';
import { renderDashboard } from './pages/dashboard.js';
import { renderRisks } from './pages/risks.js';
import { renderRiskForm } from './pages/riskForm.js';
import { renderUsers } from './pages/users.js';
import { renderSettings } from './pages/settings.js';
import { renderAuditLog } from './pages/auditLog.js';

function currentLang() {
  return localStorage.getItem('lang') || 'ar';
}

function requireAuth(path, render) {
  if (!getAccessToken()) {
    navigate('/login');
    return;
  }
  const lang = currentLang();
  renderShell(path, lang);
  mountAiAssistant(lang);
  render(lang);
}

registerRoute('/login', () => {
  if (getAccessToken()) { navigate('/dashboard'); return; }
  renderLogin();
});

registerRoute('/', () => navigate(getAccessToken() ? '/dashboard' : '/login'));
registerRoute('/dashboard', () => requireAuth('/dashboard', renderDashboard));
registerRoute('/risks', () => requireAuth('/risks', renderRisks));
registerRoute('/risks/:id', (params) => requireAuth('/risks', (lang) => renderRiskForm(lang, params.id)));
registerRoute('/users', () => {
  const user = getCurrentUser();
  if (!['ADMIN', 'MANAGER'].includes(user?.role_code)) { navigate('/dashboard'); return; }
  requireAuth('/users', renderUsers);
});
registerRoute('/settings', () => requireAuth('/settings', renderSettings));
registerRoute('/audit-log', () => {
  const user = getCurrentUser();
  if (!['ADMIN', 'MANAGER'].includes(user?.role_code)) { navigate('/dashboard'); return; }
  requireAuth('/audit-log', renderAuditLog);
});

startRouter(() => navigate('/dashboard'));

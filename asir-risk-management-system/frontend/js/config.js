// Single place to point the standalone frontend at a backend API base URL.
// Override at deploy time by editing this file (or templating it in the
// Docker image) — no build step required.
window.APP_CONFIG = {
  API_BASE_URL: window.__API_BASE_URL__ || 'http://localhost:4000/api',
  APP_NAME_AR: 'نظام إدارة المخاطر المؤسسية - تجمع عسير الصحي',
  APP_NAME_EN: 'Asir Health Cluster - Enterprise Risk Management System'
};

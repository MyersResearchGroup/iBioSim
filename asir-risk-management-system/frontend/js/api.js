const STORAGE_ACCESS = 'erms_access_token';
const STORAGE_REFRESH = 'erms_refresh_token';
const STORAGE_USER = 'erms_user';

export function getAccessToken() { return localStorage.getItem(STORAGE_ACCESS); }
export function getRefreshToken() { return localStorage.getItem(STORAGE_REFRESH); }
export function getCurrentUser() {
  const raw = localStorage.getItem(STORAGE_USER);
  return raw ? JSON.parse(raw) : null;
}
export function setSession({ user, accessToken, refreshToken }) {
  if (user) localStorage.setItem(STORAGE_USER, JSON.stringify(user));
  if (accessToken) localStorage.setItem(STORAGE_ACCESS, accessToken);
  if (refreshToken) localStorage.setItem(STORAGE_REFRESH, refreshToken);
}
export function clearSession() {
  localStorage.removeItem(STORAGE_ACCESS);
  localStorage.removeItem(STORAGE_REFRESH);
  localStorage.removeItem(STORAGE_USER);
}

let refreshPromise = null;

async function doRefresh() {
  const refreshToken = getRefreshToken();
  if (!refreshToken) throw new Error('NO_REFRESH_TOKEN');

  const res = await fetch(`${window.APP_CONFIG.API_BASE_URL}/auth/refresh`, {
    method: 'POST',
    headers: { 'content-type': 'application/json' },
    body: JSON.stringify({ refreshToken })
  });
  if (!res.ok) throw new Error('REFRESH_FAILED');
  const data = await res.json();
  setSession(data);
  return data.accessToken;
}

/**
 * Core fetch wrapper: attaches JWT, retries once on 401 via refresh token,
 * and normalizes error responses into thrown Error objects with .status/.payload.
 */
export async function apiFetch(path, { method = 'GET', body, headers = {}, isForm = false, raw = false } = {}) {
  const url = `${window.APP_CONFIG.API_BASE_URL}${path}`;
  const doFetch = async () => {
    const token = getAccessToken();
    const finalHeaders = { ...headers };
    if (token) finalHeaders.Authorization = `Bearer ${token}`;
    if (!isForm && body !== undefined) finalHeaders['content-type'] = 'application/json';

    return fetch(url, {
      method,
      headers: finalHeaders,
      body: body === undefined ? undefined : (isForm ? body : JSON.stringify(body))
    });
  };

  let res = await doFetch();

  if (res.status === 401 && getRefreshToken()) {
    try {
      refreshPromise = refreshPromise || doRefresh();
      await refreshPromise;
      refreshPromise = null;
      res = await doFetch();
    } catch {
      clearSession();
      window.location.hash = '#/login';
      throw new Error('SESSION_EXPIRED');
    }
  }

  if (raw) return res;

  if (res.status === 204) return null;

  const contentType = res.headers.get('content-type') || '';
  const data = contentType.includes('application/json') ? await res.json() : await res.text();

  if (!res.ok) {
    const err = new Error(data?.message || 'Request failed');
    err.status = res.status;
    err.payload = data;
    throw err;
  }
  return data;
}

export const api = {
  get: (path) => apiFetch(path),
  post: (path, body) => apiFetch(path, { method: 'POST', body }),
  patch: (path, body) => apiFetch(path, { method: 'PATCH', body }),
  del: (path) => apiFetch(path, { method: 'DELETE' })
};

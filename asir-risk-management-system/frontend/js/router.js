const routes = [];

export function registerRoute(pattern, handler) {
  routes.push({ pattern, handler });
}

function matchRoute(hash) {
  for (const route of routes) {
    const paramNames = [];
    const regexStr = route.pattern.replace(/:[^/]+/g, (m) => {
      paramNames.push(m.slice(1));
      return '([^/]+)';
    });
    const match = hash.match(new RegExp(`^${regexStr}$`));
    if (match) {
      const params = {};
      paramNames.forEach((name, i) => { params[name] = decodeURIComponent(match[i + 1]); });
      return { route, params };
    }
  }
  return null;
}

export function navigate(path) {
  window.location.hash = path;
}

export function startRouter(notFoundHandler) {
  const onChange = () => {
    const hash = window.location.hash.replace(/^#/, '') || '/';
    const matched = matchRoute(hash);
    if (matched) {
      matched.route.handler(matched.params);
    } else {
      notFoundHandler();
    }
  };
  window.addEventListener('hashchange', onChange);
  onChange();
}

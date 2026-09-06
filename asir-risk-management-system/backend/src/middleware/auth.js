const { verifyAccessToken } = require('../utils/jwt');

function authenticate(req, res, next) {
  const header = req.headers.authorization || '';
  const [scheme, token] = header.split(' ');

  if (scheme !== 'Bearer' || !token) {
    return res.status(401).json({ error: 'UNAUTHENTICATED', message: 'Missing or malformed Authorization header' });
  }

  try {
    const payload = verifyAccessToken(token);
    req.user = { id: payload.sub, role: payload.role, organizationId: payload.org };
    return next();
  } catch (err) {
    return res.status(401).json({ error: 'UNAUTHENTICATED', message: 'Invalid or expired token' });
  }
}

module.exports = { authenticate };

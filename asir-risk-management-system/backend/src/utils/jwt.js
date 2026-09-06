const jwt = require('jsonwebtoken');
const env = require('../config/env');

function signAccessToken(user) {
  return jwt.sign(
    { sub: user.id, role: user.role_code, org: user.organization_id },
    env.jwt.accessSecret,
    { expiresIn: env.jwt.accessTtl }
  );
}

function verifyAccessToken(token) {
  return jwt.verify(token, env.jwt.accessSecret);
}

module.exports = { signAccessToken, verifyAccessToken };

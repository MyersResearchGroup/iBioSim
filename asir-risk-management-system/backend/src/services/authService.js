const bcrypt = require('bcrypt');
const { v4: uuidv4 } = require('uuid');
const crypto = require('crypto');
const userModel = require('../models/userModel');
const { query } = require('../config/db');
const { signAccessToken } = require('../utils/jwt');
const { ApiError } = require('../middleware/errorHandler');
const env = require('../config/env');

const SALT_ROUNDS = 12;

async function register({ fullName, email, password, jobTitle, roleId, organizationId, departmentId, preferredLang }) {
  const existing = await userModel.findByEmail(email);
  if (existing) throw new ApiError(409, 'CONFLICT', 'A user with this email already exists');

  const passwordHash = await bcrypt.hash(password, SALT_ROUNDS);
  return userModel.create({ fullName, email, passwordHash, jobTitle, roleId, organizationId, departmentId, preferredLang });
}

async function login({ email, password }) {
  const user = await userModel.findByEmail(email);
  if (!user || !user.is_active) throw new ApiError(401, 'INVALID_CREDENTIALS', 'Invalid email or password');

  const match = await bcrypt.compare(password, user.password_hash);
  if (!match) throw new ApiError(401, 'INVALID_CREDENTIALS', 'Invalid email or password');

  const accessToken = signAccessToken(user);
  const refreshToken = await issueRefreshToken(user.id);
  await userModel.updateLastLogin(user.id);

  const { password_hash, ...safeUser } = user;
  return { user: safeUser, accessToken, refreshToken };
}

async function issueRefreshToken(userId) {
  const token = crypto.randomBytes(48).toString('hex');
  const tokenHash = crypto.createHash('sha256').update(token).digest('hex');
  const expiresAt = new Date(Date.now() + env.jwt.refreshTtlDays * 24 * 60 * 60 * 1000);

  await query(
    `INSERT INTO refresh_tokens (id, user_id, token_hash, expires_at) VALUES ($1,$2,$3,$4)`,
    [uuidv4(), userId, tokenHash, expiresAt]
  );
  return token;
}

async function refresh(token) {
  const tokenHash = crypto.createHash('sha256').update(token).digest('hex');
  const { rows } = await query(
    `SELECT * FROM refresh_tokens WHERE token_hash = $1 AND revoked_at IS NULL AND expires_at > now()`,
    [tokenHash]
  );
  const record = rows[0];
  if (!record) throw new ApiError(401, 'INVALID_REFRESH_TOKEN', 'Refresh token invalid or expired');

  const user = await userModel.findById(record.user_id);
  if (!user || !user.is_active) throw new ApiError(401, 'INVALID_REFRESH_TOKEN', 'User inactive');

  await query('UPDATE refresh_tokens SET revoked_at = now() WHERE id = $1', [record.id]);
  const accessToken = signAccessToken(user);
  const newRefreshToken = await issueRefreshToken(user.id);
  return { accessToken, refreshToken: newRefreshToken };
}

async function logout(token) {
  const tokenHash = crypto.createHash('sha256').update(token).digest('hex');
  await query('UPDATE refresh_tokens SET revoked_at = now() WHERE token_hash = $1', [tokenHash]);
}

async function changePassword(userId, currentPassword, newPassword) {
  const user = await userModel.findById(userId);
  if (!user) throw new ApiError(404, 'NOT_FOUND', 'User not found');

  const match = await bcrypt.compare(currentPassword, user.password_hash);
  if (!match) throw new ApiError(401, 'INVALID_CREDENTIALS', 'Current password is incorrect');

  const passwordHash = await bcrypt.hash(newPassword, SALT_ROUNDS);
  await userModel.update(userId, { password_hash: passwordHash });
}

module.exports = { register, login, refresh, logout, changePassword };

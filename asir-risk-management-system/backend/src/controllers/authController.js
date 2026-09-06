const authService = require('../services/authService');
const auditModel = require('../models/auditModel');

async function register(req, res, next) {
  try {
    const user = await authService.register(req.body);
    await auditModel.record({
      userId: null, action: 'CREATE', entityType: 'user', entityId: user.id,
      newValue: { email: user.email }, ipAddress: req.ip
    });
    res.status(201).json({ user });
  } catch (err) { next(err); }
}

async function login(req, res, next) {
  try {
    const { user, accessToken, refreshToken } = await authService.login(req.body);
    await auditModel.record({ userId: user.id, action: 'LOGIN', entityType: 'user', entityId: user.id, ipAddress: req.ip });
    res.json({ user, accessToken, refreshToken });
  } catch (err) { next(err); }
}

async function refresh(req, res, next) {
  try {
    const { refreshToken } = req.body;
    const tokens = await authService.refresh(refreshToken);
    res.json(tokens);
  } catch (err) { next(err); }
}

async function logout(req, res, next) {
  try {
    const { refreshToken } = req.body;
    await authService.logout(refreshToken);
    res.status(204).send();
  } catch (err) { next(err); }
}

async function changePassword(req, res, next) {
  try {
    await authService.changePassword(req.user.id, req.body.currentPassword, req.body.newPassword);
    res.status(204).send();
  } catch (err) { next(err); }
}

module.exports = { register, login, refresh, logout, changePassword };

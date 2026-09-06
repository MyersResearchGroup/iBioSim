const bcrypt = require('bcrypt');
const userModel = require('../models/userModel');
const auditModel = require('../models/auditModel');
const { ApiError } = require('../middleware/errorHandler');

async function me(req, res, next) {
  try {
    const user = await userModel.findById(req.user.id);
    if (!user) throw new ApiError(404, 'NOT_FOUND', 'User not found');
    const { password_hash, ...safe } = user;
    res.json(safe);
  } catch (err) { next(err); }
}

async function list(req, res, next) {
  try {
    const { page = 1, pageSize = 20, search } = req.query;
    const limit = Math.min(parseInt(pageSize, 10) || 20, 100);
    const offset = (Math.max(parseInt(page, 10) || 1, 1) - 1) * limit;
    const { rows, total } = await userModel.list({ limit, offset, search });
    res.json({ data: rows.map(({ password_hash, ...u }) => u), page: Number(page), pageSize: limit, total });
  } catch (err) { next(err); }
}

async function getOne(req, res, next) {
  try {
    const user = await userModel.findById(req.params.id);
    if (!user) throw new ApiError(404, 'NOT_FOUND', 'User not found');
    const { password_hash, ...safe } = user;
    res.json(safe);
  } catch (err) { next(err); }
}

async function update(req, res, next) {
  try {
    const before = await userModel.findById(req.params.id);
    if (!before) throw new ApiError(404, 'NOT_FOUND', 'User not found');

    const fields = { ...req.body };
    if (fields.password) {
      fields.password_hash = await bcrypt.hash(fields.password, 12);
      delete fields.password;
    }
    const user = await userModel.update(req.params.id, fields);
    await auditModel.record({
      userId: req.user.id, action: 'UPDATE', entityType: 'user', entityId: req.params.id,
      oldValue: { full_name: before.full_name, is_active: before.is_active },
      newValue: { full_name: user.full_name, is_active: user.is_active }, ipAddress: req.ip
    });
    const { password_hash, ...safe } = user;
    res.json(safe);
  } catch (err) { next(err); }
}

async function remove(req, res, next) {
  try {
    await userModel.remove(req.params.id);
    await auditModel.record({ userId: req.user.id, action: 'DELETE', entityType: 'user', entityId: req.params.id, ipAddress: req.ip });
    res.status(204).send();
  } catch (err) { next(err); }
}

module.exports = { me, list, getOne, update, remove };

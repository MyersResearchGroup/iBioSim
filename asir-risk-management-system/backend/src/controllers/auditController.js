const auditModel = require('../models/auditModel');

async function list(req, res, next) {
  try {
    const { page = 1, pageSize = 50, entityType, userId } = req.query;
    const limit = Math.min(parseInt(pageSize, 10) || 50, 200);
    const offset = (Math.max(parseInt(page, 10) || 1, 1) - 1) * limit;
    const { rows, total } = await auditModel.list({ limit, offset, entityType, userId });
    res.json({ data: rows, page: Number(page), pageSize: limit, total });
  } catch (err) { next(err); }
}

module.exports = { list };

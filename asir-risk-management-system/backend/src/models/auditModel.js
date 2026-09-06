const { query } = require('../config/db');

async function record({ userId, action, entityType, entityId, oldValue, newValue, ipAddress }) {
  await query(
    `INSERT INTO audit_logs (user_id, action, entity_type, entity_id, old_value, new_value, ip_address)
     VALUES ($1,$2,$3,$4,$5,$6,$7)`,
    [userId, action, entityType, entityId, oldValue ? JSON.stringify(oldValue) : null,
      newValue ? JSON.stringify(newValue) : null, ipAddress]
  );
}

async function list({ limit = 50, offset = 0, entityType, userId }) {
  const params = [];
  const clauses = [];
  if (entityType) { params.push(entityType); clauses.push(`entity_type = $${params.length}`); }
  if (userId) { params.push(userId); clauses.push(`user_id = $${params.length}`); }
  const where = clauses.length ? `WHERE ${clauses.join(' AND ')}` : '';

  params.push(limit, offset);
  const { rows } = await query(
    `SELECT al.*, u.full_name AS user_name FROM audit_logs al
     LEFT JOIN users u ON u.id = al.user_id
     ${where} ORDER BY al.created_at DESC LIMIT $${params.length - 1} OFFSET $${params.length}`,
    params
  );
  const countRes = await query(`SELECT COUNT(*) FROM audit_logs ${where}`, params.slice(0, clauses.length));
  return { rows, total: parseInt(countRes.rows[0].count, 10) };
}

module.exports = { record, list };

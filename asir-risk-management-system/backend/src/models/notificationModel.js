const { query } = require('../config/db');

async function create({ userId, title, message, type, entityType, entityId }) {
  const { rows } = await query(
    `INSERT INTO notifications (user_id, title, message, type, entity_type, entity_id)
     VALUES ($1,$2,$3,COALESCE($4,'INFO'),$5,$6) RETURNING *`,
    [userId, title, message, type, entityType, entityId]
  );
  return rows[0];
}

async function listForUser(userId, { unreadOnly = false, limit = 30, offset = 0 } = {}) {
  const clauses = ['user_id = $1'];
  const params = [userId];
  if (unreadOnly) clauses.push('is_read = false');
  params.push(limit, offset);
  const { rows } = await query(
    `SELECT * FROM notifications WHERE ${clauses.join(' AND ')}
     ORDER BY created_at DESC LIMIT $${params.length - 1} OFFSET $${params.length}`,
    params
  );
  return rows;
}

async function markRead(id, userId) {
  await query('UPDATE notifications SET is_read = true WHERE id = $1 AND user_id = $2', [id, userId]);
}

async function markAllRead(userId) {
  await query('UPDATE notifications SET is_read = true WHERE user_id = $1 AND is_read = false', [userId]);
}

async function unreadCount(userId) {
  const { rows } = await query('SELECT COUNT(*) FROM notifications WHERE user_id = $1 AND is_read = false', [userId]);
  return parseInt(rows[0].count, 10);
}

module.exports = { create, listForUser, markRead, markAllRead, unreadCount };

const { query } = require('../config/db');

async function record({ userId, provider, prompt, response, contextEntity, contextId }) {
  const { rows } = await query(
    `INSERT INTO ai_queries (user_id, provider, prompt, response, context_entity, context_id)
     VALUES ($1,$2,$3,$4,$5,$6) RETURNING *`,
    [userId, provider, prompt, response, contextEntity, contextId]
  );
  return rows[0];
}

async function listForUser(userId, { limit = 20 } = {}) {
  const { rows } = await query(
    'SELECT * FROM ai_queries WHERE user_id = $1 ORDER BY created_at DESC LIMIT $2',
    [userId, limit]
  );
  return rows;
}

module.exports = { record, listForUser };

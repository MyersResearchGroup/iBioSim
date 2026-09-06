const { query } = require('../config/db');

async function findById(id) {
  const { rows } = await query('SELECT * FROM controls WHERE id = $1', [id]);
  return rows[0] || null;
}

async function findByRiskId(riskId) {
  const { rows } = await query('SELECT * FROM controls WHERE risk_id = $1 ORDER BY created_at', [riskId]);
  return rows;
}

async function update(id, fields) {
  const keys = Object.keys(fields);
  if (!keys.length) return findById(id);
  const setClause = keys.map((k, i) => `${k} = $${i + 2}`).join(', ');
  await query(`UPDATE controls SET ${setClause} WHERE id = $1`, [id, ...keys.map((k) => fields[k])]);
  return findById(id);
}

async function findRiskIdForControl(id) {
  const { rows } = await query('SELECT risk_id FROM controls WHERE id = $1', [id]);
  return rows[0]?.risk_id || null;
}

module.exports = { findById, findByRiskId, update, findRiskIdForControl };

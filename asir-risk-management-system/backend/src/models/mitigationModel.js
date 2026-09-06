const { query } = require('../config/db');

async function findByRiskId(riskId) {
  const { rows } = await query(
    `SELECT mp.*, u.full_name AS owner_name FROM mitigation_plans mp
     LEFT JOIN users u ON u.id = mp.owner_id WHERE mp.risk_id = $1 ORDER BY mp.due_date NULLS LAST`,
    [riskId]
  );
  return rows;
}

async function create({ riskId, description, ownerId, dueDate, status }) {
  const { rows } = await query(
    `INSERT INTO mitigation_plans (risk_id, description, owner_id, due_date, status)
     VALUES ($1,$2,$3,$4,COALESCE($5,'PLANNED')) RETURNING *`,
    [riskId, description, ownerId, dueDate, status]
  );
  return rows[0];
}

async function update(id, fields) {
  const keys = Object.keys(fields);
  if (!keys.length) return findOne(id);
  const setClause = keys.map((k, i) => `${k} = $${i + 2}`).join(', ');
  await query(`UPDATE mitigation_plans SET ${setClause} WHERE id = $1`, [id, ...keys.map((k) => fields[k])]);
  return findOne(id);
}

async function findOne(id) {
  const { rows } = await query('SELECT * FROM mitigation_plans WHERE id = $1', [id]);
  return rows[0] || null;
}

async function remove(id) {
  await query('DELETE FROM mitigation_plans WHERE id = $1', [id]);
}

module.exports = { findByRiskId, create, update, findOne, remove };

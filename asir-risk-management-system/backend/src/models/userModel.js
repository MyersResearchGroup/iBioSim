const { query } = require('../config/db');

const BASE_SELECT = `
  SELECT u.id, u.full_name, u.email, u.password_hash, u.job_title, u.preferred_lang,
         u.is_active, u.last_login_at, u.created_at,
         r.code AS role_code, r.name_en AS role_name_en, r.name_ar AS role_name_ar,
         u.organization_id, u.department_id
  FROM users u
  JOIN roles r ON r.id = u.role_id
`;

async function findByEmail(email) {
  const { rows } = await query(`${BASE_SELECT} WHERE u.email = $1`, [email]);
  return rows[0] || null;
}

async function findById(id) {
  const { rows } = await query(`${BASE_SELECT} WHERE u.id = $1`, [id]);
  return rows[0] || null;
}

async function list({ limit, offset, search }) {
  const params = [];
  let where = '';
  if (search) {
    params.push(`%${search}%`);
    where = `WHERE u.full_name ILIKE $${params.length} OR u.email ILIKE $${params.length}`;
  }
  params.push(limit, offset);
  const { rows } = await query(
    `${BASE_SELECT} ${where} ORDER BY u.created_at DESC LIMIT $${params.length - 1} OFFSET $${params.length}`,
    params
  );
  const countRes = await query(`SELECT COUNT(*) FROM users u ${where}`, params.slice(0, where ? 1 : 0));
  return { rows, total: parseInt(countRes.rows[0].count, 10) };
}

async function create({ fullName, email, passwordHash, jobTitle, roleId, organizationId, departmentId, preferredLang }) {
  const { rows } = await query(
    `INSERT INTO users (full_name, email, password_hash, job_title, role_id, organization_id, department_id, preferred_lang)
     VALUES ($1,$2,$3,$4,$5,$6,$7,$8) RETURNING id`,
    [fullName, email, passwordHash, jobTitle, roleId, organizationId, departmentId, preferredLang || 'ar']
  );
  return findById(rows[0].id);
}

async function update(id, fields) {
  const keys = Object.keys(fields);
  if (!keys.length) return findById(id);
  const setClause = keys.map((k, i) => `${k} = $${i + 2}`).join(', ');
  await query(`UPDATE users SET ${setClause} WHERE id = $1`, [id, ...keys.map((k) => fields[k])]);
  return findById(id);
}

async function findFirstByRole(roleCode) {
  const { rows } = await query(`${BASE_SELECT} WHERE r.code = $1 AND u.is_active = true ORDER BY u.created_at LIMIT 1`, [roleCode]);
  return rows[0] || null;
}

async function updateLastLogin(id) {
  await query('UPDATE users SET last_login_at = now() WHERE id = $1', [id]);
}

async function remove(id) {
  await query('DELETE FROM users WHERE id = $1', [id]);
}

module.exports = { findByEmail, findById, list, create, update, updateLastLogin, remove, findFirstByRole };

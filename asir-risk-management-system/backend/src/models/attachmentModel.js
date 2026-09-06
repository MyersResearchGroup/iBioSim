const { query } = require('../config/db');

async function create({ entityType, entityId, fileName, filePath, mimeType, fileSize, uploadedBy }) {
  const { rows } = await query(
    `INSERT INTO attachments (entity_type, entity_id, file_name, file_path, mime_type, file_size, uploaded_by)
     VALUES ($1,$2,$3,$4,$5,$6,$7) RETURNING *`,
    [entityType, entityId, fileName, filePath, mimeType, fileSize, uploadedBy]
  );
  return rows[0];
}

async function listFor(entityType, entityId) {
  const { rows } = await query(
    'SELECT * FROM attachments WHERE entity_type = $1 AND entity_id = $2 ORDER BY uploaded_at DESC',
    [entityType, entityId]
  );
  return rows;
}

async function findById(id) {
  const { rows } = await query('SELECT * FROM attachments WHERE id = $1', [id]);
  return rows[0] || null;
}

async function remove(id) {
  await query('DELETE FROM attachments WHERE id = $1', [id]);
}

module.exports = { create, listFor, findById, remove };

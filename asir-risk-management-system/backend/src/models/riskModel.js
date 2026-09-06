const { query, withTransaction } = require('../config/db');

const LIST_SELECT = `
  SELECT r.id, r.risk_code, r.risk_statement, r.root_causes, r.identification_date,
         r.likelihood_score, r.impact_score, r.inherent_risk_rating,
         r.control_environment_sufficiency, r.residual_risk_priority, r.residual_risk_level,
         r.status, r.is_top_risk, r.latest_update_date, r.latest_update_text,
         r.created_at, r.updated_at,
         r.organization_id, r.sector_id, r.department_id, r.category_id, r.subcategory_id, r.risk_owner_id,
         o.name_ar AS organization_name_ar, o.name_en AS organization_name_en,
         s.name_ar AS sector_name_ar, d.name_ar AS department_name_ar,
         c.name_ar AS category_name_ar, c.name_en AS category_name_en,
         sc.name_ar AS subcategory_name_ar,
         u.full_name AS risk_owner_name, r.risk_owner_title
  FROM risks r
  LEFT JOIN organizations o ON o.id = r.organization_id
  LEFT JOIN sectors s ON s.id = r.sector_id
  LEFT JOIN departments d ON d.id = r.department_id
  LEFT JOIN risk_categories c ON c.id = r.category_id
  LEFT JOIN risk_subcategories sc ON sc.id = r.subcategory_id
  LEFT JOIN users u ON u.id = r.risk_owner_id
`;

const SORTABLE_COLUMNS = {
  risk_code: 'r.risk_code',
  identification_date: 'r.identification_date',
  inherent_risk_rating: 'r.inherent_risk_rating',
  residual_risk_level: 'r.residual_risk_level',
  status: 'r.status',
  created_at: 'r.created_at',
  updated_at: 'r.updated_at'
};

function buildWhere(filters) {
  const params = [];
  const clauses = [];

  if (filters.search) {
    params.push(`%${filters.search}%`);
    clauses.push(`(r.risk_statement ILIKE $${params.length} OR r.risk_code ILIKE $${params.length} OR r.root_causes ILIKE $${params.length})`);
  }
  if (filters.organizationId) { params.push(filters.organizationId); clauses.push(`r.organization_id = $${params.length}`); }
  if (filters.sectorId) { params.push(filters.sectorId); clauses.push(`r.sector_id = $${params.length}`); }
  if (filters.categoryId) { params.push(filters.categoryId); clauses.push(`r.category_id = $${params.length}`); }
  if (filters.status) { params.push(filters.status); clauses.push(`r.status = $${params.length}`); }
  if (filters.residualLevel) { params.push(filters.residualLevel); clauses.push(`r.residual_risk_level = $${params.length}`); }
  if (filters.riskOwnerId) { params.push(filters.riskOwnerId); clauses.push(`r.risk_owner_id = $${params.length}`); }
  if (filters.isTopRisk !== undefined) { params.push(filters.isTopRisk); clauses.push(`r.is_top_risk = $${params.length}`); }

  const where = clauses.length ? `WHERE ${clauses.join(' AND ')}` : '';
  return { where, params };
}

async function list({ filters = {}, sortBy = 'created_at', sortDir = 'DESC', limit = 25, offset = 0 }) {
  const { where, params } = buildWhere(filters);
  const orderCol = SORTABLE_COLUMNS[sortBy] || SORTABLE_COLUMNS.created_at;
  const dir = sortDir.toUpperCase() === 'ASC' ? 'ASC' : 'DESC';

  const dataParams = [...params, limit, offset];
  const { rows } = await query(
    `${LIST_SELECT} ${where} ORDER BY ${orderCol} ${dir} LIMIT $${dataParams.length - 1} OFFSET $${dataParams.length}`,
    dataParams
  );

  const countRes = await query(`SELECT COUNT(*) FROM risks r ${where}`, params);
  return { rows, total: parseInt(countRes.rows[0].count, 10) };
}

async function findById(id) {
  const { rows } = await query(`${LIST_SELECT} WHERE r.id = $1`, [id]);
  if (!rows[0]) return null;

  const impactScores = await query(
    `SELECT ia.id AS impact_area_id, ia.name_ar, ia.name_en, ris.score
     FROM risk_impact_scores ris JOIN impact_areas ia ON ia.id = ris.impact_area_id
     WHERE ris.risk_id = $1 ORDER BY ia.display_order`,
    [id]
  );
  const controls = await query(`SELECT * FROM controls WHERE risk_id = $1 ORDER BY created_at`, [id]);
  const mitigations = await query(`SELECT * FROM mitigation_plans WHERE risk_id = $1 ORDER BY due_date NULLS LAST`, [id]);
  const updates = await query(`SELECT * FROM risk_updates WHERE risk_id = $1 ORDER BY update_date DESC`, [id]);

  return {
    ...rows[0],
    impactScores: impactScores.rows,
    controls: controls.rows,
    mitigations: mitigations.rows,
    updates: updates.rows
  };
}

async function nextRiskCode(orgCode) {
  const { rows } = await query(
    `SELECT COALESCE(MAX(NULLIF(regexp_replace(risk_code, '^.*\\.R-(\\d+)$', '\\1'), risk_code)::int), 0) AS max_num
     FROM risks WHERE risk_code LIKE $1`,
    [`${orgCode}.R-%`]
  );
  const maxNum = rows[0]?.max_num || 0;
  return `${orgCode}.R-${maxNum + 1}`;
}

async function create(data) {
  return withTransaction(async (client) => {
    const {
      riskCode, organizationId, sectorId, departmentId, riskOwnerId, riskOwnerTitle,
      identificationDate, riskStatement, rootCauses, categoryId, subcategoryId,
      likelihoodScore, impactAreaScores, createdBy
    } = data;

    const { rows } = await client.query(
      `INSERT INTO risks (risk_code, organization_id, sector_id, department_id, risk_owner_id, risk_owner_title,
                           identification_date, risk_statement, root_causes, category_id, subcategory_id,
                           likelihood_score, created_by)
       VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12,$13) RETURNING id`,
      [riskCode, organizationId, sectorId, departmentId, riskOwnerId, riskOwnerTitle,
        identificationDate, riskStatement, rootCauses, categoryId, subcategoryId,
        likelihoodScore, createdBy]
    );
    const riskId = rows[0].id;

    if (impactAreaScores && impactAreaScores.length) {
      for (const s of impactAreaScores) {
        await client.query(
          `INSERT INTO risk_impact_scores (risk_id, impact_area_id, score) VALUES ($1,$2,$3)`,
          [riskId, s.impactAreaId, s.score]
        );
      }
    }
    await client.query(
      `INSERT INTO controls (control_code, risk_id, control_type) VALUES ($1,$2,'NONE')`,
      [`CTR-${riskCode}`, riskId]
    );
    return riskId;
  });
}

async function update(id, fields) {
  const keys = Object.keys(fields);
  if (!keys.length) return;
  const setClause = keys.map((k, i) => `${k} = $${i + 2}`).join(', ');
  await query(`UPDATE risks SET ${setClause} WHERE id = $1`, [id, ...keys.map((k) => fields[k])]);
}

async function replaceImpactScores(riskId, impactAreaScores) {
  return withTransaction(async (client) => {
    await client.query('DELETE FROM risk_impact_scores WHERE risk_id = $1', [riskId]);
    for (const s of impactAreaScores || []) {
      await client.query(
        'INSERT INTO risk_impact_scores (risk_id, impact_area_id, score) VALUES ($1,$2,$3)',
        [riskId, s.impactAreaId, s.score]
      );
    }
  });
}

async function updateRatings(riskId, ratings) {
  await query(
    `UPDATE risks SET impact_score = $2, inherent_risk_rating = $3,
                       control_environment_sufficiency = $4, residual_risk_priority = $5, residual_risk_level = $6
     WHERE id = $1`,
    [riskId, ratings.impactScore, ratings.inherentRiskRating, ratings.controlEnvironmentSufficiency,
      ratings.residualRiskPriority, ratings.residualRiskLevel]
  );
}

async function remove(id) {
  await query('DELETE FROM risks WHERE id = $1', [id]);
}

async function getControlByRiskId(riskId) {
  const { rows } = await query('SELECT * FROM controls WHERE risk_id = $1 LIMIT 1', [riskId]);
  return rows[0] || null;
}

async function getImpactScores(riskId) {
  const { rows } = await query('SELECT score FROM risk_impact_scores WHERE risk_id = $1', [riskId]);
  return rows;
}

module.exports = {
  list, findById, create, update, remove, nextRiskCode,
  replaceImpactScores, updateRatings, getControlByRiskId, getImpactScores
};

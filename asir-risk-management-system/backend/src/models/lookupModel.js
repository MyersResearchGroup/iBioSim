const { query } = require('../config/db');

// Generic read-side lookups shared across controllers for dropdowns/filters.

async function listOrganizations() {
  const { rows } = await query('SELECT * FROM organizations ORDER BY org_type, name_ar');
  return rows;
}

async function listSectors(organizationId) {
  const params = [];
  let where = '';
  if (organizationId) { params.push(organizationId); where = 'WHERE organization_id = $1'; }
  const { rows } = await query(`SELECT * FROM sectors ${where} ORDER BY name_ar`, params);
  return rows;
}

async function listDepartments(sectorId) {
  const params = [];
  let where = '';
  if (sectorId) { params.push(sectorId); where = 'WHERE sector_id = $1'; }
  const { rows } = await query(`SELECT * FROM departments ${where} ORDER BY name_ar`, params);
  return rows;
}

async function listCategories() {
  const { rows } = await query('SELECT * FROM risk_categories ORDER BY name_ar');
  return rows;
}

async function listSubcategories(categoryId) {
  const params = [];
  let where = '';
  if (categoryId) { params.push(categoryId); where = 'WHERE category_id = $1'; }
  const { rows } = await query(`SELECT * FROM risk_subcategories ${where} ORDER BY name_ar`, params);
  return rows;
}

async function listImpactAreas() {
  const { rows } = await query('SELECT * FROM impact_areas ORDER BY display_order');
  return rows;
}

async function listRoles() {
  const { rows } = await query('SELECT * FROM roles ORDER BY id');
  return rows;
}

module.exports = {
  listOrganizations, listSectors, listDepartments,
  listCategories, listSubcategories, listImpactAreas, listRoles
};

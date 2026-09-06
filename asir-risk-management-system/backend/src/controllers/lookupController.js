const lookupModel = require('../models/lookupModel');

async function organizations(req, res, next) {
  try { res.json(await lookupModel.listOrganizations()); } catch (err) { next(err); }
}
async function sectors(req, res, next) {
  try { res.json(await lookupModel.listSectors(req.query.organizationId)); } catch (err) { next(err); }
}
async function departments(req, res, next) {
  try { res.json(await lookupModel.listDepartments(req.query.sectorId)); } catch (err) { next(err); }
}
async function categories(req, res, next) {
  try { res.json(await lookupModel.listCategories()); } catch (err) { next(err); }
}
async function subcategories(req, res, next) {
  try { res.json(await lookupModel.listSubcategories(req.query.categoryId)); } catch (err) { next(err); }
}
async function impactAreas(req, res, next) {
  try { res.json(await lookupModel.listImpactAreas()); } catch (err) { next(err); }
}
async function roles(req, res, next) {
  try { res.json(await lookupModel.listRoles()); } catch (err) { next(err); }
}

module.exports = { organizations, sectors, departments, categories, subcategories, impactAreas, roles };

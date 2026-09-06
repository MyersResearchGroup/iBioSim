const riskModel = require('../models/riskModel');
const riskService = require('../services/riskService');
const mitigationModel = require('../models/mitigationModel');
const auditModel = require('../models/auditModel');
const exportService = require('../services/exportService');
const { query } = require('../config/db');
const { ApiError } = require('../middleware/errorHandler');

async function list(req, res, next) {
  try {
    const { page = 1, pageSize = 25, sortBy, sortDir, search, organizationId, sectorId,
      categoryId, status, residualLevel, riskOwnerId, isTopRisk } = req.query;

    const limit = Math.min(parseInt(pageSize, 10) || 25, 200);
    const offset = (Math.max(parseInt(page, 10) || 1, 1) - 1) * limit;

    const { rows, total } = await riskModel.list({
      filters: {
        search, organizationId, sectorId, categoryId, status, residualLevel, riskOwnerId,
        isTopRisk: isTopRisk === undefined ? undefined : isTopRisk === 'true'
      },
      sortBy, sortDir, limit, offset
    });

    res.json({ data: rows, page: Number(page), pageSize: limit, total });
  } catch (err) { next(err); }
}

async function getOne(req, res, next) {
  try {
    const risk = await riskModel.findById(req.params.id);
    if (!risk) throw new ApiError(404, 'NOT_FOUND', 'Risk not found');
    res.json(risk);
  } catch (err) { next(err); }
}

async function create(req, res, next) {
  try {
    const risk = await riskService.createRisk(req.body, req.user.id);
    await auditModel.record({
      userId: req.user.id, action: 'CREATE', entityType: 'risk', entityId: risk.id,
      newValue: { risk_code: risk.risk_code, risk_statement: risk.risk_statement }, ipAddress: req.ip
    });
    res.status(201).json(risk);
  } catch (err) { next(err); }
}

async function update(req, res, next) {
  try {
    const before = await riskModel.findById(req.params.id);
    if (!before) throw new ApiError(404, 'NOT_FOUND', 'Risk not found');

    const risk = await riskService.updateRisk(req.params.id, req.body);
    await auditModel.record({
      userId: req.user.id, action: 'UPDATE', entityType: 'risk', entityId: req.params.id,
      oldValue: { status: before.status, residual_risk_level: before.residual_risk_level },
      newValue: { status: risk.status, residual_risk_level: risk.residual_risk_level }, ipAddress: req.ip
    });
    res.json(risk);
  } catch (err) { next(err); }
}

async function remove(req, res, next) {
  try {
    await riskModel.remove(req.params.id);
    await auditModel.record({ userId: req.user.id, action: 'DELETE', entityType: 'risk', entityId: req.params.id, ipAddress: req.ip });
    res.status(204).send();
  } catch (err) { next(err); }
}

async function addUpdate(req, res, next) {
  try {
    const { rows } = await query(
      `INSERT INTO risk_updates (risk_id, update_date, update_text, updated_by) VALUES ($1, COALESCE($2, CURRENT_DATE), $3, $4) RETURNING *`,
      [req.params.id, req.body.updateDate, req.body.updateText, req.user.id]
    );
    await riskModel.update(req.params.id, {
      latest_update_date: rows[0].update_date,
      latest_update_text: rows[0].update_text
    });
    res.status(201).json(rows[0]);
  } catch (err) { next(err); }
}

async function updateControl(req, res, next) {
  try {
    const risk = await riskService.updateControl(req.params.controlId, req.body);
    res.json(risk);
  } catch (err) { next(err); }
}

async function addMitigation(req, res, next) {
  try {
    const plan = await mitigationModel.create({ riskId: req.params.id, ...req.body });
    res.status(201).json(plan);
  } catch (err) { next(err); }
}

async function updateMitigation(req, res, next) {
  try {
    const plan = await mitigationModel.update(req.params.mitigationId, req.body);
    res.json(plan);
  } catch (err) { next(err); }
}

async function exportExcel(req, res, next) {
  try {
    const { rows } = await riskModel.list({ filters: req.query, limit: 5000, offset: 0 });
    const buffer = await exportService.risksToExcelBuffer(rows);
    await auditModel.record({ userId: req.user.id, action: 'EXPORT', entityType: 'risk', entityId: 'excel', ipAddress: req.ip });
    res.setHeader('Content-Type', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet');
    res.setHeader('Content-Disposition', 'attachment; filename="risk-register.xlsx"');
    res.send(buffer);
  } catch (err) { next(err); }
}

async function exportPdf(req, res, next) {
  try {
    const { rows } = await riskModel.list({ filters: req.query, limit: 1000, offset: 0 });
    await auditModel.record({ userId: req.user.id, action: 'EXPORT', entityType: 'risk', entityId: 'pdf', ipAddress: req.ip });
    res.setHeader('Content-Type', 'application/pdf');
    res.setHeader('Content-Disposition', 'attachment; filename="risk-register.pdf"');
    exportService.risksToPdfStream(rows, res);
  } catch (err) { next(err); }
}

module.exports = {
  list, getOne, create, update, remove, addUpdate,
  updateControl, addMitigation, updateMitigation, exportExcel, exportPdf
};

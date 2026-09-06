const riskModel = require('../models/riskModel');
const controlModel = require('../models/controlModel');
const notificationModel = require('../models/notificationModel');
const userModel = require('../models/userModel');
const { computeRiskRatings } = require('../utils/riskMatrix');
const { ApiError } = require('../middleware/errorHandler');

async function recalculateAndPersist(riskId) {
  const risk = await riskModel.findById(riskId);
  if (!risk) throw new ApiError(404, 'NOT_FOUND', 'Risk not found');

  const control = await riskModel.getControlByRiskId(riskId);
  const impactAreaScores = await riskModel.getImpactScores(riskId);

  const ratings = computeRiskRatings({
    likelihoodScore: risk.likelihood_score,
    impactAreaScores,
    control
  });

  await riskModel.updateRatings(riskId, ratings);

  if (ratings.residualRiskLevel === 'مستوى 1' || ratings.residualRiskLevel === 'مستوى 2') {
    await riskModel.update(riskId, { is_top_risk: true });
    const manager = await userModel.findFirstByRole('MANAGER');
    if (manager) {
      await notificationModel.create({
        userId: manager.id,
        title: `خطر ذو أولوية عالية: ${risk.risk_code}`,
        message: risk.risk_statement,
        type: 'CRITICAL',
        entityType: 'risk',
        entityId: riskId
      });
    }
  }

  return ratings;
}

async function createRisk(payload, createdBy) {
  const riskCode = payload.riskCode || (await riskModel.nextRiskCode(payload.orgCode || 'CL(AS)'));
  const riskId = await riskModel.create({ ...payload, riskCode, createdBy });
  await recalculateAndPersist(riskId);
  return riskModel.findById(riskId);
}

async function updateRisk(riskId, fields) {
  const existing = await riskModel.findById(riskId);
  if (!existing) throw new ApiError(404, 'NOT_FOUND', 'Risk not found');

  const { impactAreaScores, ...columnFields } = fields;
  if (Object.keys(columnFields).length) await riskModel.update(riskId, columnFields);
  if (impactAreaScores) await riskModel.replaceImpactScores(riskId, impactAreaScores);

  await recalculateAndPersist(riskId);
  return riskModel.findById(riskId);
}

async function updateControl(controlId, fields) {
  const riskId = await controlModel.findRiskIdForControl(controlId);
  if (!riskId) throw new ApiError(404, 'NOT_FOUND', 'Control not found');

  await controlModel.update(controlId, fields);
  await recalculateAndPersist(riskId);
  return riskModel.findById(riskId);
}

module.exports = { createRisk, updateRisk, updateControl, recalculateAndPersist };

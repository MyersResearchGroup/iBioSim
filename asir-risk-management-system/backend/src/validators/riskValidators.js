const Joi = require('joi');

const impactAreaScore = Joi.object({
  impactAreaId: Joi.number().integer().required(),
  score: Joi.number().integer().min(1).max(5).required()
});

const createRisk = Joi.object({
  riskCode: Joi.string().max(30).allow('', null),
  orgCode: Joi.string().max(20).allow('', null),
  organizationId: Joi.number().integer().required(),
  sectorId: Joi.number().integer().allow(null),
  departmentId: Joi.number().integer().allow(null),
  riskOwnerId: Joi.string().uuid().allow(null),
  riskOwnerTitle: Joi.string().max(200).allow('', null),
  identificationDate: Joi.date().default(() => new Date()),
  riskStatement: Joi.string().min(5).required(),
  rootCauses: Joi.string().allow('', null),
  categoryId: Joi.number().integer().allow(null),
  subcategoryId: Joi.number().integer().allow(null),
  likelihoodScore: Joi.number().integer().min(1).max(5).allow(null),
  impactAreaScores: Joi.array().items(impactAreaScore).default([])
});

const updateRisk = Joi.object({
  risk_statement: Joi.string().min(5),
  root_causes: Joi.string().allow('', null),
  category_id: Joi.number().integer().allow(null),
  subcategory_id: Joi.number().integer().allow(null),
  likelihood_score: Joi.number().integer().min(1).max(5).allow(null),
  status: Joi.string().valid('OPEN', 'MITIGATING', 'MONITORING', 'CLOSED'),
  risk_owner_id: Joi.string().uuid().allow(null),
  risk_owner_title: Joi.string().max(200).allow('', null),
  impactAreaScores: Joi.array().items(impactAreaScore)
}).min(1);

const addUpdate = Joi.object({
  updateDate: Joi.date(),
  updateText: Joi.string().min(2).required()
});

const updateControl = Joi.object({
  description: Joi.string().allow('', null),
  control_type: Joi.string().valid('PREVENTIVE', 'DETECTIVE', 'CORRECTIVE', 'NONE'),
  is_activated_approved: Joi.boolean(),
  is_shared_implemented: Joi.boolean(),
  is_fully_committed: Joi.boolean(),
  is_effective: Joi.boolean()
}).min(1);

const createMitigation = Joi.object({
  description: Joi.string().min(2).required(),
  ownerId: Joi.string().uuid().allow(null),
  dueDate: Joi.date().allow(null),
  status: Joi.string().valid('PLANNED', 'IN_PROGRESS', 'DONE', 'OVERDUE', 'CANCELLED')
});

const updateMitigation = Joi.object({
  description: Joi.string().min(2),
  owner_id: Joi.string().uuid().allow(null),
  due_date: Joi.date().allow(null),
  status: Joi.string().valid('PLANNED', 'IN_PROGRESS', 'DONE', 'OVERDUE', 'CANCELLED')
}).min(1);

module.exports = { createRisk, updateRisk, addUpdate, updateControl, createMitigation, updateMitigation };

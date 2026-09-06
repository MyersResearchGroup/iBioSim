const Joi = require('joi');

const register = Joi.object({
  fullName: Joi.string().min(2).max(200).required(),
  email: Joi.string().email().required(),
  password: Joi.string().min(8).max(100).required(),
  jobTitle: Joi.string().max(200).allow('', null),
  roleId: Joi.number().integer().required(),
  organizationId: Joi.number().integer().allow(null),
  departmentId: Joi.number().integer().allow(null),
  preferredLang: Joi.string().valid('ar', 'en').default('ar')
});

const login = Joi.object({
  email: Joi.string().email().required(),
  password: Joi.string().required()
});

const refresh = Joi.object({
  refreshToken: Joi.string().required()
});

const changePassword = Joi.object({
  currentPassword: Joi.string().required(),
  newPassword: Joi.string().min(8).max(100).required()
});

module.exports = { register, login, refresh, changePassword };

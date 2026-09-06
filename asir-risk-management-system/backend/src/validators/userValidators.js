const Joi = require('joi');

const updateUser = Joi.object({
  full_name: Joi.string().min(2).max(200),
  job_title: Joi.string().max(200).allow('', null),
  role_id: Joi.number().integer(),
  organization_id: Joi.number().integer().allow(null),
  department_id: Joi.number().integer().allow(null),
  preferred_lang: Joi.string().valid('ar', 'en'),
  is_active: Joi.boolean(),
  password: Joi.string().min(8).max(100)
}).min(1);

module.exports = { updateUser };

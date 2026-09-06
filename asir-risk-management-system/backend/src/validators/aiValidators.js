const Joi = require('joi');

const chat = Joi.object({
  prompt: Joi.string().min(2).max(4000).required(),
  riskId: Joi.string().uuid().allow(null)
});

module.exports = { chat };

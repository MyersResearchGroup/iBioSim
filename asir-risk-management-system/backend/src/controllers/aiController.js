const aiService = require('../services/aiService');

async function chat(req, res, next) {
  try {
    const result = await aiService.chat({
      userId: req.user.id,
      prompt: req.body.prompt,
      contextRiskId: req.body.riskId
    });
    res.json(result);
  } catch (err) { next(err); }
}

async function suggestMitigations(req, res, next) {
  try {
    const result = await aiService.suggestMitigations({ userId: req.user.id, riskId: req.params.riskId });
    res.json(result);
  } catch (err) { next(err); }
}

module.exports = { chat, suggestMitigations };

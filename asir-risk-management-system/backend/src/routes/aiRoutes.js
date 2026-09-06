const express = require('express');
const controller = require('../controllers/aiController');
const { authenticate } = require('../middleware/auth');
const { validate } = require('../middleware/validate');
const schemas = require('../validators/aiValidators');

const router = express.Router();
router.use(authenticate);

router.post('/chat', validate(schemas.chat), controller.chat);
router.post('/risks/:riskId/suggest-mitigations', controller.suggestMitigations);

module.exports = router;

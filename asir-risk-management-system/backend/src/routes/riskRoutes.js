const express = require('express');
const controller = require('../controllers/riskController');
const { authenticate } = require('../middleware/auth');
const { requireRole } = require('../middleware/rbac');
const { validate } = require('../middleware/validate');
const schemas = require('../validators/riskValidators');

const router = express.Router();
router.use(authenticate);

const CAN_WRITE = ['ADMIN', 'MANAGER', 'RISK_OWNER'];

router.get('/export/excel', controller.exportExcel);
router.get('/export/pdf', controller.exportPdf);

router.get('/', controller.list);
router.get('/:id', controller.getOne);
router.post('/', requireRole(...CAN_WRITE), validate(schemas.createRisk), controller.create);
router.patch('/:id', requireRole(...CAN_WRITE), validate(schemas.updateRisk), controller.update);
router.delete('/:id', requireRole('ADMIN', 'MANAGER'), controller.remove);

router.post('/:id/updates', requireRole(...CAN_WRITE), validate(schemas.addUpdate), controller.addUpdate);

router.patch('/controls/:controlId', requireRole(...CAN_WRITE), validate(schemas.updateControl), controller.updateControl);

router.post('/:id/mitigations', requireRole(...CAN_WRITE), validate(schemas.createMitigation), controller.addMitigation);
router.patch('/mitigations/:mitigationId', requireRole(...CAN_WRITE), validate(schemas.updateMitigation), controller.updateMitigation);

module.exports = router;

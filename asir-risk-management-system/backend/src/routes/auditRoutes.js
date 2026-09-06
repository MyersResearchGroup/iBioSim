const express = require('express');
const controller = require('../controllers/auditController');
const { authenticate } = require('../middleware/auth');
const { requireRole } = require('../middleware/rbac');

const router = express.Router();
router.use(authenticate, requireRole('ADMIN', 'MANAGER'));
router.get('/', controller.list);

module.exports = router;

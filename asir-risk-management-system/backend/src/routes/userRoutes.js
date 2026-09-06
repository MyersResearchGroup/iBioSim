const express = require('express');
const controller = require('../controllers/userController');
const { authenticate } = require('../middleware/auth');
const { requireRole } = require('../middleware/rbac');
const { validate } = require('../middleware/validate');
const schemas = require('../validators/userValidators');

const router = express.Router();
router.use(authenticate);

router.get('/me', controller.me);
router.get('/', requireRole('ADMIN', 'MANAGER'), controller.list);
router.get('/:id', requireRole('ADMIN', 'MANAGER'), controller.getOne);
router.patch('/:id', requireRole('ADMIN'), validate(schemas.updateUser), controller.update);
router.delete('/:id', requireRole('ADMIN'), controller.remove);

module.exports = router;

const express = require('express');
const controller = require('../controllers/authController');
const { validate } = require('../middleware/validate');
const { authenticate } = require('../middleware/auth');
const { authLimiter } = require('../middleware/rateLimit');
const schemas = require('../validators/authValidators');

const router = express.Router();

router.post('/register', authLimiter, validate(schemas.register), controller.register);
router.post('/login', authLimiter, validate(schemas.login), controller.login);
router.post('/refresh', authLimiter, validate(schemas.refresh), controller.refresh);
router.post('/logout', validate(schemas.refresh), controller.logout);
router.post('/change-password', authenticate, validate(schemas.changePassword), controller.changePassword);

module.exports = router;

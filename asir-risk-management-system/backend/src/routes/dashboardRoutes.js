const express = require('express');
const controller = require('../controllers/dashboardController');
const { authenticate } = require('../middleware/auth');

const router = express.Router();
router.use(authenticate);
router.get('/summary', controller.summary);

module.exports = router;

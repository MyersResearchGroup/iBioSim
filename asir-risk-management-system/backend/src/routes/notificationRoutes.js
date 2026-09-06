const express = require('express');
const controller = require('../controllers/notificationController');
const { authenticate } = require('../middleware/auth');

const router = express.Router();
router.use(authenticate);

router.get('/', controller.list);
router.post('/:id/read', controller.markRead);
router.post('/read-all', controller.markAllRead);

module.exports = router;

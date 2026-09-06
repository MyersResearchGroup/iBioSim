const express = require('express');

const router = express.Router();

router.use('/auth', require('./authRoutes'));
router.use('/users', require('./userRoutes'));
router.use('/risks', require('./riskRoutes'));
router.use('/lookups', require('./lookupRoutes'));
router.use('/dashboard', require('./dashboardRoutes'));
router.use('/notifications', require('./notificationRoutes'));
router.use('/attachments', require('./attachmentRoutes'));
router.use('/ai', require('./aiRoutes'));
router.use('/audit-logs', require('./auditRoutes'));

router.get('/health', (req, res) => res.json({ status: 'ok', timestamp: new Date().toISOString() }));

module.exports = router;

const express = require('express');
const controller = require('../controllers/attachmentController');
const { authenticate } = require('../middleware/auth');
const upload = require('../middleware/upload');

const router = express.Router();
router.use(authenticate);

router.get('/', controller.listFor);
router.post('/', upload.single('file'), controller.upload);
router.get('/:id/download', controller.download);
router.delete('/:id', controller.remove);

module.exports = router;

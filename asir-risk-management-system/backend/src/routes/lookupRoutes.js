const express = require('express');
const controller = require('../controllers/lookupController');
const { authenticate } = require('../middleware/auth');

const router = express.Router();
router.use(authenticate);

router.get('/organizations', controller.organizations);
router.get('/sectors', controller.sectors);
router.get('/departments', controller.departments);
router.get('/categories', controller.categories);
router.get('/subcategories', controller.subcategories);
router.get('/impact-areas', controller.impactAreas);
router.get('/roles', controller.roles);

module.exports = router;

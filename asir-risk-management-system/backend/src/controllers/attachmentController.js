const path = require('path');
const fs = require('fs');
const attachmentModel = require('../models/attachmentModel');
const { ApiError } = require('../middleware/errorHandler');
const env = require('../config/env');

async function upload(req, res, next) {
  try {
    if (!req.file) throw new ApiError(400, 'VALIDATION_ERROR', 'No file uploaded');
    const { entityType, entityId } = req.body;

    const attachment = await attachmentModel.create({
      entityType, entityId,
      fileName: req.file.originalname,
      filePath: req.file.filename,
      mimeType: req.file.mimetype,
      fileSize: req.file.size,
      uploadedBy: req.user.id
    });
    res.status(201).json(attachment);
  } catch (err) { next(err); }
}

async function listFor(req, res, next) {
  try {
    const rows = await attachmentModel.listFor(req.query.entityType, req.query.entityId);
    res.json(rows);
  } catch (err) { next(err); }
}

async function download(req, res, next) {
  try {
    const attachment = await attachmentModel.findById(req.params.id);
    if (!attachment) throw new ApiError(404, 'NOT_FOUND', 'Attachment not found');
    const filePath = path.join(env.uploads.dir, attachment.file_path);
    if (!fs.existsSync(filePath)) throw new ApiError(404, 'NOT_FOUND', 'File no longer exists on disk');
    res.download(filePath, attachment.file_name);
  } catch (err) { next(err); }
}

async function remove(req, res, next) {
  try {
    const attachment = await attachmentModel.findById(req.params.id);
    if (attachment) {
      const filePath = path.join(env.uploads.dir, attachment.file_path);
      fs.unlink(filePath, () => {});
      await attachmentModel.remove(req.params.id);
    }
    res.status(204).send();
  } catch (err) { next(err); }
}

module.exports = { upload, listFor, download, remove };

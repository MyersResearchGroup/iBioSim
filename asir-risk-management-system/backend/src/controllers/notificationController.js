const notificationModel = require('../models/notificationModel');

async function list(req, res, next) {
  try {
    const rows = await notificationModel.listForUser(req.user.id, { unreadOnly: req.query.unreadOnly === 'true' });
    const unread = await notificationModel.unreadCount(req.user.id);
    res.json({ data: rows, unreadCount: unread });
  } catch (err) { next(err); }
}

async function markRead(req, res, next) {
  try {
    await notificationModel.markRead(req.params.id, req.user.id);
    res.status(204).send();
  } catch (err) { next(err); }
}

async function markAllRead(req, res, next) {
  try {
    await notificationModel.markAllRead(req.user.id);
    res.status(204).send();
  } catch (err) { next(err); }
}

module.exports = { list, markRead, markAllRead };

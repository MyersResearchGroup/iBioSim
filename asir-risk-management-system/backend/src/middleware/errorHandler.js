class ApiError extends Error {
  constructor(status, code, message) {
    super(message);
    this.status = status;
    this.code = code;
  }
}

function notFoundHandler(req, res) {
  res.status(404).json({ error: 'NOT_FOUND', message: `Route ${req.method} ${req.originalUrl} not found` });
}

// eslint-disable-next-line no-unused-vars
function errorHandler(err, req, res, next) {
  // eslint-disable-next-line no-console
  console.error(err);

  if (err instanceof ApiError) {
    return res.status(err.status).json({ error: err.code, message: err.message });
  }

  if (err.code === '23505') { // postgres unique_violation
    return res.status(409).json({ error: 'CONFLICT', message: 'Resource already exists' });
  }
  if (err.code === '23503') { // postgres foreign_key_violation
    return res.status(409).json({ error: 'CONFLICT', message: 'Related resource not found or in use' });
  }

  return res.status(500).json({ error: 'INTERNAL_ERROR', message: 'An unexpected error occurred' });
}

module.exports = { ApiError, notFoundHandler, errorHandler };

const xss = require('xss');

/**
 * Validates req[part] against a Joi schema, strips unknown keys,
 * and sanitizes string fields against XSS.
 */
function validate(schema, part = 'body') {
  return (req, res, next) => {
    const { error, value } = schema.validate(req[part], {
      abortEarly: false,
      stripUnknown: true
    });

    if (error) {
      return res.status(422).json({
        error: 'VALIDATION_ERROR',
        details: error.details.map((d) => ({ field: d.path.join('.'), message: d.message }))
      });
    }

    req[part] = sanitizeDeep(value);
    return next();
  };
}

function sanitizeDeep(value) {
  if (typeof value === 'string') return xss(value);
  if (Array.isArray(value)) return value.map(sanitizeDeep);
  if (value instanceof Date) return value;
  if (value && typeof value === 'object') {
    const out = {};
    for (const [k, v] of Object.entries(value)) out[k] = sanitizeDeep(v);
    return out;
  }
  return value;
}

module.exports = { validate };

/**
 * Role-based access control.
 * Usage: router.post('/risks', authenticate, requireRole('ADMIN', 'MANAGER', 'RISK_OWNER'), controller.create)
 */
function requireRole(...allowedRoles) {
  return (req, res, next) => {
    if (!req.user) {
      return res.status(401).json({ error: 'UNAUTHENTICATED' });
    }
    if (!allowedRoles.includes(req.user.role)) {
      return res.status(403).json({
        error: 'FORBIDDEN',
        message: `Role '${req.user.role}' is not permitted to perform this action`
      });
    }
    return next();
  };
}

module.exports = { requireRole };

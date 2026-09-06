const dashboardModel = require('../models/dashboardModel');

async function summary(req, res, next) {
  try {
    const [kpis, byCategory, byLevel, bySector, trend, overdue] = await Promise.all([
      dashboardModel.kpiSummary(),
      dashboardModel.risksByCategory(),
      dashboardModel.risksByResidualLevel(),
      dashboardModel.risksBySector(),
      dashboardModel.riskTrend(6),
      dashboardModel.overdueMitigations()
    ]);
    res.json({ kpis, byCategory, byLevel, bySector, trend, overdueMitigations: overdue });
  } catch (err) { next(err); }
}

module.exports = { summary };

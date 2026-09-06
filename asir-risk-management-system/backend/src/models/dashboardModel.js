const { query } = require('../config/db');

async function kpiSummary() {
  const { rows } = await query(`
    SELECT
      COUNT(*) AS total_risks,
      COUNT(*) FILTER (WHERE status = 'OPEN') AS open_risks,
      COUNT(*) FILTER (WHERE status = 'MITIGATING') AS mitigating_risks,
      COUNT(*) FILTER (WHERE status = 'CLOSED') AS closed_risks,
      COUNT(*) FILTER (WHERE is_top_risk) AS top_risks,
      COUNT(*) FILTER (WHERE residual_risk_level IN ('مستوى 1','مستوى 2')) AS critical_or_high_risks
    FROM risks
  `);
  return rows[0];
}

async function risksByCategory() {
  const { rows } = await query(`
    SELECT c.name_ar, c.name_en, COUNT(r.id) AS count
    FROM risk_categories c LEFT JOIN risks r ON r.category_id = c.id
    GROUP BY c.id ORDER BY count DESC
  `);
  return rows;
}

async function risksByResidualLevel() {
  const { rows } = await query(`
    SELECT COALESCE(residual_risk_level, 'غير محدد') AS level, COUNT(*) AS count
    FROM risks GROUP BY residual_risk_level ORDER BY level
  `);
  return rows;
}

async function risksBySector() {
  const { rows } = await query(`
    SELECT s.name_ar, COUNT(r.id) AS count
    FROM sectors s LEFT JOIN risks r ON r.sector_id = s.id
    GROUP BY s.id ORDER BY count DESC LIMIT 10
  `);
  return rows;
}

async function riskTrend(months = 6) {
  const { rows } = await query(`
    SELECT to_char(date_trunc('month', identification_date), 'YYYY-MM') AS month, COUNT(*) AS count
    FROM risks
    WHERE identification_date >= (CURRENT_DATE - ($1 || ' months')::interval)
    GROUP BY 1 ORDER BY 1
  `, [months]);
  return rows;
}

async function overdueMitigations() {
  const { rows } = await query(`
    SELECT mp.*, r.risk_code, r.risk_statement FROM mitigation_plans mp
    JOIN risks r ON r.id = mp.risk_id
    WHERE mp.due_date < CURRENT_DATE AND mp.status NOT IN ('DONE','CANCELLED')
    ORDER BY mp.due_date ASC LIMIT 20
  `);
  return rows;
}

module.exports = { kpiSummary, risksByCategory, risksByResidualLevel, risksBySector, riskTrend, overdueMitigations };

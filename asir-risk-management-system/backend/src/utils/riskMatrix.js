/**
 * Risk scoring logic mirroring the source Excel register:
 *  - Inherent Risk Rating = f(Likelihood 1-5, Impact 1-5) via a 5x5 matrix.
 *  - Control Environment Sufficiency = derived from the 4 yes/no control questions.
 *  - Residual Risk Level / Priority = f(Inherent Rating, Control Sufficiency).
 */

const INHERENT_MATRIX = [
  // impact 1     2        3        4        5           (rows = likelihood 1..5)
  ['منخفض', 'منخفض', 'متوسط', 'متوسط', 'عالي'],
  ['منخفض', 'متوسط', 'متوسط', 'عالي', 'عالي'],
  ['متوسط', 'متوسط', 'عالي', 'عالي', 'حرج'],
  ['متوسط', 'عالي', 'عالي', 'حرج', 'حرج'],
  ['عالي', 'عالي', 'حرج', 'حرج', 'حرج']
];

function inherentRiskRating(likelihood, impact) {
  if (!likelihood || !impact) return null;
  const li = Math.min(Math.max(likelihood, 1), 5) - 1;
  const im = Math.min(Math.max(impact, 1), 5) - 1;
  return INHERENT_MATRIX[li][im];
}

/**
 * Control sufficiency scored 0-4 based on the 4 boolean control questions
 * from the register (activated & approved / shared & implemented / fully
 * committed / effective without failure).
 */
function controlEnvironmentSufficiency(control) {
  if (!control) return 'ضعيف';
  const answers = [
    control.is_activated_approved,
    control.is_shared_implemented,
    control.is_fully_committed,
    control.is_effective
  ];
  const trueCount = answers.filter(Boolean).length;
  if (trueCount >= 4) return 'جيد';
  if (trueCount >= 2) return 'مقبول';
  return 'ضعيف';
}

const RESIDUAL_LEVEL_BY_RATING = {
  حرج: { جيد: 'مستوى 2', مقبول: 'مستوى 1', ضعيف: 'مستوى 1' },
  عالي: { جيد: 'مستوى 3', مقبول: 'مستوى 2', ضعيف: 'مستوى 2' },
  متوسط: { جيد: 'مستوى 4', مقبول: 'مستوى 3', ضعيف: 'مستوى 3' },
  منخفض: { جيد: 'مستوى 4', مقبول: 'مستوى 4', ضعيف: 'مستوى 3' }
};

const PRIORITY_BY_LEVEL = {
  'مستوى 1': 'أولوية 1',
  'مستوى 2': 'أولوية 5',
  'مستوى 3': 'أولوية 10',
  'مستوى 4': 'أولوية 15'
};

function residualRisk(inherentRating, controlSufficiency) {
  if (!inherentRating) return { level: null, priority: null };
  const level = RESIDUAL_LEVEL_BY_RATING[inherentRating]?.[controlSufficiency] || 'مستوى 3';
  const priority = PRIORITY_BY_LEVEL[level] || 'أولوية 10';
  return { level, priority };
}

/**
 * Recomputes every derived rating field for a risk given its raw inputs.
 * impactAreaScores: array of {score} rows already saved for the risk.
 */
function computeRiskRatings({ likelihoodScore, impactAreaScores, control }) {
  const impactScore = impactAreaScores && impactAreaScores.length
    ? Math.max(...impactAreaScores.map((r) => r.score))
    : null;

  const inherent = inherentRiskRating(likelihoodScore, impactScore);
  const sufficiency = controlEnvironmentSufficiency(control);
  const { level, priority } = residualRisk(inherent, sufficiency);

  return {
    impactScore,
    inherentRiskRating: inherent,
    controlEnvironmentSufficiency: sufficiency,
    residualRiskLevel: level,
    residualRiskPriority: priority
  };
}

module.exports = {
  inherentRiskRating,
  controlEnvironmentSufficiency,
  residualRisk,
  computeRiskRatings
};

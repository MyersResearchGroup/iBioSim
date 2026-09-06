const {
  inherentRiskRating,
  controlEnvironmentSufficiency,
  residualRisk,
  computeRiskRatings
} = require('../riskMatrix');

describe('inherentRiskRating', () => {
  test('low likelihood + low impact => منخفض', () => {
    expect(inherentRiskRating(1, 1)).toBe('منخفض');
  });

  test('max likelihood + max impact => حرج', () => {
    expect(inherentRiskRating(5, 5)).toBe('حرج');
  });

  test('returns null when inputs are missing', () => {
    expect(inherentRiskRating(null, 3)).toBeNull();
    expect(inherentRiskRating(3, undefined)).toBeNull();
  });
});

describe('controlEnvironmentSufficiency', () => {
  test('no control record => ضعيف', () => {
    expect(controlEnvironmentSufficiency(null)).toBe('ضعيف');
  });

  test('all four flags true => جيد', () => {
    const control = { is_activated_approved: true, is_shared_implemented: true, is_fully_committed: true, is_effective: true };
    expect(controlEnvironmentSufficiency(control)).toBe('جيد');
  });

  test('two flags true => مقبول', () => {
    const control = { is_activated_approved: true, is_shared_implemented: true, is_fully_committed: false, is_effective: false };
    expect(controlEnvironmentSufficiency(control)).toBe('مقبول');
  });

  test('zero or one flag true => ضعيف', () => {
    const control = { is_activated_approved: true, is_shared_implemented: false, is_fully_committed: false, is_effective: false };
    expect(controlEnvironmentSufficiency(control)).toBe('ضعيف');
  });
});

describe('residualRisk', () => {
  test('حرج inherent + ضعيف controls => highest priority level 1', () => {
    const { level, priority } = residualRisk('حرج', 'ضعيف');
    expect(level).toBe('مستوى 1');
    expect(priority).toBe('أولوية 1');
  });

  test('منخفض inherent + جيد controls => level 4', () => {
    const { level } = residualRisk('منخفض', 'جيد');
    expect(level).toBe('مستوى 4');
  });

  test('missing inherent rating => nulls', () => {
    expect(residualRisk(null, 'جيد')).toEqual({ level: null, priority: null });
  });
});

describe('computeRiskRatings', () => {
  test('derives impact score as the max of applicable impact areas', () => {
    const result = computeRiskRatings({
      likelihoodScore: 5,
      impactAreaScores: [{ score: 2 }, { score: 5 }, { score: 3 }],
      control: null
    });
    expect(result.impactScore).toBe(5);
    expect(result.inherentRiskRating).toBe('حرج');
    expect(result.controlEnvironmentSufficiency).toBe('ضعيف');
    expect(result.residualRiskLevel).toBe('مستوى 1');
  });

  test('no impact scores yields null impact and inherent rating', () => {
    const result = computeRiskRatings({ likelihoodScore: 3, impactAreaScores: [], control: null });
    expect(result.impactScore).toBeNull();
    expect(result.inherentRiskRating).toBeNull();
  });
});

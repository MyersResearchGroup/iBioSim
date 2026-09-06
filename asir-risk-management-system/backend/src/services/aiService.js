const aiProvider = require('../utils/aiProvider');
const aiQueryModel = require('../models/aiQueryModel');
const riskModel = require('../models/riskModel');
const env = require('../config/env');

const SYSTEM_PROMPT_AR =
  'أنت مساعد ذكاء اصطناعي متخصص في إدارة المخاطر المؤسسية لتجمع عسير الصحي التابع لشركة الصحة القابضة. ' +
  'أجب بإيجاز ووضوح، واقترح خطط تخفيف عملية عند سؤالك عن خطر معين.';

async function chat({ userId, prompt, contextRiskId }) {
  let context = '';
  if (contextRiskId) {
    const risk = await riskModel.findById(contextRiskId);
    if (risk) {
      context = `\n\nسياق الخطر:\nرمز الخطر: ${risk.risk_code}\nوصف الخطر: ${risk.risk_statement}\nالأسباب الجذرية: ${risk.root_causes || '-'}\nالتقييم الكامن: ${risk.inherent_risk_rating || '-'}\nالمستوى المتبقي: ${risk.residual_risk_level || '-'}`;
    }
  }

  const response = await aiProvider.generate(`${prompt}${context}`, { system: SYSTEM_PROMPT_AR });

  await aiQueryModel.record({
    userId,
    provider: env.ai.provider,
    prompt,
    response,
    contextEntity: contextRiskId ? 'risk' : null,
    contextId: contextRiskId || null
  });

  return { provider: env.ai.provider, response };
}

async function suggestMitigations({ userId, riskId }) {
  const risk = await riskModel.findById(riskId);
  const prompt = `اقترح 3 خطط تخفيف عملية وقابلة للتنفيذ لهذا الخطر:\n"${risk.risk_statement}"\nالأسباب الجذرية: ${risk.root_causes || 'غير محددة'}.\nقدم الإجابة كقائمة نقطية موجزة.`;
  return chat({ userId, prompt, contextRiskId: riskId });
}

module.exports = { chat, suggestMitigations };

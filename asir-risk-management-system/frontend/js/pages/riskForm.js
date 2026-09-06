import { api } from '../api.js';
import { t } from '../i18n.js';
import { escapeHtml, toast } from '../ui.js';
import { navigate } from '../router.js';

export async function renderRiskForm(lang, riskId) {
  const isNew = !riskId || riskId === 'new';
  document.getElementById('page-title').textContent = isNew ? t('new_risk', lang) : t('risk_register', lang);
  const content = document.getElementById('page-content');
  content.innerHTML = `<div class="text-slate-400">${t('loading', lang)}</div>`;

  const [organizations, categories, impactAreas, risk] = await Promise.all([
    api.get('/lookups/organizations'),
    api.get('/lookups/categories'),
    api.get('/lookups/impact-areas'),
    isNew ? Promise.resolve(null) : api.get(`/risks/${riskId}`)
  ]);

  window.__currentRiskContext = isNew ? null : riskId;

  const impactScoreFor = (areaId) => risk?.impactScores?.find((s) => s.impact_area_id === areaId)?.score || '';

  content.innerHTML = `
    <form id="risk-form" class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <div class="lg:col-span-2 space-y-4">
        <div class="bg-[var(--surface)] border border-[var(--border)] rounded-xl p-5 space-y-4">
          <div>
            <label class="text-sm font-medium">${t('organization', lang)}</label>
            <select id="organizationId" required class="mt-1 w-full border border-[var(--border)] rounded-lg px-3 py-2 bg-transparent">
              ${organizations.map((o) => `<option value="${o.id}" ${risk?.organization_id === o.id ? 'selected' : ''}>${escapeHtml(o.name_ar)}</option>`).join('')}
            </select>
          </div>
          <div>
            <label class="text-sm font-medium">${t('risk_statement', lang)}</label>
            <textarea id="riskStatement" required rows="3" class="mt-1 w-full border border-[var(--border)] rounded-lg px-3 py-2 bg-transparent">${escapeHtml(risk?.risk_statement || '')}</textarea>
          </div>
          <div>
            <label class="text-sm font-medium">${t('root_causes', lang)}</label>
            <textarea id="rootCauses" rows="2" class="mt-1 w-full border border-[var(--border)] rounded-lg px-3 py-2 bg-transparent">${escapeHtml(risk?.root_causes || '')}</textarea>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="text-sm font-medium">${t('category', lang)}</label>
              <select id="categoryId" class="mt-1 w-full border border-[var(--border)] rounded-lg px-3 py-2 bg-transparent">
                <option value="">-</option>
                ${categories.map((c) => `<option value="${c.id}" ${risk?.category_id === c.id ? 'selected' : ''}>${escapeHtml(c.name_ar)}</option>`).join('')}
              </select>
            </div>
            <div>
              <label class="text-sm font-medium">${t('likelihood', lang)} (1-5)</label>
              <input type="number" min="1" max="5" id="likelihoodScore" value="${risk?.likelihood_score || ''}" class="mt-1 w-full border border-[var(--border)] rounded-lg px-3 py-2 bg-transparent" />
            </div>
          </div>
        </div>

        <div class="bg-[var(--surface)] border border-[var(--border)] rounded-xl p-5">
          <h3 class="font-semibold mb-3">${t('impact', lang)} (1-5 ${lang === 'ar' ? 'لكل منطقة تنطبق' : 'per applicable area'})</h3>
          <div class="grid grid-cols-2 md:grid-cols-3 gap-3">
            ${impactAreas.map((a) => `
              <div class="flex items-center justify-between gap-2 text-sm border border-[var(--border)] rounded-lg px-3 py-2">
                <span>${escapeHtml(a.name_ar)}</span>
                <input type="number" min="0" max="5" data-impact-area="${a.id}" value="${impactScoreFor(a.id)}" class="w-14 border border-[var(--border)] rounded px-1 py-0.5 bg-transparent text-center" />
              </div>`).join('')}
          </div>
        </div>

        ${!isNew ? renderControlsSection(risk, lang) : ''}
        ${!isNew ? renderMitigationsSection(risk, lang) : ''}
      </div>

      <div class="space-y-4">
        <div class="bg-[var(--surface)] border border-[var(--border)] rounded-xl p-5 space-y-2 text-sm">
          <h3 class="font-semibold mb-2">${t('inherent_rating', lang)}</h3>
          <div class="flex justify-between"><span>${t('inherent_rating', lang)}</span><b>${risk?.inherent_risk_rating || '-'}</b></div>
          <div class="flex justify-between"><span>${t('residual_level', lang)}</span><b>${risk?.residual_risk_level || '-'}</b></div>
          <div class="flex justify-between"><span>Priority</span><b>${risk?.residual_risk_priority || '-'}</b></div>
          ${!isNew ? `<button type="button" id="suggest-btn" class="w-full mt-3 bg-violet-600 text-white rounded-lg py-2 text-sm">${t('suggest_mitigations', lang)}</button>` : ''}
        </div>
        <div class="flex gap-2">
          <button class="flex-1 bg-sky-600 text-white rounded-lg py-2.5 font-semibold">${t('save', lang)}</button>
          <button type="button" id="cancel-btn" class="flex-1 border border-[var(--border)] rounded-lg py-2.5">${t('cancel', lang)}</button>
        </div>
      </div>
    </form>
  `;

  document.getElementById('cancel-btn').addEventListener('click', () => navigate('/risks'));

  document.getElementById('risk-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const impactAreaScores = [...content.querySelectorAll('[data-impact-area]')]
      .map((input) => ({ impactAreaId: Number(input.dataset.impactArea), score: Number(input.value) }))
      .filter((s) => s.score >= 1 && s.score <= 5);

    try {
      if (isNew) {
        const created = await api.post('/risks', {
          organizationId: Number(document.getElementById('organizationId').value),
          riskStatement: document.getElementById('riskStatement').value,
          rootCauses: document.getElementById('rootCauses').value,
          categoryId: document.getElementById('categoryId').value ? Number(document.getElementById('categoryId').value) : null,
          likelihoodScore: document.getElementById('likelihoodScore').value ? Number(document.getElementById('likelihoodScore').value) : null,
          impactAreaScores
        });
        toast('Created', 'success');
        navigate(`/risks/${created.id}`);
      } else {
        await api.patch(`/risks/${riskId}`, {
          risk_statement: document.getElementById('riskStatement').value,
          root_causes: document.getElementById('rootCauses').value,
          category_id: document.getElementById('categoryId').value ? Number(document.getElementById('categoryId').value) : null,
          likelihood_score: document.getElementById('likelihoodScore').value ? Number(document.getElementById('likelihoodScore').value) : null,
          impactAreaScores
        });
        toast('Saved', 'success');
        renderRiskForm(lang, riskId);
      }
    } catch (err) {
      toast(err.payload?.message || t('error_occurred', lang), 'error');
    }
  });

  if (!isNew) {
    bindControlsEvents(content, riskId, lang);
    bindMitigationsEvents(content, riskId, lang);
    document.getElementById('suggest-btn')?.addEventListener('click', async () => {
      try {
        toast('...', 'info');
        const { response } = await api.post(`/ai/risks/${riskId}/suggest-mitigations`, {});
        alert(response);
      } catch (err) {
        toast(err.payload?.message || t('error_occurred', lang), 'error');
      }
    });
  }
}

function renderControlsSection(risk, lang) {
  const control = risk.controls?.[0];
  if (!control) return '';
  return `
    <div class="bg-[var(--surface)] border border-[var(--border)] rounded-xl p-5" id="controls-section" data-control-id="${control.id}">
      <h3 class="font-semibold mb-3">${t('controls', lang)} (${control.control_code})</h3>
      <textarea id="control-desc" rows="2" class="w-full border border-[var(--border)] rounded-lg px-3 py-2 bg-transparent mb-3" placeholder="Control description">${escapeHtml(control.description || '')}</textarea>
      <div class="grid grid-cols-2 gap-2 text-sm mb-3">
        ${['is_activated_approved', 'is_shared_implemented', 'is_fully_committed', 'is_effective'].map((k) => `
          <label class="flex items-center gap-2"><input type="checkbox" data-control-flag="${k}" ${control[k] ? 'checked' : ''}/> ${k.replace(/_/g, ' ')}</label>
        `).join('')}
      </div>
      <button type="button" id="save-control-btn" class="text-sm bg-slate-700 text-white rounded-lg px-3 py-1.5">${t('save', lang)}</button>
    </div>`;
}

function bindControlsEvents(content, riskId, lang) {
  const section = content.querySelector('#controls-section');
  if (!section) return;
  section.querySelector('#save-control-btn').addEventListener('click', async () => {
    const controlId = section.dataset.controlId;
    const fields = { description: section.querySelector('#control-desc').value };
    section.querySelectorAll('[data-control-flag]').forEach((cb) => { fields[cb.dataset.controlFlag] = cb.checked; });
    try {
      await api.patch(`/risks/controls/${controlId}`, fields);
      toast('Saved', 'success');
      renderRiskForm(lang, riskId);
    } catch (err) { toast(err.payload?.message || t('error_occurred', lang), 'error'); }
  });
}

function renderMitigationsSection(risk, lang) {
  return `
    <div class="bg-[var(--surface)] border border-[var(--border)] rounded-xl p-5" id="mitigations-section">
      <h3 class="font-semibold mb-3">${t('mitigation_plans', lang)}</h3>
      <ul class="space-y-2 mb-3 text-sm">
        ${(risk.mitigations || []).map((m) => `<li class="border border-[var(--border)] rounded-lg px-3 py-2 flex justify-between">
          <span>${escapeHtml(m.description)}</span><span class="text-xs text-slate-400">${m.status}</span></li>`).join('') || `<li class="text-slate-400">${t('no_data', lang)}</li>`}
      </ul>
      <div class="flex gap-2">
        <input id="new-mitigation-desc" placeholder="${t('add_mitigation', lang)}" class="flex-1 border border-[var(--border)] rounded-lg px-3 py-2 bg-transparent text-sm" />
        <button type="button" id="add-mitigation-btn" class="bg-slate-700 text-white rounded-lg px-3 text-sm">+</button>
      </div>
    </div>`;
}

function bindMitigationsEvents(content, riskId, lang) {
  const btn = content.querySelector('#add-mitigation-btn');
  if (!btn) return;
  btn.addEventListener('click', async () => {
    const description = content.querySelector('#new-mitigation-desc').value.trim();
    if (!description) return;
    try {
      await api.post(`/risks/${riskId}/mitigations`, { description });
      renderRiskForm(lang, riskId);
    } catch (err) { toast(err.payload?.message || t('error_occurred', lang), 'error'); }
  });
}

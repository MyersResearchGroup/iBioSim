import { api } from '../api.js';
import { el, escapeHtml, toast } from '../ui.js';
import { t } from '../i18n.js';

export function mountAiAssistant(lang) {
  if (document.getElementById('ai-widget')) return;

  const widget = el(`
    <div id="ai-widget" class="fixed bottom-5 rtl:left-5 ltr:right-5 z-50">
      <button id="ai-fab" class="w-14 h-14 rounded-full bg-sky-600 text-white shadow-xl flex items-center justify-center text-2xl">💬</button>
      <div id="ai-panel" class="hidden absolute bottom-16 rtl:left-0 ltr:right-0 w-80 bg-[var(--surface)] text-[var(--text)] rounded-xl shadow-2xl border border-[var(--border)] flex flex-col overflow-hidden" style="height: 420px;">
        <div class="bg-sky-600 text-white px-4 py-3 font-semibold flex items-center justify-between">
          <span>${t('ai_assistant', lang)}</span>
          <button id="ai-close" class="text-white/80">&times;</button>
        </div>
        <div id="ai-messages" class="flex-1 overflow-y-auto p-3 space-y-2 text-sm"></div>
        <form id="ai-form" class="border-t border-[var(--border)] p-2 flex gap-2">
          <input id="ai-input" class="flex-1 border border-[var(--border)] rounded-lg px-3 py-2 text-sm bg-transparent" placeholder="${t('ask_ai', lang)}" />
          <button class="bg-sky-600 text-white rounded-lg px-3 text-sm">➤</button>
        </form>
      </div>
    </div>
  `);
  document.body.appendChild(widget);

  const panel = widget.querySelector('#ai-panel');
  widget.querySelector('#ai-fab').addEventListener('click', () => panel.classList.toggle('hidden'));
  widget.querySelector('#ai-close').addEventListener('click', () => panel.classList.add('hidden'));

  widget.querySelector('#ai-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const input = widget.querySelector('#ai-input');
    const prompt = input.value.trim();
    if (!prompt) return;
    input.value = '';
    appendMessage(widget, prompt, 'user');
    appendMessage(widget, '...', 'assistant', 'ai-pending');

    try {
      const contextRiskId = window.__currentRiskContext || null;
      const { response } = await api.post('/ai/chat', { prompt, riskId: contextRiskId });
      document.getElementById('ai-pending')?.remove();
      appendMessage(widget, response, 'assistant');
    } catch (err) {
      document.getElementById('ai-pending')?.remove();
      appendMessage(widget, err.payload?.message || t('error_occurred', lang), 'assistant');
      if (err.status === 503) toast('AI provider not configured. Set AI_PROVIDER in backend .env', 'warning');
    }
  });
}

function appendMessage(widget, text, role, id) {
  const bubble = el(`
    <div ${id ? `id="${id}"` : ''} class="max-w-[85%] px-3 py-2 rounded-lg whitespace-pre-wrap ${role === 'user' ? 'bg-sky-600 text-white ms-auto' : 'bg-slate-100 dark:bg-slate-800'}">
      ${escapeHtml(text)}
    </div>`);
  const messages = widget.querySelector('#ai-messages');
  messages.appendChild(bubble);
  messages.scrollTop = messages.scrollHeight;
}

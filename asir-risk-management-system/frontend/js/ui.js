export function el(html) {
  const template = document.createElement('template');
  template.innerHTML = html.trim();
  return template.content.firstElementChild;
}

export function toast(message, type = 'info') {
  const colors = {
    info: 'bg-slate-800', success: 'bg-emerald-600', error: 'bg-rose-600', warning: 'bg-amber-600'
  };
  const node = el(`<div class="${colors[type] || colors.info} text-white px-4 py-2 rounded-lg shadow-lg text-sm mb-2 animate-fade-in">${escapeHtml(message)}</div>`);
  const container = document.getElementById('toast-container');
  container.appendChild(node);
  setTimeout(() => node.remove(), 4000);
}

export function escapeHtml(str) {
  const div = document.createElement('div');
  div.textContent = String(str ?? '');
  return div.innerHTML;
}

export function openModal(contentHtml) {
  const overlay = document.getElementById('modal-root');
  overlay.innerHTML = `
    <div class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4" id="modal-overlay">
      <div class="bg-[var(--surface)] text-[var(--text)] rounded-xl shadow-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto p-6">
        ${contentHtml}
      </div>
    </div>`;
  overlay.querySelector('#modal-overlay').addEventListener('click', (e) => {
    if (e.target.id === 'modal-overlay') closeModal();
  });
  return overlay;
}

export function closeModal() {
  document.getElementById('modal-root').innerHTML = '';
}

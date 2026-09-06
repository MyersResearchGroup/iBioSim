const fetch = require('node-fetch');
const env = require('../config/env');
const { ApiError } = require('../middleware/errorHandler');

/**
 * Thin adapter over four possible AI backends so the rest of the app
 * (aiService/aiController) never needs to know which one is configured.
 * Switch providers purely via environment variables (see .env.example) —
 * no code changes required.
 */
async function callClaude(prompt, system) {
  const { apiKey, model, baseUrl } = env.ai.claude;
  const res = await fetch(baseUrl, {
    method: 'POST',
    headers: {
      'content-type': 'application/json',
      'x-api-key': apiKey,
      'anthropic-version': '2023-06-01'
    },
    body: JSON.stringify({
      model,
      max_tokens: 1024,
      system,
      messages: [{ role: 'user', content: prompt }]
    })
  });
  if (!res.ok) throw new ApiError(502, 'AI_PROVIDER_ERROR', `Claude API error: ${res.status}`);
  const data = await res.json();
  return data.content?.[0]?.text || '';
}

async function callOpenAI(prompt, system) {
  const { apiKey, model, baseUrl } = env.ai.openai;
  const res = await fetch(baseUrl, {
    method: 'POST',
    headers: { 'content-type': 'application/json', authorization: `Bearer ${apiKey}` },
    body: JSON.stringify({
      model,
      messages: [
        { role: 'system', content: system },
        { role: 'user', content: prompt }
      ]
    })
  });
  if (!res.ok) throw new ApiError(502, 'AI_PROVIDER_ERROR', `OpenAI API error: ${res.status}`);
  const data = await res.json();
  return data.choices?.[0]?.message?.content || '';
}

async function callGemini(prompt, system) {
  const { apiKey, model, baseUrl } = env.ai.gemini;
  const url = `${baseUrl}/${model}:generateContent?key=${apiKey}`;
  const res = await fetch(url, {
    method: 'POST',
    headers: { 'content-type': 'application/json' },
    body: JSON.stringify({
      systemInstruction: { parts: [{ text: system }] },
      contents: [{ role: 'user', parts: [{ text: prompt }] }]
    })
  });
  if (!res.ok) throw new ApiError(502, 'AI_PROVIDER_ERROR', `Gemini API error: ${res.status}`);
  const data = await res.json();
  return data.candidates?.[0]?.content?.parts?.[0]?.text || '';
}

async function callOllama(prompt, system) {
  const { model, baseUrl } = env.ai.ollama;
  const res = await fetch(baseUrl, {
    method: 'POST',
    headers: { 'content-type': 'application/json' },
    body: JSON.stringify({ model, prompt: `${system}\n\n${prompt}`, stream: false })
  });
  if (!res.ok) throw new ApiError(502, 'AI_PROVIDER_ERROR', `Ollama (local Llama) error: ${res.status}`);
  const data = await res.json();
  return data.response || '';
}

async function generate(prompt, { system = 'You are a helpful enterprise risk management assistant.' } = {}) {
  switch (env.ai.provider) {
    case 'claude':
      return callClaude(prompt, system);
    case 'openai':
      return callOpenAI(prompt, system);
    case 'gemini':
      return callGemini(prompt, system);
    case 'ollama':
      return callOllama(prompt, system);
    default:
      throw new ApiError(503, 'AI_NOT_CONFIGURED', 'No AI_PROVIDER configured. Set AI_PROVIDER to claude|openai|gemini|ollama in .env');
  }
}

module.exports = { generate };

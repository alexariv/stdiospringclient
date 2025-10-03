//CommonJS
const express = require('express');
const path = require('path');
const fs = require('fs');
const yaml = require('js-yaml'); 

const app = express();
app.use(express.json());

app.use(express.static(__dirname));

// read model name from application.yml 
const APP_YAML_PATH = path.join(__dirname, '..', '..', '..', 'resources', 'application.yml');
let cachedModel = null;
app.get('/api/model', (req, res) => {
  try {
    const raw = fs.readFileSync(APP_YAML_PATH, 'utf8');
    const doc = yaml.load(raw);

    // spring.ai.ollama.chat.options.model
    const model =
      doc?.spring?.ai?.ollama?.chat?.options?.model ||
      doc?.spring?.ai?.ollama?.model ||
      'unknown';

    cachedModel = model;
    res.json({ model });
  } catch (e) {
    // If file missing locally, fall back to last good value
    res.status(200).json({ model: cachedModel || 'unknown' });
  }
});

app.post('/api/chat', async (req, res) => {
  const start = process.hrtime.bigint();
  try {
    const response = await fetch('http://127.0.0.1:3000/api/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(req.body),
    });

    const text = await response.text();
    const end = process.hrtime.bigint();
    const upstreamMs = Number(end - start) / 1e6;
    let payload = text;
    try { payload = JSON.parse(text); } catch {}
    // Expose timing header
    res.set('X-Upstream-Duration-ms', upstreamMs.toFixed(1));
    res.set('Access-Control-Allow-Origin', '*');
    res.set('Access-Control-Expose-Headers', 'X-Upstream-Duration-ms');
    res.status(response.status).send(payload);
  } catch (e) {
    const end = process.hrtime.bigint();
    const upstreamMs = Number(end - start) / 1e6;
    res.set('X-Upstream-Duration-ms', upstreamMs.toFixed(1));
    res.status(500).json({ error: String(e) });
  }
});

app.listen(9000, () => {
  console.log('Now running @ http://localhost:9000/index.html');
});
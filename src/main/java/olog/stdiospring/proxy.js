// CommonJS
const express = require('express');
const path = require('path');
const fs = require('fs');
const yaml = require('js-yaml'); 

const app = express();
app.use(express.json());

app.use(express.static(__dirname));

//LLM model
const APP_YAML_PATH = path.join(__dirname, '..', '..', '..', 'resources', 'application.yml');

let cachedModel = null;
app.get('/api/model', (req, res) => {
  try {
    const raw = fs.readFileSync(APP_YAML_PATH, 'utf8');
    const doc = yaml.load(raw);
    const model =
      doc?.spring?.ai?.ollama?.chat?.options?.model ||
      doc?.spring?.ai?.ollama?.model ||
      'unknown';
    cachedModel = model;
    res.json({ model });
  } catch (e) {
    res.json({ model: cachedModel || 'unknown' });
  }
});

//proxy to SSH-tunneled API
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
    const upstreamSec = Number(end - start) / 1e9; 

    let payload = text;
    try { payload = JSON.parse(text); } catch {}

    res.set('X-Upstream-Duration-s', upstreamSec.toFixed(2));
    res.set('Access-Control-Allow-Origin', '*');
    res.set('Access-Control-Expose-Headers', 'X-Upstream-Duration-s');
    res.status(response.status).send(payload);
  } catch (e) {
    const end = process.hrtime.bigint();
    const upstreamSec = Number(end - start) / 1e9;
    res.set('X-Upstream-Duration-s', upstreamSec.toFixed(2));
    res.status(500).json({ error: String(e) });
  }
});

app.listen(9000, () => {
  console.log('Now running @ http://localhost:9000/index.html');
});

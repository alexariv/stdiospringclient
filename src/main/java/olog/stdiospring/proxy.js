//CommonJS
const express = require('express');
const path = require('path');

const app = express();
app.use(express.json());

app.use(express.static(__dirname));

app.post('/api/chat', async (req, res) => {
  try {
    const response = await fetch('http://127.0.0.1:3000/api/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(req.body),
    });

    const text = await response.text();
    console.log('[proxy] upstream status:', response.status, 'body:', text);

    let payload = text;
    try { payload = JSON.parse(text); } catch {}
    res.set('Access-Control-Allow-Origin', '*');
    res.status(response.status).send(payload);

  } catch (e) {
    console.error('[proxy] fetch error:', e);
    res.status(500).json({ error: String(e) });
  }
});

app.listen(9000, () => {
  console.log('Now running @ http://localhost:9000/index.html');
});
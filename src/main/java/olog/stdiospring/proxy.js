//CommonJS
const express = require('express');
const fetch = require('node-fetch'); 
const path = require('path');

const app = express();
app.use(express.json());

// Serve the current folder
app.use(express.static(__dirname));

// SSH-tunneled API
app.post('/api/chat', async (req, res) => {
  try {
     const text = await upstream.text();
    let payload = text;
    try { payload = JSON.parse(text); } catch {}
    res.set('Access-Control-Allow-Origin', '*');
    res.status(upstream.status).send(payload);
  } catch (e) {
    console.error('Proxy fetch error:', e);
    res.status(500).json({ error: String(e) });
  }
});

app.listen(9000, () => {
  console.log('Now running @ http://localhost:9000/index.html');
});


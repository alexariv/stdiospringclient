import express from 'express';
import fetch from 'node-fetch';

const app = express();
app.use(express.json());

// proxy /api/* to the tunneled API
app.post('/api/chat', async (req, res) => {
  try {
    const r = await fetch('http://localhost:3000/api/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(req.body),
    });
    const data = await r.json();
    res.set('Access-Control-Allow-Origin', '*'); // allow the browser page
    res.status(r.status).json(data);
  } catch (e) {
    res.status(500).json({ error: String(e) });
  }
});

app.listen(9000, () => console.log('Proxy on http://localhost:9000'));

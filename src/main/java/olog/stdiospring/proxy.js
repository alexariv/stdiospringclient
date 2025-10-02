import express from 'express';
import fetch from 'node-fetch';

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
    const r = await fetch('http://localhost:3000/api/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(req.body),
    });
    const data = await r.json();
    res.set('Access-Control-Allow-Origin', '*');
    res.status(r.status).json(data);
  } catch (e) {
    res.status(500).json({ error: String(e) });
  }
});

app.listen(9000, () => {
  console.log('UI + Proxy running @ http://localhost:9000');
});


const CACHE_NAME = 'paper-mill-shell-v5-pages';
const APP_SHELL = ['./', './index.html', './manifest.webmanifest', './logo.svg', './page.part01.txt', './page.part02.txt', './page.part03.txt', './page.part04.txt', './page.part05.txt', './page.part06.txt', './page.part07.txt', './page.part08.txt', './page.part09.txt', './page.part10.txt', './page.part11.txt', './page.part12.txt', './page.part13.txt'];

self.addEventListener('install', (event) => { event.waitUntil(caches.open(CACHE_NAME).then((cache) => cache.addAll(APP_SHELL))); self.skipWaiting(); });
self.addEventListener('activate', (event) => { event.waitUntil(caches.keys().then((keys) => Promise.all(keys.filter((key) => key !== CACHE_NAME).map((key) => caches.delete(key))))); self.clients.claim(); });
self.addEventListener('fetch', (event) => {
  const url = new URL(event.request.url);
  if (event.request.method !== 'GET' || url.origin !== self.location.origin) return;
  const networkFirst = event.request.mode === 'navigate' || url.pathname.endsWith('index.html') || url.pathname.endsWith('service-worker.js');
  if (networkFirst) {
    event.respondWith(fetch(event.request).then((response) => { if (response.ok) caches.open(CACHE_NAME).then((cache) => cache.put(event.request.mode === 'navigate' ? './index.html' : event.request, response.clone())); return response; }).catch(() => caches.match(event.request.mode === 'navigate' ? './index.html' : event.request)));
    return;
  }
  event.respondWith(caches.match(event.request).then((cached) => cached || fetch(event.request).then((response) => { if (response.ok) caches.open(CACHE_NAME).then((cache) => cache.put(event.request, response.clone())); return response; })));
});

// Service worker Buku Tani: simpan seluruh berkas aplikasi agar bisa dibuka tanpa internet.
const CACHE = 'bukutani-v3';
const FILES = ['./', './index.html', './manifest.webmanifest', './icon-192.png', './icon-512.png', './icon-512-maskable.png'];
self.addEventListener('install', e => {
  e.waitUntil(caches.open(CACHE).then(c => c.addAll(FILES)).then(() => self.skipWaiting()));
});
self.addEventListener('activate', e => {
  e.waitUntil(caches.keys().then(keys => Promise.all(keys.filter(k => k !== CACHE).map(k => caches.delete(k)))).then(() => self.clients.claim()));
});
self.addEventListener('fetch', e => {
  if (e.request.method !== 'GET') return;
  const host = new URL(e.request.url).host;
  // Data cuaca/lokasi (API) selalu dari jaringan, tidak disimpan di cache.
  if (!(host === location.host || host.endsWith('googleapis.com') || host.endsWith('gstatic.com'))) return;
  e.respondWith(
    caches.match(e.request, { ignoreSearch: true }).then(hit => hit || fetch(e.request).then(res => {
      const h = new URL(e.request.url).host; if (res && (res.ok || res.type === 'opaque') && (h === location.host || h.endsWith('googleapis.com') || h.endsWith('gstatic.com'))) {
        const copy = res.clone(); caches.open(CACHE).then(c => c.put(e.request, copy));
      }
      return res;
    }).catch(() => e.request.mode === 'navigate' ? caches.match('./index.html') : Response.error()))
  );
});

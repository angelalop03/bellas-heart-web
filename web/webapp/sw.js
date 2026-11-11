// Service worker: guarda el juego en el dispositivo para que cargue rápido y funcione sin conexión.
// __BUILD_ID__ lo sustituye Gradle en cada compilación, así cada versión publicada usa una caché nueva.
var CACHE = "bellas-heart-__BUILD_ID__";

var SHELL = [
  "./",
  "index.html",
  "app.js",
  "bellas-db.js",
  "bellas-keyboard.js",
  "manifest.webmanifest",
  "sqljs/sql-wasm.js",
  "sqljs/sql-wasm.wasm",
  "icons/icon-192.png",
  "icons/icon-512.png",
  "icons/apple-touch-icon.png"
];

self.addEventListener("install", function (event) {
  event.waitUntil(
    caches.open(CACHE)
      .then(function (cache) { return cache.addAll(SHELL); })
      .then(function () { return self.skipWaiting(); })
  );
});

self.addEventListener("activate", function (event) {
  event.waitUntil(
    caches.keys()
      .then(function (keys) {
        return Promise.all(keys.map(function (key) {
          if (key !== CACHE) return caches.delete(key);
        }));
      })
      .then(function () { return self.clients.claim(); })
  );
});

function isShell(url) {
  var path = url.pathname;
  return path.endsWith("/") || path.endsWith(".html") || path.endsWith("app.js") ||
    path.endsWith("bellas-db.js") || path.endsWith("bellas-keyboard.js") || path.endsWith(".webmanifest");
}

self.addEventListener("fetch", function (event) {
  var request = event.request;
  if (request.method !== "GET") return;
  var url = new URL(request.url);
  if (url.origin !== self.location.origin) return;

  if (isShell(url)) {
    // Primero la red (para recibir actualizaciones); sin conexión, la copia guardada.
    event.respondWith(
      fetch(request)
        .then(function (response) {
          if (response.ok) {
            var copy = response.clone();
            caches.open(CACHE).then(function (cache) { cache.put(request, copy); });
          }
          return response;
        })
        .catch(function () {
          return caches.match(request, { ignoreSearch: true });
        })
    );
    return;
  }

  // Imágenes, música, fuentes y scripts del motor: primero la copia guardada.
  event.respondWith(
    caches.match(request, { ignoreSearch: true }).then(function (cached) {
      if (cached) return cached;
      return fetch(request).then(function (response) {
        if (response.ok && response.status === 200) {
          var copy = response.clone();
          caches.open(CACHE).then(function (cache) { cache.put(request, copy); });
        }
        return response;
      });
    })
  );
});

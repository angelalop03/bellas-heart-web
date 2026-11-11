// Base de datos del juego en el navegador.
// SQLite compilado a WebAssembly (sql.js) en memoria + copia guardada en IndexedDB para que
// las partidas, usuarios y decisiones se conserven al cerrar la app.
(function () {
  "use strict";

  var IDB_NAME = "bellas-heart";
  var IDB_STORE = "db";
  var IDB_KEY = "game.db";

  var db = null;
  var isNew = false;
  var inTransaction = false;
  var saveTimer = null;

  function openIdb() {
    return new Promise(function (resolve, reject) {
      var req = indexedDB.open(IDB_NAME, 1);
      req.onupgradeneeded = function () {
        req.result.createObjectStore(IDB_STORE);
      };
      req.onsuccess = function () { resolve(req.result); };
      req.onerror = function () { reject(req.error); };
    });
  }

  function idbGet() {
    return openIdb().then(function (idb) {
      return new Promise(function (resolve, reject) {
        var req = idb.transaction(IDB_STORE, "readonly").objectStore(IDB_STORE).get(IDB_KEY);
        req.onsuccess = function () { resolve(req.result || null); };
        req.onerror = function () { reject(req.error); };
      });
    });
  }

  function idbPut(bytes) {
    return openIdb().then(function (idb) {
      return new Promise(function (resolve, reject) {
        var tx = idb.transaction(IDB_STORE, "readwrite");
        tx.objectStore(IDB_STORE).put(bytes, IDB_KEY);
        tx.oncomplete = function () { resolve(); };
        tx.onerror = function () { reject(tx.error); };
      });
    });
  }

  function saveNow() {
    if (saveTimer) {
      clearTimeout(saveTimer);
      saveTimer = null;
    }
    // export() no se puede hacer a mitad de una transacción.
    if (!db || inTransaction) return Promise.resolve();
    return idbPut(db.export()).catch(function (e) {
      console.error("No se pudo guardar la base de datos", e);
    });
  }

  function scheduleSave() {
    if (inTransaction) return;
    if (saveTimer) clearTimeout(saveTimer);
    saveTimer = setTimeout(saveNow, 400);
  }

  function error(e) {
    return JSON.stringify({ error: String(e && e.message ? e.message : e) });
  }

  function exec(sql) {
    try {
      db.exec(sql);
      return "{}";
    } catch (e) {
      return error(e);
    }
  }

  window.BellasDB = {
    init: function () {
      return initSqlJs({ locateFile: function (file) { return "sqljs/" + file; } })
        .then(function (SQL) {
          return idbGet().catch(function () { return null; }).then(function (saved) {
            if (saved) {
              db = new SQL.Database(new Uint8Array(saved));
              isNew = false;
            } else {
              db = new SQL.Database();
              isNew = true;
            }
          });
        });
    },

    isNew: function () { return isNew; },

    query: function (sql, params) {
      var stmt;
      try {
        stmt = db.prepare(sql);
        var p = JSON.parse(params);
        if (p.length) stmt.bind(p);
        var values = [];
        while (stmt.step()) values.push(stmt.get());
        return JSON.stringify({ columns: stmt.getColumnNames(), values: values });
      } catch (e) {
        return error(e);
      } finally {
        if (stmt) stmt.free();
      }
    },

    run: function (sql, params) {
      try {
        var p = JSON.parse(params);
        db.run(sql, p.length ? p : undefined);
        var changes = db.getRowsModified();
        var lastId = db.exec("SELECT last_insert_rowid()")[0].values[0][0];
        scheduleSave();
        return JSON.stringify({ changes: changes, lastId: lastId });
      } catch (e) {
        return error(e);
      }
    },

    begin: function () {
      var r = exec("BEGIN");
      inTransaction = r === "{}";
      return r;
    },

    commit: function () {
      var r = exec("COMMIT");
      inTransaction = false;
      scheduleSave();
      return r;
    },

    rollback: function () {
      var r = exec("ROLLBACK");
      inTransaction = false;
      return r;
    },

    saveNow: saveNow,

    /** Borra la partida guardada en este dispositivo (útil para empezar de cero). */
    reset: function () {
      return openIdb().then(function (idb) {
        return new Promise(function (resolve) {
          var tx = idb.transaction(IDB_STORE, "readwrite");
          tx.objectStore(IDB_STORE).delete(IDB_KEY);
          tx.oncomplete = function () { resolve(); };
        });
      });
    }
  };

  // Guardar antes de que el móvil congele o cierre la pestaña/app.
  document.addEventListener("visibilitychange", function () {
    if (document.visibilityState === "hidden") saveNow();
  });
  window.addEventListener("pagehide", saveNow);
})();

package com.us.TFG.web;

import org.teavm.jso.JSBody;

/**
 * Llamadas a window.BellasDB (ver webapp/bellas-db.js), que envuelve SQLite compilado a WebAssembly (sql.js)
 * y guarda la base de datos en IndexedDB. Los resultados viajan como texto JSON.
 */
final class SqlJsBridge {
    private SqlJsBridge() {
    }

    @JSBody(script = "return window.BellasDB.isNew();")
    static native boolean isNew();

    /** Devuelve {"columns":[...],"values":[[...],...]} o {"error":"..."}. */
    @JSBody(params = {"sql", "params"}, script = "return window.BellasDB.query(sql, params);")
    static native String query(String sql, String params);

    /** Devuelve {"changes":n,"lastId":n} o {"error":"..."}. */
    @JSBody(params = {"sql", "params"}, script = "return window.BellasDB.run(sql, params);")
    static native String run(String sql, String params);

    @JSBody(script = "return window.BellasDB.begin();")
    static native String begin();

    @JSBody(script = "return window.BellasDB.commit();")
    static native String commit();

    @JSBody(script = "return window.BellasDB.rollback();")
    static native String rollback();
}

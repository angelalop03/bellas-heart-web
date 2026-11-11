package com.us.TFG.web;

import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.github.xpenatan.gdx.teavm.backends.web.WebApplication;
import com.github.xpenatan.gdx.teavm.backends.web.WebApplicationConfiguration;
import com.us.TFG.MyTfgGame;
import com.us.TFG.utils.Config;

/** Arranca el juego en el navegador. La base de datos (SQLite WebAssembly) ya la ha preparado index.html. */
public class WebLauncher {
    public static void main(String[] args) {
        WebApplicationConfiguration config = new WebApplicationConfiguration();
        // El juego está maquetado para 1280x768: se dibuja siempre a esa resolución y index.html escala
        // el canvas con CSS para que quepa en cualquier pantalla (la entrada táctil ya corrige esa escala).
        config.width = Config.WIDTH;
        config.height = Config.HEIGHT;
        config.showDownloadLogs = false;
        config.antialiasing = true;
        // Librería FreeType (fuentes .ttf/.otf) compilada a JavaScript.
        config.preloadListener = assetLoader -> assetLoader.loadScript("freetype.js");
        // En el móvil, tocar un campo de texto abre un cuadro con el teclado del sistema.
        TextField.DEFAULT_ONSCREEN_KEYBOARD = new WebOnscreenKeyboard();
        new WebApplication(new MyTfgGame(new SqlJsDatabaseProvider()), config);
    }
}

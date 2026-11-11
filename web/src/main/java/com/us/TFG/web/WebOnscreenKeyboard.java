package com.us.TFG.web;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

/**
 * Teclado para los campos de texto del juego en el navegador del móvil.
 *
 * El juego se dibuja en un canvas, que no puede abrir el teclado del sistema. Al tocar un TextField en un
 * dispositivo táctil se muestra un cuadro HTML con un campo de texto real (ver webapp/bellas-keyboard.js) y,
 * al aceptar, el texto se copia al TextField. En ordenador se escribe directamente sobre el juego, como siempre.
 */
class WebOnscreenKeyboard implements TextField.OnscreenKeyboard {
    private boolean dialogOpen;

    @JSFunctor
    interface TextCallback extends JSObject {
        void onText(String text);
    }

    @JSBody(script = "return !!(window.BellasKeyboard && window.BellasKeyboard.isTouchDevice());")
    private static native boolean isTouchDevice();

    @JSBody(params = {"text", "hint", "password", "multiline", "onDone", "onCancel"},
        script = "window.BellasKeyboard.open(text, hint, password, multiline, onDone, onCancel);")
    private static native void openDialog(String text, String hint, boolean password, boolean multiline,
                                          TextCallback onDone, TextCallback onCancel);

    @Override
    public void show(TextField field) {
        if (dialogOpen || field == null || field.isDisabled() || !isTouchDevice()) return;
        dialogOpen = true;
        String hint = field.getMessageText() == null ? "" : field.getMessageText();
        // Algunos campos traen el texto de ayuda como texto real y lo borran al tocarlos: no copiarlo.
        String current = field.getText().equals(hint) ? "" : field.getText();
        openDialog(current, hint, field.isPasswordMode(), field instanceof TextArea,
            text -> {
                dialogOpen = false;
                Gdx.app.postRunnable(() -> {
                    field.setText(text);
                    field.setCursorPosition(text.length());
                    // Igual que al escribir: avisar a los ChangeListener del campo.
                    field.fire(new ChangeListener.ChangeEvent());
                });
            },
            text -> dialogOpen = false);
    }

    @Override
    public void close() {
    }
}

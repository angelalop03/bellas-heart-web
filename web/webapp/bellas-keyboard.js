// Cuadro de texto HTML para escribir en los campos del juego desde el móvil.
// El juego se dibuja en un <canvas>, que no puede abrir el teclado del sistema; cuando el jugador toca un
// campo de texto, el juego (MobileKeyboardInput.java) llama a BellasKeyboard.open().
(function () {
  "use strict";

  function stop(e) { e.stopPropagation(); }

  // El juego pide el teclado mientras el dedo aún está sobre la pantalla. Si el cuadro se abriera ya,
  // el "soltar" no le llegaría al juego y este creería que sigue habiendo un toque. Por eso se espera a
  // que se suelte; además, abrirlo dentro de ese evento permite que iPhone muestre el teclado directamente.
  var pointerDown = false;
  var pending = null;

  function runPending() {
    if (!pending) return;
    var p = pending;
    pending = null;
    show.apply(null, p);
  }

  window.addEventListener("pointerdown", function () { pointerDown = true; }, true);
  ["pointerup", "pointercancel", "touchend", "mouseup"].forEach(function (type) {
    window.addEventListener(type, function () {
      pointerDown = false;
      runPending();
    }, true);
  });

  window.BellasKeyboard = {
    isTouchDevice: function () {
      return (window.matchMedia && window.matchMedia("(pointer: coarse)").matches) ||
        (navigator.maxTouchPoints || 0) > 0;
    },

    open: function (text, hint, password, multiline, onDone, onCancel) {
      pending = [text, hint, password, multiline, onDone, onCancel];
      if (!pointerDown) setTimeout(runPending, 0);
    }
  };

  function show(text, hint, password, multiline, onDone, onCancel) {
    var old = document.getElementById("kb-overlay");
    if (old) old.remove();

    var overlay = document.createElement("div");
    overlay.id = "kb-overlay";

    var box = document.createElement("div");
    box.id = "kb-box";

    var label = document.createElement("label");
    label.textContent = hint || (password ? "Contraseña" : "Escribe aquí");
    label.htmlFor = "kb-input";

    var input = document.createElement(multiline ? "textarea" : "input");
    input.id = "kb-input";
    if (!multiline) input.type = password ? "password" : "text";
    input.value = text || "";
    input.placeholder = hint || "";
    input.autocomplete = password ? "current-password" : "off";
    input.setAttribute("autocapitalize", "off");
    input.setAttribute("autocorrect", "off");
    input.spellcheck = false;

    var buttons = document.createElement("div");
    buttons.id = "kb-buttons";
    var cancel = document.createElement("button");
    cancel.id = "kb-cancel";
    cancel.type = "button";
    cancel.textContent = "Cancelar";
    var ok = document.createElement("button");
    ok.id = "kb-ok";
    ok.type = "button";
    ok.textContent = "Aceptar";
    buttons.appendChild(cancel);
    buttons.appendChild(ok);

    box.appendChild(label);
    box.appendChild(input);
    box.appendChild(buttons);
    overlay.appendChild(box);

    // Que las teclas y toques no lleguen también al juego.
    ["keydown", "keyup", "keypress", "mousedown", "mouseup", "mousemove", "touchstart", "touchmove",
      "touchend", "wheel"].forEach(function (type) {
      overlay.addEventListener(type, stop);
    });

    var closed = false;
    function close(accepted) {
      if (closed) return;
      closed = true;
      var value = input.value;
      input.blur();
      overlay.remove();
      if (accepted) onDone(value); else onCancel(value);
    }

    ok.addEventListener("click", function () { close(true); });
    cancel.addEventListener("click", function () { close(false); });
    var openedAt = Date.now();
    overlay.addEventListener("click", function (e) {
      // Tocar fuera cierra; se ignora el clic "fantasma" justo al abrir.
      if (e.target === overlay && Date.now() - openedAt > 500) close(false);
    });
    input.addEventListener("keydown", function (e) {
      if (e.key === "Enter" && !multiline) { e.preventDefault(); close(true); }
      if (e.key === "Escape") { e.preventDefault(); close(false); }
    });

    document.body.appendChild(overlay);
    // En Android el teclado se abre solo; en iPhone basta con tocar el campo.
    input.focus();
    try {
      var len = input.value.length;
      input.setSelectionRange(len, len);
    } catch (ignored) { /* type=password en algunos navegadores */ }
  }
})();

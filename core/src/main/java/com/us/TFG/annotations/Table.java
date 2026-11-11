package com.us.TFG.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// Anotación para definir el nombre de la tabla
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
    String name();
}


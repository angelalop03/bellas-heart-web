package com.us.TFG.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// Anotación para definir los parámetros de las columnas
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    String name();
    String type();
    boolean primaryKey() default false;
    boolean notNull() default false;
    boolean unique() default false;
    String defaultValue() default "";
}

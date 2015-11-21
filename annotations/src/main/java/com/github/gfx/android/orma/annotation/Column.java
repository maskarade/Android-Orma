package com.github.gfx.android.orma.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface Column {

    /**
     * @return A column name representation for SQLite tables.
     */
    String value() default "";

    boolean indexed() default false;

    boolean unique() default false;

    /**
     * @return Must be one of {@code "BINARY"}, {@code "NOCASE"} or {@code "RTRIM"}
     */
    String collate() default "";
}

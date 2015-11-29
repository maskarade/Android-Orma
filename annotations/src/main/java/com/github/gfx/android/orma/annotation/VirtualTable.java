package com.github.gfx.android.orma.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a virtual table.
 *
 * See https://www.sqlite.org/fts3.html for details.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface VirtualTable {

    /**
     * @return The table name in SQLite. It is case-insensitive.
     */
    String value() default "";

    String using() default "";

    String content() default "";

}

package com.github.gfx.android.orma.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Table {

    /**
     * @return The table name in SQLite. It is case-insensitive.
     */
    String value() default "";

    String schemaClassName() default "";

    String relationClassName() default "";

    String updaterClassName() default "";

    String deleterClassName() default "";
}

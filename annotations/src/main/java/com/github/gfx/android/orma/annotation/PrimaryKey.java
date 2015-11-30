package com.github.gfx.android.orma.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface PrimaryKey {

    /**
     * Corresponds the {@code AUTOINCREMENT} keyword.
     * Note that this is slower than {@code auto = true}.
     * See https://www.sqlite.org/autoinc.html for details.
     */
    boolean autoincrement() default false;

    /**
     * Tell Orma that the primary key is automatically assigned if the primary key is a primitive integer type.
     * If true, any value you set to this column will be ignored in {@code INSERT}.
     */
    boolean auto() default true;
}

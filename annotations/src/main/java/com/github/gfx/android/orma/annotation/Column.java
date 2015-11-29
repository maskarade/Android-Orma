package com.github.gfx.android.orma.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface Column {

    /**
     * @return The column name in SQLite tables. It is case-insensitive.
     */
    String value() default "";

    /**
     * @return Create an index for the column.
     */
    boolean indexed() default false;

    /**
     * @return Indicates the column value is unique in rows.
     */
    boolean unique() default false;

    /**
     * @return Specifies the DEFAULT expression in terms of DDL. This is useful to fill values in migration.
     */
    String defaultExpr() default "";

    /**
     * @return Specifies how the column is compared. Must be one of {@code "BINARY"}, {@code "NOCASE"} or {@code "RTRIM"}
     */
    String collate() default "";
}

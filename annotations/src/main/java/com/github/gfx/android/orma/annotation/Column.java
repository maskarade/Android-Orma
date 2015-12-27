/*
 * Copyright (c) 2015 FUJI Goro (gfx).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.gfx.android.orma.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface Column {

    enum Collate {
        BINARY,
        NOCASE,
        RTRIM
    }

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
     * The {@code DEFAULT} expression for the column. Currently it is only used in migration.
     *
     * @return An SQLite expression. For example. {@code "''"} for an empty string, and {@code "0"} for a literal zero,
     */
    String defaultExpr() default "";

    /**
     * Specifies how the column is compared.
     *
     * @return One of {@code Column.Collate.BINARY}, {@code Column.Collate.NOCASE} or {@code Column.Collate.RTRIM}
     */
    Collate collate() default Collate.BINARY;
}

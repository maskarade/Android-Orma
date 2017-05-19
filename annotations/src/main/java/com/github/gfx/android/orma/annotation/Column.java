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

import android.support.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.CLASS)
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
     * Specifies {@code UNIQUE} constraint. Shortcut of {@code uniqueOnConflict = OnConflict.ABORT}.
     *
     * @return True if the column value is unique in the table.
     */
    boolean unique() default false;

    /**
     * Specifies {@code UNIQUE} constraint with an {@code ON CONFLICT} clause.
     *
     * @return One of {@code OnConflict.ABORT}, {@code FAIL}, {@code IGNORE},
     * {@code REPLACE}, or {@code ROLLBACK}
     */
    @OnConflict int uniqueOnConflict() default OnConflict.NONE;

    ForeignKeyAction onDelete() default ForeignKeyAction.CASCADE;

    ForeignKeyAction onUpdate() default ForeignKeyAction.CASCADE;

    /**
     * The {@code DEFAULT} expression for the column. Currently it is only used in migration.
     *
     * @return An SQLite expression. For example. {@code "''"} for an empty string, and {@code "0"} for a literal zero,
     */
    String defaultExpr() default "";

    /**
     * Specifies how the column is compared.
     *
     * @return One of {@code Column.Collate.BINARY} (the default), {@code NOCASE} or {@code RTRIM}
     */
    Collate collate() default Collate.BINARY;

    /**
     * Specifies a storage type for the column to suppress automatic migration for existing tables.
     * <strong>Not recommended</strong>.  Will become deprecated in a future.
     *
     * @return An SQLite data type. e.g. {@code "TEXT"}, {@code "FLOAT"}, {@code "INTEGER"}, or {@code "BLOB"}
     */
    String storageType() default "";

    /**
     * @return Flags that control which helpers to generate
     */
    @Helpers long helpers() default Helpers.AUTO;

    /**
     * SQLite's {@code COLLATE} algorithms.
     *
     * @see <a href="https://www.sqlite.org/datatype3.html#collation">https://www.sqlite.org/datatype3.html#collation</a>
     */
    enum Collate {
        /**
         * <blockquote cite="https://www.sqlite.org/datatype3.html#collation">Compares string data using memcmp(), regardless
         * of text encoding.</blockquote>
         */
        BINARY,
        /**
         * <blockquote cite="https://www.sqlite.org/datatype3.html#collation">The same as binary, except the 26 upper
         * case characters of ASCII are folded to their lower case equivalents before the comparison is performed. Note that
         * only ASCII characters are case folded. SQLite does not attempt to do full UTF case folding due to the size of the
         * tables required.</blockquote>
         */
        NOCASE,
        /**
         * <blockquote cite="https://www.sqlite.org/datatype3.html#collation">The same as binary, except that trailing space
         * characters are ignored.</blockquote>
         */
        RTRIM
    }

    /**
     * SQLite's <code>ON DELETE</code> and <code>ON UPDATE</code> actions.
     *
     * @see <a href="https://www.sqlite.org/foreignkeys.html">SQLite Foreign Key Support</a>
     */
    enum ForeignKeyAction {
        NO_ACTION,
        RESTRICT,
        SET_NULL,
        SET_DEFAULT,
        CASCADE,
    }

    @Target({ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.CLASS)
    @IntDef(flag = true, value = {
            Helpers.NONE,
            Helpers.AUTO,
            Helpers.CONDITION_EQ,
            Helpers.CONDITION_NOT_EQ,
            Helpers.CONDITION_IS_NULL,
            Helpers.CONDITION_IS_NOT_NULL,
            Helpers.CONDITION_IN,
            Helpers.CONDITION_NOT_IN,
            Helpers.CONDITION_LT,
            Helpers.CONDITION_LE,
            Helpers.CONDITION_GT,
            Helpers.CONDITION_GE,
            Helpers.CONDITION_BETWEEN,
            Helpers.ORDER_IN_ASC,
            Helpers.ORDER_IN_DESC,
            Helpers.PLUCK,
            Helpers.MIN,
            Helpers.MAX,
            Helpers.SUM,
            Helpers.AVG,
    })
    @interface Helpers {

        long NONE = 0;
        long AUTO = 1;

        long CONDITION_EQ = AUTO << 1;
        long CONDITION_NOT_EQ = CONDITION_EQ << 1;
        long CONDITION_IS_NULL = CONDITION_NOT_EQ << 1;
        long CONDITION_IS_NOT_NULL = CONDITION_IS_NULL << 1;
        long CONDITION_IN = CONDITION_IS_NOT_NULL << 1;
        long CONDITION_NOT_IN = CONDITION_IN << 1;
        long CONDITION_GLOB = CONDITION_NOT_IN << 1;
        long CONDITION_NOT_GLOB = CONDITION_GLOB << 1;
        long CONDITION_LIKE = CONDITION_NOT_GLOB << 1;
        long CONDITION_NOT_LIKE = CONDITION_LIKE << 1;

        long CONDITION_LT = CONDITION_NOT_LIKE << 1;
        long CONDITION_LE = CONDITION_LT << 1;
        long CONDITION_GT = CONDITION_LE << 1;
        long CONDITION_GE = CONDITION_GT << 1;
        long CONDITION_BETWEEN = CONDITION_GE << 1;

        long CONDITIONS = CONDITION_EQ | CONDITION_NOT_EQ | CONDITION_IS_NULL | CONDITION_IS_NOT_NULL
                | CONDITION_IN | CONDITION_NOT_IN
                | CONDITION_GLOB | CONDITION_NOT_GLOB | CONDITION_LIKE | CONDITION_NOT_LIKE
                | CONDITION_LT | CONDITION_LE | CONDITION_GT | CONDITION_GE | CONDITION_BETWEEN;

        long ORDER_IN_ASC = CONDITION_BETWEEN << 1;
        long ORDER_IN_DESC = ORDER_IN_ASC << 1;

        long ORDERS = ORDER_IN_ASC | ORDER_IN_DESC;

        long PLUCK = ORDER_IN_DESC << 1;

        long MIN = PLUCK << 1;
        long MAX = MIN << 1;
        long SUM = MAX << 1;
        long AVG = SUM << 1;

        long AGGREGATORS = MIN | MAX | SUM | AVG;

        long ALL = CONDITIONS | ORDERS | PLUCK | AGGREGATORS;
    }
}

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

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Table {

    /**
     * @return The table name in SQLite. It is case-insensitive.
     */
    String value() default "";

    /**
     * @return Table constraints added to {@code CREATE TABLE}.
     * @see <a href="https://www.sqlite.org/lang_createtable.html">CREATE TABLE</a> in the SQLite reference.
     */
    String[] constraints() default {};

    String schemaClassName() default "";

    String relationClassName() default "";

    String updaterClassName() default "";

    String deleterClassName() default "";

    String selectorClassName() default "";
}

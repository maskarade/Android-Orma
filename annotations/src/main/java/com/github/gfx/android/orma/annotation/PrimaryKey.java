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

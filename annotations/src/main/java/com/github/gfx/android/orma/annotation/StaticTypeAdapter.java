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

/**
 * {@link StaticTypeAdapter} defines how a type is stored in a database column.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface StaticTypeAdapter {

    /**
     * @return The name of a static method used to serialize {@link #targetType()} to {@link #serializedType()}
     */
    String serializer() default "serialize";

    /**
     * @return the name of a static method used to deserialize {@link #targetType()} from {@link #serializedType()}
     */
    String deserializer() default "deserialize";

    /**
     * @return A target type to serialize.
     */
    Class<?> targetType();

    /**
     * @return What {@link #targetType()}} is serialized to. Must be integers, floating point numbers, {@link String} and
     * {@link byte[]}
     */
    Class<?> serializedType();
}

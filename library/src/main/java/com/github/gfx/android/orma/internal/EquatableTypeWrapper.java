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

package com.github.gfx.android.orma.internal;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

// Hack to make Android 4.x's type to be equatable
public class EquatableTypeWrapper implements Type {

    static final boolean ON_JVM = !System.getProperty("java.version").equals("0"); // robolectric

    static final boolean USE_TYPE_WRAPPER;

    static {
        Type a = new TypeHolder<List<String>>() {
        }.getType();
        Type b = new TypeHolder<List<String>>() {
        }.getType();

        // ParameterizedType implementation differs in runtimes:
        // sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl (Oracle JDK 8)
        // org.apache.harmony.luni.lang.reflect.ImplForType (Android 4.2.2)
        // libcore.reflect.ParameterizedTypeImpl (Android 5.0.2)

        USE_TYPE_WRAPPER = ON_JVM || !a.equals(b);
    }

    public static Type wrap(Type type) {
        if (USE_TYPE_WRAPPER && type instanceof ParameterizedType) {
            return new EquatableTypeWrapper(type);
        } else {
            return type;
        }
    }

    final Type type;

    final String stringRepresentation;

    public EquatableTypeWrapper(Type type) {
        this.type = type;
        this.stringRepresentation = type.toString();
    }

    @Override
    public int hashCode() {
        return stringRepresentation.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EquatableTypeWrapper)) {
            return false;
        }
        EquatableTypeWrapper that = (EquatableTypeWrapper) o;
        return stringRepresentation.equals(that.stringRepresentation);
    }

    @Override
    public String toString() {
        return stringRepresentation;
    }
}

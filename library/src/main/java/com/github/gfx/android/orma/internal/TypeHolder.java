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
import java.util.Arrays;
import java.util.List;

/**
 * A helper class to hold the type instance of parameterized types.
 *
 * @see <a href="https://github.com/google/gson/blob/master/gson/src/main/java/com/google/gson/reflect/TypeToken.java">google/gson/TypeToken.java</a>
 */
public abstract class TypeHolder<T> {

    public Type getType() {
        @SuppressWarnings("unchecked")
        Class<TypeHolder<T>> c = (Class<TypeHolder<T>>) getClass();
        ParameterizedType t;
        try {
            t = (ParameterizedType) c.getGenericSuperclass();
        } catch (ClassCastException e) {
            throw new RuntimeException("No type signature found. Missing -keepattributes Signature in progurad-rules.pro?", e);
        }
        return EquatableTypeWrapper.wrap(t.getActualTypeArguments()[0]);
    }

    private static class EquatableTypeWrapper implements ParameterizedType {

        static final boolean JVM_TESTING = !System.getProperty("java.vm.name").equals("Dalvik");

        static final boolean USE_TYPE_WRAPPER;

        static {
            Type a = new TypeHolder<List<String>>() {
            }.getType();
            Type b = new TypeHolder<List<String>>() {
            }.getType();

            // ParameterizedType implementation differs in runtime environments:
            // sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl (Oracle JDK 8)
            // org.apache.harmony.luni.lang.reflect.ImplForType (Android 4.2.2)
            // libcore.reflect.ParameterizedTypeImpl (Android 5.0.2)
            // Anyway, always use EquatableTypeWrapper in JVM testing.
            USE_TYPE_WRAPPER = JVM_TESTING || !a.equals(b);
        }

        final ParameterizedType type;

        public EquatableTypeWrapper(ParameterizedType type) {
            this.type = type;
        }

        public static Type wrap(Type type) {
            if (USE_TYPE_WRAPPER && type instanceof ParameterizedType) {
                return new EquatableTypeWrapper((ParameterizedType) type);
            } else {
                return type;
            }
        }

        @Override
        public int hashCode() {
            return type.toString().hashCode();
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
            return type.getRawType().equals(that.type.getRawType())
                    && Arrays.deepEquals(type.getActualTypeArguments(), that.type.getActualTypeArguments());
        }

        @Override
        public String toString() {
            return type.toString();
        }

        @Override
        public Type[] getActualTypeArguments() {
            return type.getActualTypeArguments();
        }

        @Override
        public Type getOwnerType() {
            return type.getOwnerType();
        }

        @Override
        public Type getRawType() {
            return type.getRawType();
        }
    }
}

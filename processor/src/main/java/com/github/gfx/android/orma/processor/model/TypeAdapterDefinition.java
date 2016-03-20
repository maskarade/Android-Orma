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

package com.github.gfx.android.orma.processor.model;

import com.github.gfx.android.orma.annotation.StaticTypeAdapter;
import com.github.gfx.android.orma.processor.util.Mirrors;
import com.github.gfx.android.orma.processor.util.Types;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Currency;
import java.util.UUID;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

public class TypeAdapterDefinition {

    public static TypeAdapterDefinition[] BUILTINS = {
            make(BigDecimal.class, String.class),
            make(BigInteger.class, String.class),
            make(ByteBuffer.class, byte[].class),
            make(Currency.class, String.class),
            make(java.util.Date.class, long.class),
            make(java.sql.Date.class, String.class, "SqlDate"),
            make(java.sql.Time.class, String.class, "SqlTime"),
            make(java.sql.Timestamp.class, String.class, "SqlTimestamp"),
            make(Types.getList(Types.String), String.class, "StringList"),
            make(Types.getSet(Types.String), String.class, "StringSet"),
            make(Types.getArrayList(Types.String), String.class, "StringArrayList"),
            make(Types.getHashSet(Types.String), String.class, "StringHashSet"),
            make(ClassName.get("android.net", "Uri"), String.class),
            make(UUID.class, String.class),
    };

    @Nullable
    public final TypeElement element;

    public final ClassName typeAdapterImpl;

    public final TypeName targetType;

    public final TypeName serializedType;

    public final String serializer;

    public final String deserializer;

    public TypeAdapterDefinition(Element element) {
        this.element = (TypeElement) element;
        this.typeAdapterImpl = ClassName.get(this.element);

        StaticTypeAdapter annotation = element.getAnnotation(StaticTypeAdapter.class);
        AnnotationMirror annotationMirror = Mirrors.findAnnotationMirror(element, StaticTypeAdapter.class).get();

        // Can't access program class instances in annotation processing in , throwing MirroredTypeException
        targetType = Mirrors.findAnnotationValueAsType(annotationMirror, "targetType").get();
        serializedType = Mirrors.findAnnotationValueAsType(annotationMirror, "serializedType").get();
        serializer = annotation.serializer();
        deserializer = annotation.deserializer();
    }

    public TypeAdapterDefinition(ClassName typeAdapter, TypeName targetType, TypeName serializedType,
            String serializer, String deserializer) {
        this.element = null;
        this.typeAdapterImpl = typeAdapter;
        this.targetType = targetType;
        this.serializedType = serializedType;
        this.serializer = serializer;
        this.deserializer = deserializer;
    }

    public static TypeAdapterDefinition make(Class<?> targetType, Class<?> serializedType) {
        return make(TypeName.get(targetType), TypeName.get(serializedType), targetType.getSimpleName());
    }

    public static TypeAdapterDefinition make(ClassName targetType, Class<?> serializedType) {
        return make(targetType, TypeName.get(serializedType), targetType.simpleName());
    }

    public static TypeAdapterDefinition make(Class<?> targetType, Class<?> serializedType, String typeId) {
        return make(TypeName.get(targetType), serializedType, typeId);
    }

    public static TypeAdapterDefinition make(TypeName targetType, Class<?> serializedType, String typeId) {
        return make(targetType, TypeName.get(serializedType), typeId);
    }

    public static TypeAdapterDefinition make(TypeName targetType, TypeName serializedType, String typeId) {
        return new TypeAdapterDefinition(Types.BuiltInSerializers, targetType, serializedType,
                "serialize" + typeId, "deserialize" + typeId);
    }

    public String getSerializerName() {
        return serializer;
    }

    public String getDeserializerName() {
        return deserializer;
    }

    @Override
    public String toString() {
        return "TypeAdapterDefinition{" +
                "element=" + element +
                ", typeAdapterImpl=" + typeAdapterImpl +
                ", targetType=" + targetType +
                ", serializedType=" + serializedType +
                ", serializer='" + serializer + '\'' +
                ", deserializer='" + deserializer + '\'' +
                '}';
    }
}

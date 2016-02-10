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

package com.github.gfx.android.orma.processor;

import com.github.gfx.android.orma.annotation.StaticTypeAdapter;
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
            TypeAdapterDefinition.make(BigDecimal.class, String.class),
            TypeAdapterDefinition.make(BigInteger.class, String.class),
            TypeAdapterDefinition.make(ByteBuffer.class, byte[].class),
            TypeAdapterDefinition.make(Currency.class, String.class),
            TypeAdapterDefinition.make(java.util.Date.class, long.class),
            TypeAdapterDefinition.make(java.sql.Date.class, String.class, "SqlDate"),
            TypeAdapterDefinition.make(java.sql.Time.class, String.class, "SqlTime"),
            TypeAdapterDefinition.make(java.sql.Timestamp.class, String.class, "SqlTimestamp"),
            TypeAdapterDefinition.make(Types.getList(Types.String), String.class, "StringList"),
            TypeAdapterDefinition.make(Types.getSet(Types.String), String.class, "StringSet"),
            TypeAdapterDefinition.make(Types.getArrayList(Types.String), String.class, "StringArrayList"),
            TypeAdapterDefinition.make(Types.getHashSet(Types.String), String.class, "StringHashSet"),
            TypeAdapterDefinition.make(ClassName.get("android.net", "Uri"), String.class),
            TypeAdapterDefinition.make(UUID.class, String.class),
    };

    @Nullable
    public final TypeElement element;

    public final ClassName typeAdapterImpl;

    public final TypeName targetType;

    public final TypeName serializedType;

    public final String serializer;

    public final String deserializer;

    public static TypeAdapterDefinition make(Class<?> targetType, Class<?> serializedType) {
        return TypeAdapterDefinition.make(TypeName.get(targetType), TypeName.get(serializedType), targetType.getSimpleName());
    }

    public static TypeAdapterDefinition make(ClassName targetType, Class<?> serializedType) {
        return TypeAdapterDefinition.make(targetType, TypeName.get(serializedType), targetType.simpleName());
    }

    public static TypeAdapterDefinition make(Class<?> targetType, Class<?> serializedType, String typeId) {
        return TypeAdapterDefinition.make(TypeName.get(targetType), serializedType, typeId);
    }

    public static TypeAdapterDefinition make(TypeName targetType, Class<?> serializedType, String typeId) {
        return TypeAdapterDefinition.make(targetType, TypeName.get(serializedType), typeId);
    }

    public static TypeAdapterDefinition make(TypeName targetType, TypeName serializedType, String typeId) {
        return new TypeAdapterDefinition(Types.BuiltInSerializers, targetType, serializedType,
                "serialize" + typeId, "deserialize" + typeId);
    }

    public TypeAdapterDefinition(Element element) {
        this.element = (TypeElement) element;
        this.typeAdapterImpl = ClassName.get(this.element);

        StaticTypeAdapter annotation = element.getAnnotation(StaticTypeAdapter.class);
        AnnotationMirror annotationMirror = Mirrors.findAnnotationMirror(element, StaticTypeAdapter.class);

        // Can't access program class instances in annotation processing in , throwing MirroredTypeException
        targetType =  TypeName.get(Mirrors.findAnnotationValueAsTypeMirror(annotationMirror, "targetType"));
        serializedType = TypeName.get(Mirrors.findAnnotationValueAsTypeMirror(annotationMirror, "serializedType"));
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

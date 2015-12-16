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

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;

public class ColumnDefinition {

    public final Element element;

    public final String name;

    public final String columnName;

    public final TypeName type;

    public final boolean nullable;

    public final boolean primaryKey;

    public final boolean autoincrement;

    public final boolean autoId;

    public final boolean indexed;

    public final boolean unique;

    public final String defaultExpr;

    public final String collate;

    public Element getter;

    public Element setter;

    public ColumnDefinition(Element element) {
        this.element = element;

        // TODO: autoincrement, conflict clause, default value, etc...
        // See https://www.sqlite.org/lang_createtable.html for full specification
        Column column = element.getAnnotation(Column.class);
        PrimaryKey primaryKeyAnnotation = element.getAnnotation(PrimaryKey.class);

        name = element.getSimpleName().toString();
        columnName = getColumnName(column, element);

        type = ClassName.get(element.asType());

        if (column != null) {
            indexed = column.indexed();
            unique = column.unique();
            collate = column.collate();
            defaultExpr = column.defaultExpr();
        } else {
            indexed = false;
            unique = false;
            defaultExpr = null;
            collate = null;
        }

        if (primaryKeyAnnotation != null) {
            primaryKey = true;
            autoincrement = primaryKeyAnnotation.autoincrement();
            autoId = primaryKeyAnnotation.auto() && Types.looksLikeIntegerType(type);
        } else {
            primaryKey = false;
            autoincrement = false;
            autoId = false;
        }

        nullable = hasNullableAnnotation(element);
    }

    public static String getColumnName(Element element) {
        Column column = element.getAnnotation(Column.class);
        return getColumnName(column, element);
    }

    static String getColumnName(Column column, Element element) {
        if (column != null && !column.value().equals("")) {
            return column.value();
        } else {
            for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
                Name annotationName = annotation.getAnnotationType().asElement().getSimpleName();
                if (annotationName.contentEquals("SerializedName") // GSON
                        || annotationName.contentEquals("JsonProperty") // Jackson
                        ) {
                    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotation
                            .getElementValues().entrySet()) {
                        if (entry.getKey().getSimpleName().contentEquals("value")) {
                            return entry.getValue().getValue().toString();
                        }
                    }
                }
            }
        }
        return element.getSimpleName().toString();
    }

    static boolean hasNullableAnnotation(Element element) {
        for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
            // allow anything named "Nullable"
            if (annotation.getAnnotationType().asElement().getSimpleName().contentEquals("Nullable")) {
                return true;
            }
        }
        return false;
    }

    public AssociationDefinition getRelation() {
        if (AssociationDefinition.isSingleAssociation(type)) {
            return AssociationDefinition.create(type);
        }
        return null;

    }

    public TypeName getType() {
        return type;
    }

    public TypeName getBoxType() {
        return type.box();
    }

    public TypeName getUnboxType() {
        if (type.equals(TypeName.VOID.box())) {
            return TypeName.VOID;
        } else if (type.equals(TypeName.BOOLEAN.box())) {
            return TypeName.BOOLEAN;
        } else if (type.equals(TypeName.BYTE.box())) {
            return TypeName.BYTE;
        } else if (type.equals(TypeName.SHORT.box())) {
            return TypeName.SHORT;
        } else if (type.equals(TypeName.INT.box())) {
            return TypeName.INT;
        } else if (type.equals(TypeName.LONG.box())) {
            return TypeName.LONG;
        } else if (type.equals(TypeName.CHAR.box())) {
            return TypeName.CHAR;
        } else if (type.equals(TypeName.FLOAT.box())) {
            return TypeName.FLOAT;
        } else if (type.equals(TypeName.DOUBLE.box())) {
            return TypeName.DOUBLE;
        }

        return type;
    }

    public TypeName getRawType() {
        if (type instanceof ParameterizedTypeName) {
            return ((ParameterizedTypeName) type).rawType;
        } else {
            return type;
        }
    }

    /**
     * @return A representation of {@code ColumnDef<T>}
     */
    public ParameterizedTypeName getColumnDefType() {
        return Types.getColumnDef(getBoxType());
    }

    public CodeBlock buildSetColumnExpr(CodeBlock rhsExpr) {
        if (setter != null) {
            return CodeBlock.builder()
                    .add("$L($L)", setter.getSimpleName(), rhsExpr)
                    .build();
        } else {
            return CodeBlock.builder()
                    .add("$L = $L", name, rhsExpr)
                    .build();
        }
    }

    public String buildGetColumnExpr() {
        if (getter != null) {
            return getter.getSimpleName() + "()";
        } else {
            return name;
        }
    }
}

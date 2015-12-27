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
import com.github.gfx.android.orma.annotation.OnConflict;
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
import javax.lang.model.element.VariableElement;

public class ColumnDefinition {

    public final SchemaDefinition schema;

    public final VariableElement element;

    public final String name;

    public final String columnName;

    public final TypeName type;

    public final boolean nullable;

    public final boolean primaryKey;

    public final int primaryKeyOnConflict;

    public final boolean autoincrement;

    public final boolean autoId;

    public final boolean indexed;

    public final boolean unique;

    public final int uniqueOnConflict;

    public final String defaultExpr;

    public final Column.Collate collate;

    public Element getter;

    public Element setter;

    public ColumnDefinition(SchemaDefinition schema, VariableElement element) {
        this.schema = schema;
        this.element = element;

        // See https://www.sqlite.org/lang_createtable.html for full specification
        Column column = element.getAnnotation(Column.class);
        PrimaryKey primaryKeyAnnotation = element.getAnnotation(PrimaryKey.class);

        name = element.getSimpleName().toString();
        columnName = getColumnName(column, element);

        type = ClassName.get(element.asType());

        if (column != null) {
            indexed = column.indexed();
            uniqueOnConflict = column.uniqueOnConflict();
            unique = uniqueOnConflict != OnConflict.NONE || column.unique();
            collate = column.collate();
            defaultExpr = column.defaultExpr();
        } else {
            indexed = false;
            uniqueOnConflict = OnConflict.NONE;
            unique = false;
            defaultExpr = null;
            collate = Column.Collate.BINARY;
        }

        if (primaryKeyAnnotation != null) {
            primaryKeyOnConflict = primaryKeyAnnotation.onConflict();
            primaryKey = true;
            autoincrement = primaryKeyAnnotation.autoincrement();
            autoId = primaryKeyAnnotation.auto() && Types.looksLikeIntegerType(type);
        } else {
            primaryKeyOnConflict = OnConflict.NONE;
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
        return Types.asUnboxType(type);
    }

    public TypeName getRawType() {
        return Types.asRawType(type);
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

    public CodeBlock buildGetTypeAdapter(String connectionExpr) {
        return CodeBlock.builder()
                .add("$L.<$T>getTypeAdapter($T.$L.type)",
                        connectionExpr,
                        type,
                        schema.getSchemaClassName(),
                        name)
                .build();
    }

    public CodeBlock buildSerializeExpr(String connectionExpr, String paramnExpr) {
        CodeBlock.Builder expr = CodeBlock.builder();
        if (needsTypeAdapter()) {
            expr.add("$L.$L($L)",
                    buildGetTypeAdapter(connectionExpr),
                    nullable ? "serializeNullable" : "serialize",
                    paramnExpr);
        } else {
            expr.add(paramnExpr);
        }
        return expr.build();
    }

    public boolean needsTypeAdapter() {
        return Types.needsTypeAdapter(getUnboxType());
    }
}

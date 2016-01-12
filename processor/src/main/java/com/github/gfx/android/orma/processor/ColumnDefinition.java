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

    public static final String kDefaultPrimaryKeyName = "_rowid_";

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

    public final String storageType;

    public ExecutableElement getter;

    public ExecutableElement setter;

    public ColumnDefinition(SchemaDefinition schema, VariableElement element) {
        this.schema = schema;
        this.element = element;

        // See https://www.sqlite.org/lang_createtable.html for full specification
        Column column = element.getAnnotation(Column.class);
        PrimaryKey primaryKeyAnnotation = element.getAnnotation(PrimaryKey.class);

        name = element.getSimpleName().toString();
        columnName = columnName(column, element);

        type = ClassName.get(element.asType());
        storageType = storageType(column, type);

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

    // to create primary key columns
    private ColumnDefinition(SchemaDefinition schema) {
        this.schema = schema;
        this.element = null;
        this.name = kDefaultPrimaryKeyName;
        this.columnName = kDefaultPrimaryKeyName;
        this.type = TypeName.LONG;
        this.nullable = false;
        this.primaryKey = true;
        this.primaryKeyOnConflict = OnConflict.NONE;
        this.autoincrement = false;
        this.autoId = true;
        this.indexed = false;
        this.unique = false;
        this.uniqueOnConflict = OnConflict.NONE;
        this.defaultExpr = "";
        this.collate = Column.Collate.BINARY;
        this.storageType = storageType(null, type);
    }

    public static ColumnDefinition createDefaultPrimaryKey(SchemaDefinition schema) {
        return new ColumnDefinition(schema);
    }

    public static String getColumnName(Element element) {
        Column column = element.getAnnotation(Column.class);
        return columnName(column, element);
    }

    static String columnName(Column column, Element element) {
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

    static String storageType(Column column, TypeName type) {
        if (column != null && !Strings.isEmpty(column.storageType())) {
            return column.storageType();
        } else {
            return SqlTypes.getSqliteType(Types.asRawType(type));
        }
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

    public void initGetterAndSetter(ExecutableElement getter, ExecutableElement setter) {
        if (getter != null) {
            this.getter = getter;
        }
        if (setter != null) {
            this.setter = setter;
        }
    }

    public AssociationDefinition getAssociation() {
        if (Types.isSingleAssociation(type)) {
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

    public String getStorageType() {
        return storageType;
    }

    /**
     * @return A representation of {@code ColumnDef<T>}
     */
    public ParameterizedTypeName getColumnDefType() {
        return Types.getColumnDef(schema.getModelClassName(), getBoxType());
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

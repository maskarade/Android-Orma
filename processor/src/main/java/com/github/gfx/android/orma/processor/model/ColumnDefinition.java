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

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.OnConflict;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.processor.ProcessingContext;
import com.github.gfx.android.orma.processor.exception.ProcessingException;
import com.github.gfx.android.orma.processor.util.Annotations;
import com.github.gfx.android.orma.processor.util.SqlTypes;
import com.github.gfx.android.orma.processor.util.Strings;
import com.github.gfx.android.orma.processor.util.Types;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import android.support.annotation.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;


public class ColumnDefinition {

    public static final String kDefaultPrimaryKeyName = "_rowid_";

    public final ProcessingContext context;

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

    public final Column.ForeignKeyAction onDeleteAction;

    public final Column.ForeignKeyAction onUpdateAction;

    public final long helperFlags;

    public final TypeAdapterDefinition typeAdapter;

    private String storageType;

    public ExecutableElement getter;

    public ExecutableElement setter;

    public ColumnDefinition(SchemaDefinition schema, VariableElement element) {
        this.schema = schema;
        this.element = element;
        context = schema.context;

        // See https://www.sqlite.org/lang_createtable.html for full specification
        Column column = element.getAnnotation(Column.class);
        PrimaryKey primaryKeyAnnotation = element.getAnnotation(PrimaryKey.class);

        name = element.getSimpleName().toString();
        columnName = columnName(column, element);

        type = ClassName.get(element.asType());
        typeAdapter = schema.context.typeAdapterMap.get(type);

        storageType = (column != null && !Strings.isEmpty(column.storageType())) ? column.storageType() : null;

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

        if (column != null) {
            indexed = column.indexed();
            uniqueOnConflict = column.uniqueOnConflict();
            unique = uniqueOnConflict != OnConflict.NONE || column.unique();
            collate = column.collate();
            onDeleteAction = column.onDelete();
            onUpdateAction = column.onUpdate();
            defaultExpr = column.defaultExpr();
            helperFlags = normalizeHelperFlags(primaryKey, indexed, autoincrement, autoId, column.helpers());
        } else {
            indexed = false;
            uniqueOnConflict = OnConflict.NONE;
            unique = false;
            defaultExpr = null;
            collate = Column.Collate.BINARY;
            onDeleteAction = Column.ForeignKeyAction.NO_ACTION;
            onUpdateAction = Column.ForeignKeyAction.NO_ACTION;
            helperFlags = normalizeHelperFlags(primaryKey, indexed, autoincrement, autoId, Column.Helpers.AUTO);
        }

        nullable = hasNullableAnnotation(element);
    }

    // to create primary key columns
    private ColumnDefinition(SchemaDefinition schema) {
        this.schema = schema;
        context = schema.context;
        element = null;
        name = kDefaultPrimaryKeyName;
        columnName = kDefaultPrimaryKeyName;
        type = TypeName.LONG;
        nullable = false;
        primaryKey = true;
        primaryKeyOnConflict = OnConflict.NONE;
        autoincrement = false;
        autoId = true;
        indexed = false;
        unique = false;
        uniqueOnConflict = OnConflict.NONE;
        defaultExpr = "";
        collate = Column.Collate.BINARY;
        onDeleteAction = Column.ForeignKeyAction.NO_ACTION;
        onUpdateAction = Column.ForeignKeyAction.NO_ACTION;
        helperFlags = normalizeHelperFlags(primaryKey, indexed, autoincrement, autoId, Column.Helpers.AUTO);
        typeAdapter = schema.context.typeAdapterMap.get(type);
        storageType = null;
    }

    public static ColumnDefinition createDefaultPrimaryKey(SchemaDefinition schema) {
        return new ColumnDefinition(schema);
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

    static boolean hasNullableAnnotation(Element element) {
        for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
            // allow anything named "Nullable"
            if (annotation.getAnnotationType().asElement().getSimpleName().contentEquals("Nullable")) {
                return true;
            }
        }
        return false;
    }

    @Column.Helpers
    static long normalizeHelperFlags(boolean primaryKey, boolean indexed, boolean autoincrement, boolean autoId, long flags) {
        if (flags == Column.Helpers.AUTO) {
            if (primaryKey) {
                return (autoincrement || !autoId)
                        ? Column.Helpers.CONDITIONS | Column.Helpers.ORDERS
                        : Column.Helpers.CONDITIONS;
            } else if (indexed) {
                return Column.Helpers.CONDITIONS | Column.Helpers.ORDERS | Column.Helpers.AGGREGATORS;
            } else {
                return Column.Helpers.AGGREGATORS;
            }
        } else {
            return flags;
        }
    }

    private static String extractStorageType(ProcessingContext context, TypeName type, Element element,
            TypeAdapterDefinition typeAdapter) {
        if (typeAdapter != null) {
            return SqlTypes.getSqliteType(typeAdapter.serializedType);
        } else if (Types.isSingleAssociation(type)) {
            return SqlTypes.getSqliteType(TypeName.LONG);
        } else if (Types.isDirectAssociation(context, type)) {
            return context.getSchemaDef(type).getPrimaryKey()
                    .map(primaryKey -> SqlTypes.getSqliteType(primaryKey.getType()))
                    .orElseGet(() -> {
                        context.addError("Missing @PrimaryKey as foreign key", element);
                        return "UNKNOWN";
                    });
        } else {
            return SqlTypes.getSqliteType(type);
        }
    }

    public void initGetterAndSetter(ExecutableElement getter, ExecutableElement setter) {
        if (getter != null) {
            this.getter = getter;
        }
        if (setter != null) {
            this.setter = setter;
        }
    }

    public CharSequence getEscapedColumnName() {
        return context.sqlg.escapeIdentifier(columnName);
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

    public TypeName getSerializedType() {
        if (isDirectAssociation() || isSingleAssociation()) {
            return getAssociatedSchema().getPrimaryKey()
                    .map(ColumnDefinition::getSerializedType)
                    .orElseGet(() -> Types.ByteArray); // dummy
        } else if (typeAdapter != null) {
            return Types.asUnboxType(typeAdapter.serializedType);
        } else {
            return getUnboxType();
        }
    }

    public TypeName getSerializedBoxType() {
        return getSerializedType().box();
    }

    public String getStorageType() {
        if (storageType == null) {
            storageType = extractStorageType(context, type, element, typeAdapter);
        }
        return storageType;
    }

    public boolean isNullableInSQL() {
        return nullable;
    }

    public boolean isNullableInJava() {
        return !type.isPrimitive() && nullable;
    }

    /**
     * @return A representation of {@code ColumnDef<T>}
     */
    public ParameterizedTypeName getColumnDefType() {
        if (isDirectAssociation()) {
            return Types
                    .getAssociationDef(schema.getModelClassName(), getBoxType(), getAssociatedSchema().getSchemaClassName());
        } else {
            return Types.getColumnDef(schema.getModelClassName(), getBoxType());
        }
    }

    public CodeBlock buildSetColumnExpr(CodeBlock rhsExpr) {
        if (setter != null) {
            return CodeBlock.of("$L($L)", setter.getSimpleName(), rhsExpr);
        } else {
            return CodeBlock.of("$L = $L", name, rhsExpr);
        }
    }

    public CodeBlock buildGetColumnExpr(String modelExpr) {
        return buildGetColumnExpr(CodeBlock.of("$L", modelExpr));
    }

    public CodeBlock buildGetColumnExpr(CodeBlock modelExpr) {
        return CodeBlock.of("$L.$L", modelExpr, getter != null ? getter.getSimpleName() + "()" : name);
    }

    public CodeBlock buildSerializedColumnExpr(String connectionExpr, String modelExpr) {
        CodeBlock getColumnExpr = buildGetColumnExpr(modelExpr);

        if (isSingleAssociation()) {
            return CodeBlock.of("$L.getId()", getColumnExpr);
        } else if (isDirectAssociation()) {
            return getAssociatedSchema().getPrimaryKey()
                    .map(primaryKey -> primaryKey.buildGetColumnExpr(getColumnExpr))
                    .orElseGet(() -> CodeBlock.of("null /* missing @PrimaryKey */"));
        } else if (needsTypeAdapter()) {
            return buildSerializeExpr(connectionExpr, getColumnExpr);
        } else {
            return getColumnExpr;
        }
    }

    public CodeBlock buildSerializeExpr(String connectionExpr, String valueExpr) {
        return buildSerializeExpr(connectionExpr, CodeBlock.of("$L", valueExpr));
    }

    public CodeBlock buildSerializeExpr(String connectionExpr, CodeBlock valueExpr) {
        // TODO: parameter injection for static type serializers
        if (needsTypeAdapter()) {
            if (typeAdapter == null) {
                throw new ProcessingException("Missing @StaticTypeAdapter to serialize " + type, element);
            }

            return CodeBlock.of("$T.$L($L)", typeAdapter.typeAdapterImpl, typeAdapter.getSerializerName(), valueExpr);
        } else {
            return valueExpr;
        }
    }

    public CodeBlock buildDeserializeExpr(CodeBlock valueExpr) {
        if (needsTypeAdapter()) {
            if (typeAdapter == null) {
                throw new ProcessingException("Missing @StaticTypeAdapter to deserialize " + type, element);
            }

            if (!typeAdapter.generic) {
                return CodeBlock.of("$T.$L($L)",
                        typeAdapter.typeAdapterImpl, typeAdapter.getDeserializerName(), valueExpr);
            } else {
                // inject Class<T> if the deserializer takes more than one
                TypeName rawType = (type instanceof ParameterizedTypeName ? ((ParameterizedTypeName) type).rawType : type);
                return CodeBlock.of("$T.$L($L, $T.class)",
                        typeAdapter.typeAdapterImpl, typeAdapter.getDeserializerName(), valueExpr, rawType);
            }

        } else {
            return valueExpr;
        }
    }

    public boolean needsTypeAdapter() {
        return Types.needsTypeAdapter(type);
    }

    public Collection<AnnotationSpec> nullabilityAnnotations() {
        if (type.isPrimitive()) {
            return Collections.emptyList();
        }

        if (nullable) {
            return Collections.singletonList(Annotations.nullable());
        } else {
            return Collections.singletonList(Annotations.nonNull());
        }
    }

    @Nullable
    public AssociationDefinition getAssociation() {
        if (Types.isSingleAssociation(type)) {
            return AssociationDefinition.createSingleAssociation(type);
        } else if (Types.isDirectAssociation(context, type)) {
            return AssociationDefinition.createDirectAssociation(type);
        }
        return null;
    }

    public boolean isDirectAssociation() {
        return Types.isDirectAssociation(context, type);
    }

    public boolean isSingleAssociation() {
        return Types.isSingleAssociation(type);
    }

    public SchemaDefinition getAssociatedSchema() {
        AssociationDefinition r = getAssociation();
        assert r != null;
        return context.getSchemaDef(r.modelType);
    }

    public boolean hasConditionHelpers() {
        return (helperFlags & Column.Helpers.CONDITIONS) != 0;
    }

    public boolean hasOrderHelpers() {
        return (helperFlags & Column.Helpers.ORDERS) != 0;
    }

    public boolean hasAggregationHelpers() {
        return (helperFlags & Column.Helpers.AGGREGATORS) != 0;
    }

    public boolean hasHelper(@Column.Helpers long f) {
        assert f != Column.Helpers.NONE && f != Column.Helpers.AUTO;
        return (helperFlags & f) == f;
    }
}

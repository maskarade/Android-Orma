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
import com.github.gfx.android.orma.annotation.Getter;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Setter;
import com.github.gfx.android.orma.annotation.Table;
import com.github.gfx.android.orma.processor.ProcessingContext;
import com.github.gfx.android.orma.processor.util.Strings;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public class SchemaDefinition {

    final ProcessingContext context;

    final TypeElement typeElement;

    final ClassName modelClassName;

    final ClassName schemaClassName;

    final ClassName relationClassName;

    final ClassName selectorClassName;

    final ClassName updaterClassName;

    final ClassName deleterClassName;

    final String tableName;

    final String[] constraints;

    final List<ColumnDefinition> columns;

    final ColumnDefinition primaryKey;

    @Nullable
    final ExecutableElement constructorElement; // null if it has a default constructor

    String createTableStatement = null;

    List<String> createIndexStatements = null;

    public SchemaDefinition(ProcessingContext context, TypeElement typeElement) {
        this.context = context;
        this.typeElement = typeElement;
        this.modelClassName = ClassName.get(typeElement);

        Table table = typeElement.getAnnotation(Table.class);
        this.constraints = table.constraints();
        this.schemaClassName = helperClassName(table.schemaClassName(), modelClassName, "_Schema");
        this.relationClassName = helperClassName(table.relationClassName(), modelClassName, "_Relation");
        this.selectorClassName = helperClassName(table.selectorClassName(), modelClassName, "_Selector");
        this.updaterClassName = helperClassName(table.updaterClassName(), modelClassName, "_Updater");
        this.deleterClassName = helperClassName(table.deleterClassName(), modelClassName, "_Deleter");
        this.tableName = firstNonEmptyName(table.value(), modelClassName.simpleName());

        this.constructorElement = findSetterConstructor(context, typeElement);

        columns = collectColumns(typeElement);
        // Places primaryKey as the last in columns to handle withoutAutoId.
        // See also the bindArgs() generator in SchemaWriter.
        columns.sort((a, b) -> {
            if (a.primaryKey) {
                return 1;
            } else if (b.primaryKey) {
                return -1;
            }
            return 0;
        });

        this.primaryKey = findPrimaryKey(columns);

        SchemaValidator.validate(context, this);
    }

    /**
     * @param typeElement the target class element
     * @return null if it has the default constructor
     */
    @Nullable
    static ExecutableElement findSetterConstructor(ProcessingContext context, TypeElement typeElement) {
        List<ExecutableElement> constructors = collectConstructors(typeElement);

        List<ExecutableElement> setterConstructors = collectSetterConstructors(constructors);

        if (setterConstructors.isEmpty()) {
            // use the default constructor
            return null;
        } else if (setterConstructors.size() != 1) {
            context.addError("Too many @Setter constructors", typeElement);
            return null;
        } else {
            return setterConstructors.get(0);
        }
    }

    static List<ExecutableElement> collectConstructors(TypeElement typeElement) {
        return typeElement.getEnclosedElements()
                .stream()
                .filter(SchemaDefinition::isConstructor)
                .map(element -> (ExecutableElement) element)
                .collect(Collectors.toList());
    }

    static List<ExecutableElement> collectSetterConstructors(List<ExecutableElement> constructors) {
        return constructors.stream()
                .filter(constructor -> constructor.getAnnotation(Setter.class) != null || constructor.getParameters()
                        .stream()
                        .anyMatch(param -> param.getAnnotation(Setter.class) != null))
                .collect(Collectors.toList());
    }

    static boolean isConstructor(Element element) {
        if (element instanceof ExecutableElement) {
            ExecutableElement method = (ExecutableElement) element;
            return method.getSimpleName().contentEquals("<init>");
        } else {
            return false;
        }
    }

    private static ClassName helperClassName(String specifiedName, ClassName modelClassName, String helperSuffix) {
        String simpleName = firstNonEmptyName(specifiedName, modelClassName.simpleName() + helperSuffix);
        return ClassName.get(modelClassName.packageName(), simpleName);
    }

    static String firstNonEmptyName(String... names) {
        for (String name : names) {
            if (!Strings.isEmpty(name)) {
                return name;
            }
        }
        throw new AssertionError("No non-empty string found");
    }

    static ColumnDefinition findPrimaryKey(List<ColumnDefinition> columns) {
        for (ColumnDefinition c : columns) {
            if (c.primaryKey) {
                return c;
            }
        }
        return null;
    }

    public boolean hasDirectAssociations() {
        return columns.stream().anyMatch(ColumnDefinition::isDirectAssociation);
    }


    List<ColumnDefinition> collectColumns(@NonNull TypeElement typeElement) {
        List<ColumnDefinition> columns = new ArrayList<>();
        TypeMirror superclass = typeElement.getSuperclass();
        if (!superclass.toString().equals(Object.class.getCanonicalName())) {
            // superclass might represent  com.example.C<java.lang.String>
            TypeElement superclassElement = context.getTypeElement(superclass);
            columns.addAll(collectColumns(superclassElement));
        }

        Map<String, ExecutableElement> getters = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Map<String, ExecutableElement> setters = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        List<VariableElement> columnElements = new ArrayList<>();

        typeElement.getEnclosedElements()
                .forEach(element -> {
                    if (element instanceof VariableElement) {
                        if (element.getAnnotation(PrimaryKey.class) != null) {
                            columnElements.add((VariableElement) element);
                        } else if (element.getAnnotation(Column.class) != null) {
                            columnElements.add((VariableElement) element);
                        }
                        return;
                    }

                    if (!(element instanceof ExecutableElement)) {
                        return;
                    }
                    if (isConstructor(element)) {
                        return;
                    }

                    ExecutableElement methodElement = (ExecutableElement) element;

                    Getter getter = methodElement.getAnnotation(Getter.class);
                    Setter setter = methodElement.getAnnotation(Setter.class);

                    extractNameFromGetter(getters, getter, methodElement);
                    extractNameFromSetter(setters, setter, methodElement);
                });

        columns.addAll(columnElements.stream()
                .map((element) -> {
                    ColumnDefinition column = new ColumnDefinition(this, element);
                    column.initGetterAndSetter(
                            getEither(getters, column.columnName, column.name),
                            getEither(setters, column.columnName, column.name));
                    return column;
                })
                .collect(Collectors.toList()));
        return columns;
    }

    private void extractNameFromGetter(Map<String, ExecutableElement> map, Getter annotation, ExecutableElement accessor) {
        if (annotation != null && !Strings.isEmpty(annotation.value())) {
            map.put(annotation.value(), accessor);
        } else if (accessor.getParameters().isEmpty()) {
            String name = accessor.getSimpleName().toString();
            if (isBooleanType(accessor.getReturnType())) {
                if (name.startsWith("is")) {
                    map.put(name.substring("is".length()), accessor);
                }
            }
            if (name.startsWith("get")) {
                map.put(name.substring("get".length()), accessor);
            }
            map.put(name, accessor);
        }
    }

    @SafeVarargs
    private final <K, V> V getEither(Map<K, V> map, K... keys) {
        for (K key : keys) {
            V value = map.get(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private void extractNameFromSetter(Map<String, ExecutableElement> map, Setter annotation, ExecutableElement accessor) {
        if (constructorElement != null) {
            if (annotation != null) {
                context.addError("@Setter annotations are already used for the constructor", accessor);
            }
            return;
        }

        if (annotation != null && !Strings.isEmpty(annotation.value())) {
            map.put(annotation.value(), accessor);
        } else if (accessor.getParameters().size() == 1) {
            String name = accessor.getSimpleName().toString();
            if (name.startsWith("set")) {
                map.put(name.substring("set".length()), accessor);
            }
            map.put(name, accessor);

        }
    }

    private boolean isBooleanType(TypeMirror type) {
        return type.getKind() == TypeKind.BOOLEAN
                || context.isSameType(type, context.getTypeMirrorOf(Boolean.class));
    }

    public Optional<ExecutableElement> getConstructorElement() {
        return Optional.ofNullable(constructorElement);
    }

    public String[] getConstraints() {
        return constraints;
    }

    public TypeElement getElement() {
        return typeElement;
    }

    public String getPackageName() {
        return schemaClassName.packageName();
    }

    public String getTableName() {
        return tableName;
    }

    public String getEscapedTableName() {
        return context.sqlg.escapeIdentifier(tableName);
    }

    public ClassName getModelClassName() {
        return modelClassName;
    }

    public ClassName getSchemaClassName() {
        return schemaClassName;
    }

    public ClassName getRelationClassName() {
        return relationClassName;
    }

    public ClassName getSelectorClassName() {
        return selectorClassName;
    }

    public ClassName getUpdaterClassName() {
        return updaterClassName;
    }

    public ClassName getDeleterClassName() {
        return deleterClassName;
    }

    public List<ColumnDefinition> getColumns() {
        return columns;
    }

    public List<ColumnDefinition> getColumnsWithoutAutoId() {
        return columns.stream().filter(c -> !c.autoId).collect(Collectors.toList());
    }

    public Optional<ColumnDefinition> findColumnByColumnName(String name) {
        return columns.stream().filter(column -> column.columnName.contentEquals(name)).findFirst();
    }

    public Optional<ColumnDefinition> getPrimaryKey() {
        return Optional.ofNullable(primaryKey);
    }

    public String getPrimaryKeyName() {
        return primaryKey != null ? primaryKey.columnName : ColumnDefinition.kDefaultPrimaryKeyName;
    }

    private void buildStatements() {
        createTableStatement = context.sqlg.buildCreateTableStatement(context, this);
        createIndexStatements = context.sqlg.buildCreateIndexStatements(this);
    }

    public synchronized String getCreateTableStatement() {
        if (createIndexStatements == null) {
            buildStatements();
        }
        return createTableStatement;
    }

    public synchronized List<String> getCreateIndexStatements() {
        if (createIndexStatements == null) {
            buildStatements();
        }
        return createIndexStatements;
    }

    public CodeBlock createSchemaInstanceExpr() {
        return CodeBlock.of("$T.INSTANCE", schemaClassName);
    }

    public int calculateConsumingColumnSize() {
        return columns.size() + columns.stream()
                .filter(ColumnDefinition::isDirectAssociation)
                .map(column -> column.getAssociatedSchema().calculateConsumingColumnSize())
                .reduce(0, (a, b) -> a + b);
    }

    public boolean hasPrimaryIdEqHelper() {
        return primaryKey != null && primaryKey.hasHelper(Column.Helpers.CONDITION_EQ);
    }

    @Override
    public String toString() {
        return getModelClassName().simpleName();
    }
}

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
package com.github.gfx.android.orma.processor.util;

import com.github.gfx.android.orma.processor.ProcessingContext;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;
import com.squareup.javapoet.WildcardTypeName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Types {

    public static final String ormaPackageName = "com.github.gfx.android.orma";

    public static final String ormaRxPackageName = "com.github.gfx.android.orma.rx";

    // Android standard types

    public static final WildcardTypeName WildcardType = WildcardTypeName.subtypeOf(TypeName.OBJECT);

    public static final ClassName String = ClassName.get(String.class);

    public static final ArrayTypeName StringArray = ArrayTypeName.of(String);

    public static final ArrayTypeName ByteArray = ArrayTypeName.of(TypeName.BYTE);

    public static final ClassName List = ClassName.get(List.class);

    public static final ClassName Collection = ClassName.get(Collection.class);

    public static final ClassName Arrays = ClassName.get(Arrays.class);

    public static final ClassName Collections = ClassName.get(Collections.class);

    public static final ClassName Context = ClassName.get("android.content", "Context");

    public static final ClassName ContentValues = ClassName.get("android.content", "ContentValues");

    public static final ClassName Cursor = ClassName.get("android.database", "Cursor");

    public static final ClassName DatabaseStatement = ClassName.get("com.github.gfx.android.orma.core", "DatabaseStatement");

    public static final ClassName SQLiteConstraintException = ClassName
            .get("android.database.sqlite", "SQLiteConstraintException");

    public static final ClassName NonNull = ClassName.get("android.support.annotation", "NonNull");

    public static final ClassName Nullable = ClassName.get("android.support.annotation", "Nullable");

    public static final ClassName WorkerThread = ClassName.get("android.support.annotation", "WorkerThread");

    public static final ClassName CheckResult = ClassName.get("android.support.annotation", "CheckResult");

    public static final ClassName Runnable = ClassName.get(java.lang.Runnable.class);

    public static final ClassName Callable = ClassName.get(java.util.concurrent.Callable.class);

    // RxJava

    public static final ClassName Single = ClassName.get("io.reactivex", "Single");

    public static final ClassName Completable = ClassName.get("io.reactivex", "Completable");


    public static final TypeVariableName T = TypeVariableName.get("T");

    // Orma types
    public static final ClassName Schema = ClassName.get(ormaPackageName, "Schema");

    public static final TypeName WildcardSchema = getSchema(WildcardType);

    public static final ClassName ColumnDef = ClassName.get(ormaPackageName, "ColumnDef");

    public static final ClassName AssociationDef = ClassName.get(ormaPackageName, "AssociationDef");

    public static final ClassName Relation = ClassName.get(ormaPackageName, "Relation");

    public static final ClassName RxRelation = ClassName.get(ormaRxPackageName, "RxRelation");

    public static final ClassName Selector = ClassName.get(ormaPackageName, "Selector");

    public static final ClassName RxSelector = ClassName.get(ormaRxPackageName, "RxSelector");

    public static final ClassName Updater = ClassName.get(ormaPackageName, "Updater");

    public static final ClassName RxUpdater = ClassName.get(ormaRxPackageName, "RxUpdater");

    public static final ClassName Deleter = ClassName.get(ormaPackageName, "Deleter");

    public static final ClassName RxDeleter = ClassName.get(ormaRxPackageName, "RxDeleter");

    public static final ClassName AssociationCondition = ClassName.get(ormaPackageName, "AssociationCondition");

    public static final ClassName OrmaConnection = ClassName.get(ormaPackageName, "OrmaConnection");

    public static final ClassName RxOrmaConnection = ClassName.get(ormaRxPackageName, "RxOrmaConnection");

    public static final ClassName Inserter = ClassName.get(ormaPackageName, "Inserter");

    public static final ClassName RxInserter = ClassName.get(ormaRxPackageName, "RxInserter");

    public static final ClassName SingleAssociation = ClassName.get(ormaPackageName, "SingleAssociation");

    public static final ClassName RxSingleAssociation = ClassName.get(ormaRxPackageName, "RxSingleAssociation");

    public static final ClassName ModelFactory = ClassName.get(ormaPackageName, "ModelFactory");

    public static final ClassName TypeHolder = ClassName.get(ormaPackageName + ".internal", "TypeHolder");

    public static final ClassName OrmaDatabaseBuilderBase = ClassName.get(ormaPackageName, "OrmaDatabaseBuilderBase");

    public static final ClassName DatabaseHandle = ClassName.get(ormaPackageName, "DatabaseHandle");

    public static final ClassName OrmaConditionBase = ClassName.get(ormaPackageName + ".internal", "OrmaConditionBase");

    public static final ClassName OrderSpec = ClassName.get(ormaPackageName, "OrderSpec");

    public static final ClassName NoValueException = ClassName.get(ormaPackageName + ".exception", "NoValueException");

    public static final ClassName BuiltInSerializers = ClassName.get(ormaPackageName, "BuiltInSerializers");

    public static final ClassName Schemas = ClassName.get(ormaPackageName + ".internal", "Schemas");

    public static final ClassName Aliases = ClassName.get(ormaPackageName + ".internal", "Aliases");

    public static final ClassName ColumnPath = ClassName.get(ormaPackageName + ".internal", "Aliases", "ColumnPath");

    public static final ClassName Function1 = ClassName.get(ormaPackageName + ".function", "Function1");

    public static final ClassName IllegalArgumentException = ClassName.get(IllegalArgumentException.class);

    private static final Set<TypeName> integerTypes = new HashSet<>(java.util.Arrays.asList(
            TypeName.BYTE,
            TypeName.BYTE.box(),
            TypeName.SHORT,
            TypeName.SHORT.box(),
            TypeName.INT,
            TypeName.INT.box(),
            TypeName.LONG,
            TypeName.LONG.box()
    ));

    private static final Set<TypeName> floatTypes = new HashSet<>(java.util.Arrays.asList(
            TypeName.FLOAT,
            TypeName.FLOAT.box(),
            TypeName.DOUBLE,
            TypeName.DOUBLE.box()
    ));


    private static final Set<TypeName> numericTypes = Stream.concat(integerTypes.stream(), floatTypes.stream()).collect(
            Collectors.toSet());

    // helper methods

    public static ParameterizedTypeName getCollection(TypeName type) {
        return ParameterizedTypeName.get(Collection, type);
    }

    public static ParameterizedTypeName getSchema(TypeName modelType) {
        return ParameterizedTypeName.get(Schema, modelType);
    }

    public static ParameterizedTypeName getColumnDef(TypeName modelType, TypeName typeName) {
        return ParameterizedTypeName.get(ColumnDef, modelType, typeName);
    }

    public static ParameterizedTypeName getAssociationDef(ClassName modelType, TypeName typeName, ClassName schemaType) {
        return ParameterizedTypeName.get(AssociationDef, modelType, typeName, schemaType);
    }

    public static ParameterizedTypeName getColumnDefList(TypeName schemaType) {
        return ParameterizedTypeName.get(List, getColumnDef(schemaType, WildcardType));
    }

    public static ParameterizedTypeName getOrmaConditionBase(TypeName modelType) {
        return ParameterizedTypeName.get(OrmaConditionBase, modelType, WildcardType);
    }

    public static ParameterizedTypeName getRelation(TypeName modelType, TypeName concreteRelationType) {
        return ParameterizedTypeName.get(Relation, modelType, concreteRelationType);
    }

    public static ParameterizedTypeName getRxRelation(TypeName modelType, TypeName concreteRelationType) {
        return ParameterizedTypeName.get(RxRelation, modelType, concreteRelationType);
    }

    public static ParameterizedTypeName getSelector(TypeName modelType, TypeName concreteSelectorType) {
        return ParameterizedTypeName.get(Selector, modelType, concreteSelectorType);
    }

    public static ParameterizedTypeName getRxSelector(TypeName modelType, TypeName concreteSelectorType) {
        return ParameterizedTypeName.get(RxSelector, modelType, concreteSelectorType);
    }

    public static ParameterizedTypeName getUpdater(TypeName modelType, TypeName concreteUpdaterType) {
        return ParameterizedTypeName.get(Updater, modelType, concreteUpdaterType);
    }

    public static ParameterizedTypeName getRxUpdater(TypeName modelType, TypeName concreteUpdaterType) {
        return ParameterizedTypeName.get(RxUpdater, modelType, concreteUpdaterType);
    }

    public static ParameterizedTypeName getDeleter(TypeName modelType, TypeName concreteDeleterType) {
        return ParameterizedTypeName.get(Deleter, modelType, concreteDeleterType);
    }

    public static ParameterizedTypeName getRxDeleter(TypeName modelType, TypeName concreteDeleterType) {
        return ParameterizedTypeName.get(RxDeleter, modelType, concreteDeleterType);
    }

    public static ParameterizedTypeName getAssociationCondition(TypeName modelType, TypeName concreteSelectorType) {
        return ParameterizedTypeName.get(AssociationCondition, modelType, concreteSelectorType);
    }

    public static ParameterizedTypeName getSet(ClassName typeName) {
        return ParameterizedTypeName.get(ClassName.get(Set.class), typeName);
    }

    public static ParameterizedTypeName getList(TypeName typeName) {
        return ParameterizedTypeName.get(List, typeName);
    }


    public static TypeName getArrayList(ClassName typeName) {
        return ParameterizedTypeName.get(ClassName.get(ArrayList.class), typeName);
    }

    public static TypeName getHashSet(ClassName typeName) {
        return ParameterizedTypeName.get(ClassName.get(HashSet.class), typeName);
    }

    public static ParameterizedTypeName getInserter(TypeName typeName) {
        return ParameterizedTypeName.get(Inserter, typeName);
    }

    public static ParameterizedTypeName getRxInserter(TypeName typeName) {
        return ParameterizedTypeName.get(RxInserter, typeName);
    }

    public static ParameterizedTypeName getSingle(TypeName typeName) {
        return ParameterizedTypeName.get(Single, typeName);
    }

    public static TypeName getFunction1(TypeName argType, TypeName returnType) {
        return ParameterizedTypeName.get(Function1, argType, returnType);
    }

    public static ParameterizedTypeName getModelFactory(TypeName typeName) {
        return ParameterizedTypeName.get(ModelFactory, typeName);
    }

    public static ArrayTypeName getOrderSpecArray(TypeName modelType) {
        return ArrayTypeName.of(ParameterizedTypeName.get(OrderSpec, modelType));
    }

    public static boolean looksLikeIntegerType(TypeName type) {
        return integerTypes.contains(type);
    }

    public static boolean looksLikeFloatType(TypeName type) {
        return floatTypes.contains(type);
    }

    public static boolean isSingleAssociation(TypeName type) {
        if (type instanceof ParameterizedTypeName) {
            ParameterizedTypeName pt = (ParameterizedTypeName) type;
            return pt.rawType.equals(Types.SingleAssociation) || pt.rawType.equals(Types.RxSingleAssociation);
        } else {
            return false;
        }
    }

    public static boolean isDirectAssociation(ProcessingContext context, TypeName type) {
        return context.getSchemaDef(type) != null;
    }

    public static boolean needsTypeAdapter(TypeName type) {
        return type instanceof ParameterizedTypeName
                || !(type.isPrimitive() || type.isBoxedPrimitive() || type.equals(Types.String) || type
                .equals(Types.ByteArray));
    }

    public static TypeName asUnboxType(TypeName type) {
        if (type.isBoxedPrimitive()) {
            return type.unbox();
        } else {
            return type;
        }
    }

    public static boolean isNumeric(TypeName type) {
        return numericTypes.contains(type);
    }

    public static ParameterizedTypeName getLinkedList(TypeName type) {
        return ParameterizedTypeName.get(ClassName.get(LinkedList.class), type);
    }

    public static ParameterizedTypeName getLinkedHashSet(TypeName type) {
        return ParameterizedTypeName.get(ClassName.get(LinkedHashSet.class), type);
    }
}

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
package com.github.gfx.android.orma.migration.test.util;

import com.github.gfx.android.orma.migration.MigrationSchema;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SchemaData implements MigrationSchema {

    final String tableName;

    final String createTableStatement;

    final List<String> createIndexStatements;

    public SchemaData(String tableName, String createTableStatement, @NonNull String... createIndexStatements) {
        this.tableName = tableName;
        this.createTableStatement = createTableStatement;
        this.createIndexStatements = new ArrayList<>(createIndexStatements.length);
        Collections.addAll(this.createIndexStatements, createIndexStatements);
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public String getCreateTableStatement() {
        return createTableStatement;
    }

    @Override
    public List<String> getCreateIndexStatements() {
        return createIndexStatements;
    }

    public void addCreateIndexStatements(String... statements) {
        Collections.addAll(createIndexStatements, statements);
    }

    public List<String> getAllTheStatements() {
        List<String> statements = new ArrayList<>();
        statements.add(createTableStatement);
        statements.addAll(createIndexStatements);
        return statements;
    }
}

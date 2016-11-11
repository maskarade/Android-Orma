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

    @NonNull final String tableName;

    @NonNull final String createTableStatement;

    @NonNull final List<String> createIndexStatements;

    public SchemaData(@NonNull String tableName, @NonNull String createTableStatement, @NonNull String... createIndexStatements) {
        this.tableName = tableName;
        this.createTableStatement = createTableStatement;
        this.createIndexStatements = new ArrayList<>(createIndexStatements.length);
        Collections.addAll(this.createIndexStatements, createIndexStatements);
    }

    @NonNull
    @Override
    public String getTableName() {
        return tableName;
    }

    @NonNull
    @Override
    public String getCreateTableStatement() {
        return createTableStatement;
    }

    @NonNull
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SchemaData that = (SchemaData) o;

        if (!tableName.equals(that.tableName)) {
            return false;
        }
        if (!createTableStatement.equals(that.createTableStatement)) {
            return false;
        }
        return createIndexStatements.equals(that.createIndexStatements);

    }

    @Override
    public int hashCode() {
        int result = tableName.hashCode();
        result = 31 * result + createTableStatement.hashCode();
        result = 31 * result + createIndexStatements.hashCode();
        return result;
    }
}

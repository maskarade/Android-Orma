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

package com.github.gfx.android.orma.sqliteparser;

import java.util.ArrayList;
import java.util.List;

public class CreateTableStatement extends SQLiteComponent {

    String tableName;

    List<ColumnDef> columns = new ArrayList<>();

    List<Constraint> constraints = new ArrayList<>();

    SelectStatement selectStatement;

    public CreateTableStatement() {

    }

    public String getTableName() {
        return tableName;
    }

    public List<ColumnDef> getColumns() {
        return columns;
    }

    public List<Constraint> getConstraints() {
        return constraints;
    }

    public SelectStatement getSelectStatement() {
        return selectStatement;
    }

    public static class ColumnDef extends SQLiteComponent {

        String name;

        String type;

        List<Constraint> constraints = new ArrayList<>();

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public List<Constraint> getConstraints() {
            return constraints;
        }

        public static class Constraint extends SQLiteComponent {

            boolean primaryKey;

            boolean nullable = true;

            String defaultExpr;

            public boolean isPrimaryKey() {
                return primaryKey;
            }

            public boolean isNullable() {
                return !primaryKey && nullable;
            }

            public String getDefaultExpr() {
                return defaultExpr;
            }
        }
    }

    public static class Constraint extends SQLiteComponent {

        String name;

        public String getName() {
            return name;
        }
    }
}

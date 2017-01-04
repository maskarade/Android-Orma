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

package com.github.gfx.android.orma.migration.sqliteparser;

import java.util.ArrayList;
import java.util.List;

/**
 * Representation of {@code CREATE INDEX $index ON $table ($columns...)}.
 */
public class CreateIndexStatement extends SQLiteComponent {

    Name indexName;

    Name tableName;

    List<Name> columns = new ArrayList<>();

    public boolean isUnique() {
        for (CharSequence token : tokens) {
            if (token.equals("unique")) {
                return true;
            }
        }
        return false;
    }

    public Name getIndexName() {
        return indexName;
    }

    public Name getTableName() {
        return tableName;
    }

    public List<Name> getColumns() {
        return columns;
    }
}

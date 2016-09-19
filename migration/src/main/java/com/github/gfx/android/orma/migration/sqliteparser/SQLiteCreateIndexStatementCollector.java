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

import com.github.gfx.android.orma.migration.sqliteparser.g.SQLiteBaseListener;
import com.github.gfx.android.orma.migration.sqliteparser.g.SQLiteParser;

public class SQLiteCreateIndexStatementCollector extends SQLiteBaseListener {

    CreateIndexStatement createIndexStatement;

    @Override
    public void enterCreate_index_stmt(SQLiteParser.Create_index_stmtContext ctx) {
        createIndexStatement = new CreateIndexStatement();
    }

    @Override
    public void exitIndex_name(SQLiteParser.Index_nameContext ctx) {
        if (createIndexStatement.indexName == null) {
            createIndexStatement.indexName = new SQLiteComponent.Name(ctx.getText());
        }
    }

    @Override
    public void exitTable_name(SQLiteParser.Table_nameContext ctx) {
        if (createIndexStatement.tableName == null) {
            createIndexStatement.tableName = new SQLiteComponent.Name(ctx.getText());
        }
    }

    @Override
    public void exitIndexed_column(SQLiteParser.Indexed_columnContext ctx) {
        createIndexStatement.columns.add(new SQLiteComponent.Name(ctx.getText()));
    }
}

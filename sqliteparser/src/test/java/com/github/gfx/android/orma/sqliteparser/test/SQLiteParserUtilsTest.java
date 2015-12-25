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

package com.github.gfx.android.orma.sqliteparser.test;

import com.github.gfx.android.orma.sqliteparser.CreateTableStatement;
import com.github.gfx.android.orma.sqliteparser.SQLiteParserUtils;
import com.github.gfx.android.orma.sqliteparser.g.SQLiteParser;

import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class SQLiteParserUtilsTest {

    @Test
    public void testCreateParser() throws Exception {
        SQLiteParser parser = SQLiteParserUtils.createParser("CREATE TABLE foo (id INTEGER PRIMARY KEY)");
        assertThat(parser, is(not(nullValue())));
    }

    @Test
    public void testParseGood() throws Exception {
        SQLiteParser.ParseContext parseContext = SQLiteParserUtils.parse("CREATE TABLE foo (id INTEGER PRIMARY KEY)");
        assertThat(parseContext.error(), is(empty()));
    }

    @Test(expected = ParseCancellationException.class)
    public void testParseBad() throws Exception {
        SQLiteParser.ParseContext parseContext = SQLiteParserUtils.parse("CREATE TABLE");
        assertThat(parseContext.error(), is(not(empty())));
    }

    @Test
    public void testParseIntoCreateTableStatement() {
        CreateTableStatement createTableStatement = SQLiteParserUtils.parseIntoCreateTableStatement(
                "CREATE TABLE IF NOT EXISTS \"foo\" (\n"
                        + "\"id\" INTEGER PRIMARY KEY,\n"
                        + "`title` TEXT NOT NULL ON CONFLICT REPLACE,\n"
                        + "price INTEGER UNSIGNED (8) NOT NULL DEFAULT(100 * 1.08)"
                        + ")"
        );
        assertThat(createTableStatement, is(not(nullValue())));

        assertThat(createTableStatement.getTableName(), is("foo"));

        List<CreateTableStatement.ColumnDef> columns = createTableStatement.getColumns();

        assertThat(columns, hasSize(3));
        {
            CreateTableStatement.ColumnDef column = columns.get(0);
            assertThat(column.getName(), is("id"));
            assertThat(column.getType(), is("INTEGER"));

            List<CreateTableStatement.ColumnDef.Constraint> constraints = column.getConstraints();
            assertThat(constraints, hasSize(1));
            assertThat(constraints.get(0).isPrimaryKey(), is(true));
            assertThat(constraints.get(0).isNullable(), is(false));
            assertThat(constraints.get(0).getTokens(), contains("PRIMARY", "KEY"));
        }

        {
            CreateTableStatement.ColumnDef column = columns.get(1);
            assertThat(column.getName(), is("title"));
            assertThat(column.getType(), is("TEXT"));
            List<CreateTableStatement.ColumnDef.Constraint> constraints = column.getConstraints();
            assertThat(constraints, hasSize(1));
            assertThat(constraints.get(0).isPrimaryKey(), is(false));
            assertThat(constraints.get(0).isNullable(), is(false));
            assertThat(constraints.get(0).getTokens(), contains("NOT", "NULL", "ON", "CONFLICT", "REPLACE"));
        }

        {
            CreateTableStatement.ColumnDef column = columns.get(2);
            assertThat(column.getName(), is("price"));
            assertThat("ignore size of data types", column.getType(), is("INTEGER UNSIGNED"));
            List<CreateTableStatement.ColumnDef.Constraint> constraints = column.getConstraints();
            assertThat(constraints, hasSize(2));
            assertThat(constraints.get(0).isPrimaryKey(), is(false));
            assertThat(constraints.get(0).isNullable(), is(false));
            assertThat(constraints.get(0).getTokens(), contains("NOT", "NULL"));

            assertThat(constraints.get(1).getDefaultExpr(), is("( 100 * 1.08 )"));
            assertThat(constraints.get(1).getTokens(), contains("DEFAULT", "(", "100", "*", "1.08", ")"));
        }
    }
}

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

package com.github.gfx.android.orma.migration.test.sqliteparser_test;


import com.github.gfx.android.orma.migration.sqliteparser.CreateIndexStatement;
import com.github.gfx.android.orma.migration.sqliteparser.CreateTableStatement;
import com.github.gfx.android.orma.migration.sqliteparser.SQLiteComponent;
import com.github.gfx.android.orma.migration.sqliteparser.SQLiteParserUtils;
import com.github.gfx.android.orma.migration.sqliteparser.g.SQLiteParser;
import com.github.gfx.android.orma.migration.test.util.TestUtils;

import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.assumeFalse;

@RunWith(AndroidJUnit4.class)
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
    public void testSQLiteComponent() throws Exception {
        String sql = "CREATE TABLE foo (\n"
                + "id INTEGER,\n"
                + "title TEXT\n"
                + ")";

        SQLiteComponent a = SQLiteParserUtils.parseIntoSQLiteComponent(sql);
        SQLiteComponent b = SQLiteParserUtils.parseIntoSQLiteComponent(sql);

        assertThat(a, is(b));
        assertThat(a.hashCode(), is(b.hashCode()));
        assertThat(a.toString(), is(b.toString()));
    }

    @Test
    public void testParseIntoCreateTableStatement() {
        CreateTableStatement createTableStatement = SQLiteParserUtils.parseIntoCreateTableStatement(
                "CREATE TABLE IF NOT EXISTS \"foo\" (\n"
                        + "\"id\" INTEGER PRIMARY KEY,\n"
                        + "`title` TEXT NOT NULL ON CONFLICT REPLACE,\n"
                        + "price INTEGER UNSIGNED (8) NOT NULL DEFAULT 100"
                        + ")"
        );
        assertThat(createTableStatement, is(not(nullValue())));

        assertThat(createTableStatement.getTableName(), is(new SQLiteComponent.Name("foo")));

        List<CreateTableStatement.ColumnDef> columns = createTableStatement.getColumns();

        assertThat(columns, hasSize(3));
        {
            CreateTableStatement.ColumnDef column = columns.get(0);
            assertThat(column.getName(), is(new SQLiteComponent.Name("id")));
            assertThat(column.getType(), is("INTEGER"));

            List<CreateTableStatement.ColumnDef.Constraint> constraints = column.getConstraints();
            assertThat(constraints, hasSize(1));
            assertThat(constraints.get(0).isPrimaryKey(), is(true));
            assertThat(constraints.get(0).isNullable(), is(false));
            assertThat(constraints.get(0).getTokens(), is(keywordList("PRIMARY", "KEY")));
        }

        {
            CreateTableStatement.ColumnDef column = columns.get(1);
            assertThat(column.getName(), is(new SQLiteComponent.Name("title")));
            assertThat(column.getType(), is("TEXT"));
            List<CreateTableStatement.ColumnDef.Constraint> constraints = column.getConstraints();
            assertThat(constraints, hasSize(1));
            assertThat(constraints.get(0).isPrimaryKey(), is(false));
            assertThat(constraints.get(0).isNullable(), is(false));
            assertThat(constraints.get(0).getTokens(), is(keywordList("NOT", "NULL", "ON", "CONFLICT", "REPLACE")));
        }

        {
            CreateTableStatement.ColumnDef column = columns.get(2);
            assertThat(column.getName(), is(new SQLiteComponent.Name("price")));
            assertThat("ignore size of data types", column.getType(), is("INTEGER UNSIGNED"));
            List<CreateTableStatement.ColumnDef.Constraint> constraints = column.getConstraints();
            assertThat(constraints, hasSize(2));
            assertThat(constraints.get(0).isPrimaryKey(), is(false));
            assertThat(constraints.get(0).isNullable(), is(false));
            assertThat(constraints.get(0).getTokens(), is(keywordList("NOT", "NULL")));

            assertThat(constraints.get(1).getDefaultExpr(), is("100"));
            assertThat(constraints.get(1).getTokens(), contains(
                    new SQLiteComponent.Keyword("DEFAULT"), "100"));
        }
    }

    @Test
    public void testTableConstraints() throws Exception {
        CreateTableStatement createTableStatement = SQLiteParserUtils.parseIntoCreateTableStatement(
                "CREATE TABLE foo (\n"
                        + "id INTEGER,\n"
                        + "title TEXT,\n"
                        + "PRIMARY KEY (id),\n"
                        + "UNIQUE (title) ON CONFLICT REPLACE\n"
                        + ")"
        );

        assertThat(createTableStatement.getTableName(), is(new SQLiteComponent.Name("foo")));
        assertThat(createTableStatement.getColumns(), hasSize(2));

        assertThat(createTableStatement.getConstraints(), hasSize(2));
    }

    @Test
    public void testTableWithForeignKey() throws Exception {
        CreateTableStatement createTableStatement = SQLiteParserUtils.parseIntoCreateTableStatement(
                "CREATE TABLE foo (id INTEGER PRIMARY KEY, bar INTEGER NOT NULL REFERENCES baz (id))"
        );

        List<CreateTableStatement.ColumnDef> columns =  createTableStatement.getColumns();
        assertThat(columns, hasSize(2));
        assertThat(columns.get(0).getName(), is(new SQLiteComponent.Name("id")));
        assertThat(columns.get(1).getName(), is(new SQLiteComponent.Name("bar")));
    }


    @Test
    public void testComplexTable() throws Exception {
        assumeFalse(TestUtils.runOnAndroid()); // FIXME

        CreateTableStatement createTableStatement = SQLiteParserUtils.parseIntoCreateTableStatement(
                "CREATE TABLE foo (title TEXT DEFAULT (''))"
        );

        assertThat(createTableStatement.getTableName(), is(new SQLiteComponent.Name("foo")));
    }

    @Test
    public void testParseIntoCreateIndexStatement() throws Exception {
        CreateIndexStatement statement = SQLiteParserUtils.parseIntoCreateIndexStatement(
                "CREATE INDEX index_title_on_book ON \"book\" (`title`)"
        );

        assertThat(statement.getIndexName(), is(new SQLiteComponent.Name("index_title_on_book")));
        assertThat(statement.getTableName(), is(new SQLiteComponent.Name("book")));
        assertThat(statement.getColumns(), contains(new SQLiteComponent.Name("title")));
    }

    @Test
    public void testParseIntoSQLiteComponent() throws Exception {
        SQLiteComponent component = SQLiteParserUtils.parseIntoSQLiteComponent(
                "CREATE INDEX index_title_on_book ON \"book\" (`title`)"
        );

        assertThat(component.getTokens(),
                contains(new SQLiteComponent.Keyword("CREATE"),
                        new SQLiteComponent.Keyword("INDEX"),
                        new SQLiteComponent.Name("index_title_on_book"),
                        new SQLiteComponent.Keyword("ON"),
                        new SQLiteComponent.Name("book"),
                        "(",
                        new SQLiteComponent.Name("title"),
                        ")"));
    }

    public List<CharSequence> keywordList(String... keywords) {
        List<CharSequence> keywordList = new ArrayList<>();
        for (String k : keywords) {
            keywordList.add(new SQLiteComponent.Keyword(k));
        }
        return keywordList;
    }
}

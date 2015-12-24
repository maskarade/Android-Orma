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
                "CREATE TABLE foo (id INTEGER PRIMARY KEY, title TEXT NOT NULL ON CONFLICT REPLACE)"
        );
        assertThat(createTableStatement, is(not(nullValue())));

        assertThat(createTableStatement.getTableName(), is("foo"));

        List<CreateTableStatement.ColumnDef> columns = createTableStatement.getColumns();
        assertThat(columns, hasSize(2));
        assertThat(columns.get(0).getName(), is("id"));
        assertThat(columns.get(1).getName(), is("title"));
    }
}

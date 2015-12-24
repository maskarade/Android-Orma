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

import com.github.gfx.android.orma.sqliteparser.g.SQLiteBaseListener;
import com.github.gfx.android.orma.sqliteparser.g.SQLiteLexer;
import com.github.gfx.android.orma.sqliteparser.g.SQLiteParser;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;

public class SQLiteParserUtils {

    public static SQLiteParser createParser(String sql) {
        CharStream source = new ANTLRInputStream(sql);
        Lexer lexer = new SQLiteLexer(source);
        TokenStream tokenStream = new CommonTokenStream(lexer);
        SQLiteParser parser = new SQLiteParser(tokenStream);
        parser.setErrorHandler(new BailErrorStrategy());
        return parser;
    }

    public static SQLiteParser.ParseContext parse(String sql) throws ParseCancellationException {
        SQLiteParser parser = createParser(sql);
        return parser.parse();
    }

    public static CreateTableStatement parseIntoCreateTableStatement(String sql) throws ParseCancellationException {
        SQLiteParser parser = createParser(sql);
        CreateTableCollector collector = new CreateTableCollector();
        parser.addParseListener(collector);
        parser.parse();

        return collector.createTableStatement;
    }

    public static class CreateTableCollector extends SQLiteBaseListener {

        CreateTableStatement.ColumnDef columnDef;

        CreateTableStatement createTableStatement;

        @Override
        public void enterCreate_table_stmt(SQLiteParser.Create_table_stmtContext ctx) {
            createTableStatement = new CreateTableStatement();
        }

        @Override
        public void exitCreate_table_stmt(SQLiteParser.Create_table_stmtContext ctx) {
            createTableStatement.tableName = ctx.table_name().getText();
        }

        @Override
        public void enterColumn_def(SQLiteParser.Column_defContext ctx) {
            columnDef = new CreateTableStatement.ColumnDef();
        }

        @Override
        public void exitColumn_def(SQLiteParser.Column_defContext ctx) {
            columnDef.name = ctx.column_name().getText();
            createTableStatement.columns.add(columnDef);
            columnDef = null;
        }
    }
}

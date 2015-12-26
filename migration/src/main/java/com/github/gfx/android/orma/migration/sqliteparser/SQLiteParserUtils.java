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

import com.github.gfx.android.orma.migration.sqliteparser.g.SQLiteLexer;
import com.github.gfx.android.orma.migration.sqliteparser.g.SQLiteParser;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;

/**
 * An entrypoint of {@link SQLiteParser}
 */
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
        SQLiteDdlCollector collector = new SQLiteDdlCollector();
        parser.addParseListener(collector);
        parser.parse();

        return collector.createTableStatement;
    }
}

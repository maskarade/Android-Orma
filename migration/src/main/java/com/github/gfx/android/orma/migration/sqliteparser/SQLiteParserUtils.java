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
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

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
        try {
            return parser.parse();
        } catch (StackOverflowError e) {
            throw new ParseCancellationException("SQL is too complex to parse: " + sql, e);
        }
    }

    public static CreateTableStatement parseIntoCreateTableStatement(String sql) throws ParseCancellationException {
        SQLiteParser parser = createParser(sql);
        SQLiteCreateTableStatementCollector collector = new SQLiteCreateTableStatementCollector();
        parser.addParseListener(collector);
        try {
            parser.parse();
        } catch (StackOverflowError e) {
            throw new ParseCancellationException("SQL is too complex to parse: " + sql, e);
        }
        return collector.createTableStatement;
    }

    public static CreateIndexStatement parseIntoCreateIndexStatement(String sql) {
        SQLiteParser parser = createParser(sql);
        SQLiteCreateIndexStatementCollector collector = new SQLiteCreateIndexStatementCollector();
        parser.addParseListener(collector);
        SQLiteParser.ParseContext parseContext;
        try {
            parseContext = parser.parse();
        } catch (StackOverflowError e) {
            throw new ParseCancellationException("SQL is too complex to parse: " + sql, e);
        }
        appendTokenList(collector.createIndexStatement, parseContext);
        return collector.createIndexStatement;
    }

    public static SQLiteComponent parseIntoSQLiteComponent(String sql) throws ParseCancellationException {
        SQLiteParser parser = createParser(sql);

        SQLiteParser.ParseContext parseContext;
        try {
            parseContext = parser.parse();
        } catch (StackOverflowError e) {
            throw new ParseCancellationException("SQL is too complex to parse: " + sql);
        }
        SQLiteComponent component = new SQLiteComponent();
        appendTokenList(component, parseContext);
        return component;
    }

    public static void appendTokenList(final SQLiteComponent component, ParseTree node) {
        node.accept(new AbstractParseTreeVisitor<Void>() {
            @Override
            public Void visitTerminal(TerminalNode node) {
                int type = node.getSymbol().getType();
                if (type == Token.EOF) {
                    return null;
                }

                if (node.getParent() instanceof SQLiteParser.Any_nameContext) {
                    component.tokens.add(new SQLiteComponent.Name(node.getText()));
                } else if (isKeyword(type)) {
                    component.tokens.add(new SQLiteComponent.Keyword(node.getText()));
                } else {
                    component.tokens.add(node.getText());
                }
                return null;
            }
        });
    }

    private static boolean isKeyword(int type) {
        String name = SQLiteLexer.VOCABULARY.getSymbolicName(type);
        return name.startsWith("K_");
    }
}

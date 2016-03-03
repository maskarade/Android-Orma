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

import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

/**
 * The SQLite DDL collector for {@link SQLiteParser}, used in {@link SQLiteParserUtils}
 */
public class SQLiteCreateTableStatementCollector extends SQLiteBaseListener {

    CreateTableStatement.ColumnDef columnDef;

    CreateTableStatement createTableStatement;

    static String combineParseTree(ParseTree node) {
        return node.accept(new AbstractParseTreeVisitor<StringBuilder>() {
            final StringBuilder sb = new StringBuilder();

            @Override
            protected StringBuilder defaultResult() {
                return sb;
            }

            @Override
            public StringBuilder visitTerminal(TerminalNode node) {
                if (sb.length() != 0) {
                    sb.append(' ');
                }
                sb.append(node.getText());
                return sb;
            }
        }).toString();
    }

    @Override
    public void enterCreate_table_stmt(SQLiteParser.Create_table_stmtContext ctx) {
        createTableStatement = new CreateTableStatement();
    }

    @Override
    public void exitCreate_table_stmt(SQLiteParser.Create_table_stmtContext ctx) {
        if (ctx.K_AS() != null) {
            createTableStatement.selectStatement = new SelectStatement();
            SQLiteParserUtils.appendTokenList(createTableStatement.selectStatement, ctx);
        }

        SQLiteParserUtils.appendTokenList(createTableStatement, ctx);
    }

    @Override
    public void exitTable_name(SQLiteParser.Table_nameContext ctx) {
        createTableStatement.tableName = new SQLiteComponent.Name(ctx.getText());
    }

    @Override
    public void enterColumn_def(SQLiteParser.Column_defContext ctx) {
        columnDef = new CreateTableStatement.ColumnDef();
        createTableStatement.columns.add(columnDef);
    }

    @Override
    public void exitColumn_def(SQLiteParser.Column_defContext ctx) {
        SQLiteParserUtils.appendTokenList(columnDef, ctx);
        columnDef = null;
    }

    @Override
    public void exitColumn_name(SQLiteParser.Column_nameContext ctx) {
        if (columnDef != null && columnDef.name == null) {
            columnDef.name = new SQLiteComponent.Name(ctx.getText());
        }
    }

    @Override
    public void exitType_name(SQLiteParser.Type_nameContext ctx) {
        StringBuilder name = new StringBuilder();
        for (SQLiteParser.NameContext nameContext : ctx.name()) {
            if (name.length() != 0) {
                name.append(' ');
            }
            name.append(nameContext.getText());
        }
        columnDef.type = name.toString();
    }

    @Override
    public void exitColumn_constraint(SQLiteParser.Column_constraintContext ctx) {
        CreateTableStatement.ColumnDef.Constraint constraint = new CreateTableStatement.ColumnDef.Constraint();

        if (ctx.K_PRIMARY() != null) {
            constraint.primaryKey = true;
        } else if (ctx.K_NOT() != null) {
            constraint.nullable = false;
        } else if (ctx.K_NULL() != null) {
            constraint.nullable = true;
        } else if (ctx.K_DEFAULT() != null) {
            List<ParseTree> nodes = ctx.children.subList(1, ctx.children.size());
            for (ParseTree node : nodes) {
                if (constraint.defaultExpr == null) {
                    constraint.defaultExpr = combineParseTree(node);
                } else {
                    constraint.defaultExpr += " " + combineParseTree(node);
                }
            }
        }

        SQLiteParserUtils.appendTokenList(constraint, ctx);

        columnDef.constraints.add(constraint);
    }

    @Override
    public void exitTable_constraint(SQLiteParser.Table_constraintContext ctx) {
        CreateTableStatement.Constraint constraint = new CreateTableStatement.Constraint();

        if (ctx.K_CONSTRAINT() != null) {
            constraint.name = new SQLiteComponent.Name(ctx.name().getText());
        }
        SQLiteParserUtils.appendTokenList(constraint, ctx);

        createTableStatement.constraints.add(constraint);
    }
}

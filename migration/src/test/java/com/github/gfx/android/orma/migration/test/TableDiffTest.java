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
package com.github.gfx.android.orma.migration.test;

import com.github.gfx.android.orma.migration.SchemaDiffMigration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
public class TableDiffTest {

    static final String SCHEMA_HASH = "aaa";

    SchemaDiffMigration migration;

    Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    @Before
    public void setUp() throws Exception {
        migration = new SchemaDiffMigration(getContext(), SCHEMA_HASH);
    }

    @Test
    public void same() throws Exception {
        String a = "CREATE TABLE todo (title TEXT, content TEXT)";
        String b = "CREATE TABLE todo (title TEXT, content TEXT)";
        List<String> statements = migration.tableDiff(a, b);
        assertThat(statements, is(empty()));
    }

    @Test
    public void sameButDifferentCases() throws Exception {
        String a = "CREATE TABLE TODO (TITLE TEXT, CONTENT TEXT)";
        String b = "create table todo (title text, content text)";
        List<String> statements = migration.tableDiff(a, b);
        assertThat(statements, is(empty()));
    }

    @Test
    public void sameButAddQuotes() throws Exception {
        String a = "CREATE TABLE todo (title TEXT, content TEXT)";
        String b = "CREATE TABLE `todo` (`title` TEXT, `content` TEXT)";
        List<String> statements = migration.tableDiff(a, b);
        assertThat(statements, is(empty()));
    }

    @Test
    public void sameButRemoveQuotes() throws Exception {
        String a = "CREATE TABLE `todo` (`title` TEXT, `content` TEXT)";
        String b = "CREATE TABLE todo (title TEXT, content TEXT)";
        List<String> statements = migration.tableDiff(a, b);
        assertThat(statements, is(empty()));
    }

    @Test
    public void reorder() throws Exception {
        String from = "CREATE TABLE todo (title TEXT, content TEXT)";
        String to = "CREATE TABLE todo (content TEXT, title TEXT)";
        List<String> statements = migration.tableDiff(from, to);

        assertThat(statements, is(empty()));
    }

    @Test
    public void addOneColumn() throws Exception {
        String from = "CREATE TABLE todo (title TEXT)";
        String to = "CREATE TABLE todo (title TEXT, content TEXT)";
        List<String> statements = migration.tableDiff(from, to);

        assertThat(statements, contains(
                "CREATE TABLE \"__temp_todo\" (\"title\" TEXT, \"content\" TEXT)",
                "INSERT INTO \"__temp_todo\" (\"title\") SELECT \"title\" FROM \"todo\"",
                "DROP TABLE \"todo\"",
                "ALTER TABLE \"__temp_todo\" RENAME TO \"todo\""));
    }

    @Test
    public void addTowColumns() throws Exception {
        String from = "CREATE TABLE todo (title TEXT)";
        String to = "CREATE TABLE todo (title TEXT, content TEXT, createdDate TIMESTAMP)";
        List<String> statements = migration.tableDiff(from, to);

        assertThat(statements, contains(
                "CREATE TABLE \"__temp_todo\" (\"title\" TEXT, \"content\" TEXT, \"createdDate\" TIMESTAMP)",
                "INSERT INTO \"__temp_todo\" (\"title\") SELECT \"title\" FROM \"todo\"",
                "DROP TABLE \"todo\"",
                "ALTER TABLE \"__temp_todo\" RENAME TO \"todo\""));
    }

    @Test
    public void dropOneColumn() throws Exception {
        String from = "CREATE TABLE todo (title TEXT, content TEXT)";
        String to = "CREATE TABLE todo (title TEXT)";
        List<String> statements = migration.tableDiff(from, to);

        assertThat(statements, contains(
                "CREATE TABLE \"__temp_todo\" (\"title\" TEXT)",
                "INSERT INTO \"__temp_todo\" (\"title\") SELECT \"title\" FROM \"todo\"",
                "DROP TABLE \"todo\"",
                "ALTER TABLE \"__temp_todo\" RENAME TO \"todo\""));
    }

    @Test
    public void dropTowColumns() throws Exception {
        String from = "CREATE TABLE todo (title TEXT, content TEXT, createdDate TIMESTAMP)";
        String to = "CREATE TABLE todo (title TEXT)";
        List<String> statements = migration.tableDiff(from, to);

        assertThat(statements, contains(
                "CREATE TABLE \"__temp_todo\" (\"title\" TEXT)",
                "INSERT INTO \"__temp_todo\" (\"title\") SELECT \"title\" FROM \"todo\"",
                "DROP TABLE \"todo\"",
                "ALTER TABLE \"__temp_todo\" RENAME TO \"todo\""));
    }


    @Test
    public void addNonNullConstraint() throws Exception {
        String from = "CREATE TABLE todo (title TEXT, content TEXT)";
        String to = "CREATE TABLE todo (title TEXT, content TEXT NOT NULL)";
        List<String> statements = migration.tableDiff(from, to);

        assertThat(statements, contains(
                "CREATE TABLE \"__temp_todo\" (\"title\" TEXT, \"content\" TEXT NOT NULL)",
                "INSERT INTO \"__temp_todo\" (\"title\", \"content\") SELECT \"title\", \"content\" FROM \"todo\"",
                "DROP TABLE \"todo\"",
                "ALTER TABLE \"__temp_todo\" RENAME TO \"todo\""));
    }

    @Test
    public void removeNonNullConstraint() throws Exception {
        String from = "CREATE TABLE todo (title TEXT, content TEXT NOT NULL)";
        String to = "CREATE TABLE todo (title TEXT, content TEXT)";
        List<String> statements = migration.tableDiff(from, to);

        assertThat(statements, contains(
                "CREATE TABLE \"__temp_todo\" (\"title\" TEXT, \"content\" TEXT)",
                "INSERT INTO \"__temp_todo\" (\"title\", \"content\") SELECT \"title\", \"content\" FROM \"todo\"",
                "DROP TABLE \"todo\"",
                "ALTER TABLE \"__temp_todo\" RENAME TO \"todo\""));
    }

    @Test
    public void addPrimaryKeyConstraint() throws Exception {
        String from = "CREATE TABLE todo (title TEXT, content TEXT)";
        String to = "CREATE TABLE todo (title TEXT PRIMARY KEY, content TEXT)";
        List<String> statements = migration.tableDiff(from, to);

        assertThat(statements, contains(
                "CREATE TABLE \"__temp_todo\" (\"title\" TEXT PRIMARY KEY, \"content\" TEXT)",
                "INSERT INTO \"__temp_todo\" (\"title\", \"content\") SELECT \"title\", \"content\" FROM \"todo\"",
                "DROP TABLE \"todo\"",
                "ALTER TABLE \"__temp_todo\" RENAME TO \"todo\""));
    }

    @Test
    public void addUniqueConstraint() throws Exception {
        String from = "CREATE TABLE todo (title TEXT, content TEXT)";
        String to = "CREATE TABLE todo (title TEXT UNIQUE, content TEXT)";
        List<String> statements = migration.tableDiff(from, to);

        assertThat(statements, contains(
                "CREATE TABLE \"__temp_todo\" (\"title\" TEXT UNIQUE, \"content\" TEXT)",
                "INSERT INTO \"__temp_todo\" (\"title\", \"content\") SELECT \"title\", \"content\" FROM \"todo\"",
                "DROP TABLE \"todo\"",
                "ALTER TABLE \"__temp_todo\" RENAME TO \"todo\""));
    }

    @Test
    public void withQuotedNames() throws Exception {
        String from = "CREATE TABLE \"todo\" (\"title\" TEXT)";
        String to = "CREATE TABLE \"todo\" (\"title\" TEXT, \"content\" TEXT)";
        List<String> statements = migration.tableDiff(from, to);

        assertThat(statements, contains(
                "CREATE TABLE \"__temp_todo\" (\"title\" TEXT, \"content\" TEXT)",
                "INSERT INTO \"__temp_todo\" (\"title\") SELECT \"title\" FROM \"todo\"",
                "DROP TABLE \"todo\"",
                "ALTER TABLE \"__temp_todo\" RENAME TO \"todo\""));
    }

    @Test
    public void addConstraints() throws Exception {
        String from = "CREATE TABLE todo (title TEXT, content TEXT)";
        String to = "CREATE TABLE todo (title TEXT, content TEXT, UNIQUE(title))";
        List<String> statements = migration.tableDiff(from, to);

        assertThat(statements, contains(
                "CREATE TABLE \"__temp_todo\" (\"title\" TEXT, \"content\" TEXT)",
                "INSERT INTO \"__temp_todo\" (\"title\", \"content\") SELECT \"title\", \"content\" FROM \"todo\"",
                "DROP TABLE \"todo\"",
                "ALTER TABLE \"__temp_todo\" RENAME TO \"todo\""
        ));
    }

    @Test
    public void changeTypeFromTextToBlob() throws Exception {
        String from = "CREATE TABLE todo (title TEXT, content TEXT)";
        String to = "CREATE TABLE todo (title TEXT, content BLOB)";
        List<String> statements = migration.tableDiff(from, to);

        assertThat(statements, contains(
                "CREATE TABLE \"__temp_todo\" (\"title\" TEXT, \"content\" BLOB)",
                "INSERT INTO \"__temp_todo\" (\"title\", \"content\") SELECT \"title\", \"content\" FROM \"todo\"",
                "DROP TABLE \"todo\"",
                "ALTER TABLE \"__temp_todo\" RENAME TO \"todo\""
        ));
    }

    @Test
    public void changeTypeFromBlobToText() throws Exception {
        String from = "CREATE TABLE todo (title TEXT, content BLOB)";
        String to = "CREATE TABLE todo (title TEXT, content TEXT)";
        List<String> statements = migration.tableDiff(from, to);

        assertThat(statements, contains(
                "CREATE TABLE \"__temp_todo\" (\"title\" TEXT, \"content\" TEXT)",
                "INSERT INTO \"__temp_todo\" (\"title\", \"content\") SELECT \"title\", \"content\" FROM \"todo\"",
                "DROP TABLE \"todo\"",
                "ALTER TABLE \"__temp_todo\" RENAME TO \"todo\""
        ));
    }

    @Test
    public void addColumnConstraint() throws Exception {
        String from = "CREATE TABLE todo (title TEXT, content TEXT)";
        String to = "CREATE TABLE todo (title TEXT UNIQUE ON CONFLICT IGNORE, content TEXT)";
        List<String> statements = migration.tableDiff(from, to);

        assertThat(statements, contains(
                "CREATE TABLE \"__temp_todo\" (\"title\" TEXT UNIQUE ON CONFLICT IGNORE, \"content\" TEXT)",
                "INSERT INTO \"__temp_todo\" (\"title\", \"content\") SELECT \"title\", \"content\" FROM \"todo\"",
                "DROP TABLE \"todo\"",
                "ALTER TABLE \"__temp_todo\" RENAME TO \"todo\""
        ));
    }

    @Test
    public void removeColumnConstraint() throws Exception {
        String from = "CREATE TABLE todo (title TEXT UNIQUE ON CONFLICT IGNORE, content TEXT)";
        String to = "CREATE TABLE todo (title TEXT, content TEXT)";
        List<String> statements = migration.tableDiff(from, to);

        assertThat(statements, contains(
                "CREATE TABLE \"__temp_todo\" (\"title\" TEXT, \"content\" TEXT)",
                "INSERT INTO \"__temp_todo\" (\"title\", \"content\") SELECT \"title\", \"content\" FROM \"todo\"",
                "DROP TABLE \"todo\"",
                "ALTER TABLE \"__temp_todo\" RENAME TO \"todo\""
        ));
    }

    @Test
    public void addForeignKeyConstraints() throws Exception {
        String a = "CREATE TABLE foo (id INTEGER PRIMARY KEY, bar INTEGER NOT NULL)";
        String b = "CREATE TABLE foo (id INTEGER PRIMARY KEY, bar INTEGER NOT NULL REFERENCES baz (id))";

        List<String> statements = migration.tableDiff(a, b);
        assertThat(statements, contains(
                "CREATE TABLE \"__temp_foo\" (\"id\" INTEGER PRIMARY KEY, \"bar\" INTEGER NOT NULL REFERENCES \"baz\" ( \"id\" ))",
                "INSERT INTO \"__temp_foo\" (\"id\", \"bar\") SELECT \"id\", \"bar\" FROM \"foo\"",
                "DROP TABLE \"foo\"",
                "ALTER TABLE \"__temp_foo\" RENAME TO \"foo\""
        ));
    }
}

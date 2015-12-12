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

import com.github.gfx.android.orma.migration.BuildConfig;
import com.github.gfx.android.orma.migration.SchemaDiffMigration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import android.content.Context;

import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE)
public class TableDiffTest {

    SchemaDiffMigration migration;

    Context getContext() {
        return RuntimeEnvironment.application;
    }

    @Before
    public void setUp() throws Exception {
        migration = new SchemaDiffMigration(getContext());
    }

    @Test
    public void tableDiff_reorder() throws Exception {
        String from = "CREATE TABLE todo (title TEXT, content TEXT)";
        String to = "CREATE TABLE todo (content TEXT, title TEXT)";
        List<String> statements = migration.tableDiff(from, to);

        assertThat(statements, is(empty()));
    }

    @Test
    public void tableDiff_addColumn() throws Exception {
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
    public void tableDiff_addTowColumns() throws Exception {
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
    public void tableDiff_dropColumn() throws Exception {
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
    public void tableDiff_dropTowColumns() throws Exception {
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
    public void tableDiff_addNonNullConstraint() throws Exception {
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
    public void tableDiff_removeNonNullConstraint() throws Exception {
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
    public void tableDiff_addPrimaryKeyConstraint() throws Exception {
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
    public void tableDiff_addUniqueConstraint() throws Exception {
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
    public void tableDiff_withQuotedNames() throws Exception {
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
    public void tableDiff_addConstraints() throws Exception {
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
}

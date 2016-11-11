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

import com.github.gfx.android.orma.migration.SQLiteMaster;
import com.github.gfx.android.orma.migration.SchemaDiffMigration;
import com.github.gfx.android.orma.migration.test.util.OpenHelper;
import com.github.gfx.android.orma.migration.test.util.SchemaData;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
public class SchemaDiffMigrationTest {

    static final String SCHEMA_HASH = "aaa";

    List<SchemaData> schemas;

    List<String> initialData;

    OpenHelper openHelper;

    SchemaDiffMigration migration;

    SQLiteDatabase db;

    Map<String, SQLiteMaster> metadata;

    List<String> statements; // result of each test case

    Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    @Before
    public void setUp() throws Exception {
        schemas = new ArrayList<>(Arrays.asList(
                new SchemaData("foo", "CREATE TABLE `foo` (`field01` TEXT, `field02` TEXT)",
                        "CREATE INDEX `index_field01_on_foo` ON `foo` (`field01`)",
                        "CREATE INDEX `index_field02_on_foo` ON `foo` (`field02`)"
                ),
                new SchemaData("bar", "CREATE TABLE `bar` (`field10` TEXT, `field20` TEXT)")
        ));

        initialData = Arrays.asList(
                "INSERT INTO foo (field01, field02) VALUES ('value01', 'value02')",
                "INSERT INTO bar (field10, field10) VALUES ('value10', 'value10')"
        );

        openHelper = new OpenHelper(getContext(), schemas, initialData);
        db = openHelper.getWritableDatabase();
        migration = new SchemaDiffMigration(getContext(), SCHEMA_HASH);
        metadata = SchemaDiffMigration.loadMetadata(db, schemas);
    }

    @After
    public void tearDown() throws Exception {
        if (statements != null) {
            migration.executeStatements(db, statements);
        }
    }

    @Test
    public void doNothing() throws Exception {
        statements = migration.diffAll(metadata, schemas);
        assertThat(statements, is(empty()));
    }

    @Test
    public void isSchemaChanged() throws Exception {
        db.execSQL("DROP TABLE foo");
        db.execSQL("DROP TABLE bar");
        migration.start(db, schemas);

        assertThat(migration.isSchemaChanged(db), is(false));

        db.execSQL("CREATE TABLE baz (id INTEGER PRIMARY KEY)");

        assertThat(migration.isSchemaChanged(db), is(true));
    }

    @Test
    public void differentCases() throws Exception {
        List<SchemaData> newSchemas = Arrays.asList(
                new SchemaData("FOO", "CREATE TABLE `FOO` (`FIELD01` TEXT, `FIELD02` TEXT)",
                        "CREATE INDEX `INDEX_FIELD01_ON_FOO` ON `FOO` (`FIELD01`)",
                        "CREATE INDEX `INDEX_FIELD02_ON_FOO` ON `FOO` (`FIELD02`)"
                ),
                new SchemaData("BAR", "CREATE TABLE `BAR` (`FIELD10` TEXT, `FIELD20` TEXT)")
        );

        statements = migration.diffAll(metadata, newSchemas);

        assertThat(statements, is(empty()));
    }

    @Test
    public void createTable() throws Exception {
        SchemaData newSchema = new SchemaData("baz", "CREATE TABLE `baz` (`x10` TEXT, `x20` TEXT)");
        schemas.add(newSchema);

        statements = migration.diffAll(metadata, schemas);

        assertThat(statements, is(Collections.singletonList(newSchema.getCreateTableStatement())));
    }

    @Test
    public void createTableAndCreateIndexes() throws Exception {
        SchemaData newSchema = new SchemaData("baz", "CREATE TABLE `baz` (`x01` TEXT, `x02` TEXT)",
                "CREATE INDEX `index_x01_on_baz` ON `baz` (`x01`)",
                "CREATE INDEX `index_x02_on_baz` ON `baz` (`x02`)"
        );
        schemas.add(newSchema);

        statements = migration.diffAll(metadata, schemas);

        assertThat(statements, is(newSchema.getAllTheStatements()));
    }

    @Test
    public void createIndexes() throws Exception {
        schemas.get(1).addCreateIndexStatements(
                "CREATE INDEX `index_field10_on_bar` ON `bar` (`field10`)",
                "CREATE INDEX `index_field20_on_bar` ON `bar` (`field20`)"
        );

        statements = migration.diffAll(metadata, schemas);

        assertThat(statements, is(schemas.get(1).getCreateIndexStatements()));
    }

    @Test
    public void recreateTableWithIndexes() throws Exception {
        schemas.set(0, new SchemaData("foo", "CREATE TABLE `foo` (`field01` TEXT, `field02` TEXT, `field03` TEXT)",
                "CREATE INDEX `index_field01_on_foo` ON `foo` (`field01`)",
                "CREATE INDEX `index_field02_on_foo` ON `foo` (`field02`)"
        ));
        statements = migration.diffAll(metadata, schemas);
        migration.executeStatements(db, statements);

        assertThat(migration.diffAll(SchemaDiffMigration.loadMetadata(db, schemas), schemas), is(empty()));
    }

    @Test
    public void migrationStepTableMigration1To2() throws Exception {
        // setup v1 table
        db.execSQL("CREATE TABLE IF NOT EXISTS "
                        + SchemaDiffMigration.MIGRATION_STEPS_TABLE_1 + " ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        // no `db_version`
                        + "version_name TEXT NOT NULL, "
                        + "version_code INTEGER NOT NULL, "
                        + "schema_hash TEXT NOT NULL, "
                        + "sql TEXT NULL, "
                        + "args TEXT NULL, "
                        + "created_timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP)"
        );
        db.execSQL("INSERT INTO " + SchemaDiffMigration.MIGRATION_STEPS_TABLE_1
                        + " (version_name, version_code, schema_hash, sql, args)"
                        + " VALUES"
                        + " ('1.2.3', 123, 'deadbeef', '--', '[]')"
        );

        migration.start(db, schemas);

        assertThat(SQLiteMaster.checkIfTableNameExists(db, SchemaDiffMigration.MIGRATION_STEPS_TABLE_1), is(false));
        assertThat(SQLiteMaster.checkIfTableNameExists(db, SchemaDiffMigration.MIGRATION_STEPS_TABLE), is(true));

        assertThat(migration.isSchemaChanged(db), is(false));
    }
}

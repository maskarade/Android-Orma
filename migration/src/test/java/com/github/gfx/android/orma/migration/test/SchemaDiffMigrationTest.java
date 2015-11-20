package com.github.gfx.android.orma.migration.test;

import com.github.gfx.android.orma.migration.BuildConfig;
import com.github.gfx.android.orma.migration.NamedDdl;
import com.github.gfx.android.orma.migration.SQLiteMaster;
import com.github.gfx.android.orma.migration.SchemaDiffMigration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE)
public class SchemaDiffMigrationTest {

    static final List<NamedDdl> namedDdls = Arrays.asList(
          new NamedDdl("foo", "CREATE TABLE \"foo\" (\"field01\" TEXT, \"field02\" TEXT)", Collections.<String>emptyList()),
          new NamedDdl("bar", "CREATE TABLE \"bar\" (\"field10\" TEXT, \"field20\" TEXT)", Collections.<String>emptyList())
    );

    static class OpenHelper extends SQLiteOpenHelper {

        public OpenHelper(Context context) {
            super(context, null, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            for (NamedDdl ddl : namedDdls) {
                db.execSQL(ddl.getCreateTableStatement());
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    SchemaDiffMigration migration;

    SQLiteDatabase db;

    Context getContext() {
        return RuntimeEnvironment.application;
    }

    @Before
    public void setUp() throws Exception {
        OpenHelper openHelper = new OpenHelper(getContext());

        db = openHelper.getWritableDatabase();

        migration = new SchemaDiffMigration(getContext());
    }

    @Test
    public void start() throws Exception {
        migration.start(db, namedDdls);
    }

    @Test
    public void diffAll_doNothing() throws Exception {
        Map<String, SQLiteMaster> metadata = migration.loadMetadata(db, namedDdls);

        List<String> statements = migration.diffAll(namedDdls, metadata);

        assertThat(statements, is(empty()));
    }

    @Test
    public void diffAll_createTable() throws Exception {
        Map<String, SQLiteMaster> metadata = migration.loadMetadata(db,  namedDdls.subList(0, 1));

        List<String> statements = migration.diffAll(namedDdls, metadata);

        assertThat(statements, contains(namedDdls.get(1).getCreateTableStatement()));
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
    public void tableDiff_withuQuotedNames() throws Exception {
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
    public void buildDropIndexStatement() throws Exception {
        assertThat(migration.buildDropIndexStatement("CREATE INDEX IF NOT EXISTS index_foo ON foo (bar)"),
                is("DROP INDEX IF EXISTS \"index_foo\""));
    }

    @Test
    public void buildDropIndexStatement_caseInsensitive() throws Exception {
        assertThat(migration.buildDropIndexStatement("create index if not exists index_foo on foo (bar)"),
                is("DROP INDEX IF EXISTS \"index_foo\""));
    }

    @Test
    public void buildDropIndexStatement_spaceInsensitive() throws Exception {
        assertThat(migration.buildDropIndexStatement("create \n"
                        + "index \n"
                        + "if \n"
                        + "not \n"
                        + "exists \n"
                        + "index_foo \n"
                        + "on \n"
                        + "foo \n"
                        + "(bar)\n"),
                is("DROP INDEX IF EXISTS \"index_foo\""));
    }

    @Test
    public void buildDropIndexStatement_omitIfNotExists() throws Exception {
        assertThat(migration.buildDropIndexStatement("CREATE INDEX index_foo ON foo (bar)"),
                is("DROP INDEX IF EXISTS \"index_foo\""));
    }

    @Test
    public void buildDropIndexStatement_doubleQuotedNames() throws Exception {
        assertThat(migration.buildDropIndexStatement("CREATE INDEX IF NOT EXISTS \"index_foo\" ON \"foo\" (\"bar\")"),
                is("DROP INDEX IF EXISTS \"index_foo\""));
    }

    @Test
    public void buildDropIndexStatement_backQuotedNames() throws Exception {
        assertThat(migration.buildDropIndexStatement("CREATE INDEX IF NOT EXISTS `index_foo` ON `foo` (`bar`)"),
                is("DROP INDEX IF EXISTS \"index_foo\""));
    }
}

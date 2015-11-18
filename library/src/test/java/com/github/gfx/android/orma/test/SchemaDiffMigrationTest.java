package com.github.gfx.android.orma.test;

import com.github.gfx.android.orma.BuildConfig;
import com.github.gfx.android.orma.migration.SQLiteMaster;
import com.github.gfx.android.orma.migration.SchemaDiffMigration;
import com.github.gfx.android.orma.test.model.OrmaDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE)
public class SchemaDiffMigrationTest {

    SchemaDiffMigration migration;

    OrmaDatabase orma;

    Context getContext() {
        return RuntimeEnvironment.application;
    }


    @Before
    public void setUp() throws Exception {
        orma = new OrmaDatabase(getContext(), null);

        migration = new SchemaDiffMigration();
    }

    @Test
    public void start() throws Exception {
        SQLiteDatabase db = orma.getConnection().getDatabase();
        migration.start(db, OrmaDatabase.schemas);
    }

    @Test
    public void diffAll() throws Exception {
        SQLiteDatabase db = orma.getConnection().getDatabase();
        Map<String, SQLiteMaster> metadata = migration.loadMetadata(db, OrmaDatabase.schemas);

        List<String> statements = migration.diffAll(OrmaDatabase.schemas, metadata);

        assertThat(statements, is(empty()));
    }

    @Test
    public void tableDiff_addColumn() throws Exception {
        String from = "CREATE TABLE todo (title TEXT, content TEXT)";
        String to = "CREATE TABLE todo (id TEXT, title TEXT, content TEXT)";
        List<String> statements = migration.tableDiff(from, to);
        assertThat(statements, contains("ALTER TABLE \"todo\" ADD COLUMN \"id\" TEXT"));
    }

    @Test
    public void tableDiff_addUniqueConstraint() throws Exception {
        String from = "CREATE TABLE todo (title TEXT, content TEXT)";
        String to = "CREATE TABLE todo (title TEXT UNIQUE, content TEXT)";
        List<String> statements = migration.tableDiff(from, to);
        assertThat(statements, contains("ALTER TABLE \"todo\" ADD COLUMN \"title\" TEXT UNIQUE"));
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

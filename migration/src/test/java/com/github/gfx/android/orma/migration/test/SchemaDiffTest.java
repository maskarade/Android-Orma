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
public class SchemaDiffTest {

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
}

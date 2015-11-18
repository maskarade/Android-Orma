package com.github.gfx.android.orma.test;

import com.github.gfx.android.orma.BuildConfig;
import com.github.gfx.android.orma.migration.OrmaMigration;
import com.github.gfx.android.orma.migration.SQLiteMaster;
import com.github.gfx.android.orma.test.model.OrmaDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import android.content.Context;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE)
public class OrmaMigrationTest {

    OrmaMigration migration;

    OrmaDatabase db;

    Context getContext() {
        return RuntimeEnvironment.application;
    }


    @Before
    public void setUp() throws Exception {
        db = new OrmaDatabase(getContext(), "migration-test.db");
        db.getConnection().resetDatabase();

        migration = new OrmaMigration();
    }


    @Test
    public void diffAll() throws Exception {
        Map<String, SQLiteMaster> metadata = migration.loadMetadata(db.getConnection().getDatabase(), db.schemas);

        List<String> statements = migration.diffAll(OrmaDatabase.schemas, metadata);

        assertThat(statements, is(empty()));
    }
}

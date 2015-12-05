package com.github.gfx.android.orma.test;

import com.github.gfx.android.orma.BuildConfig;
import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.migration.MigrationEngine;
import com.github.gfx.android.orma.migration.OrmaMigration;
import com.github.gfx.android.orma.test.model.OrmaDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import android.content.Context;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE)
public class MigrationEngineTest {

    MigrationEngine migration;

    OrmaConnection conn;

    Context getContext() {
        return RuntimeEnvironment.application;
    }

    @Before
    public void setUp() throws Exception {
        migration = new OrmaMigration(getContext(), 1, false);
        conn = OrmaDatabase.builder(getContext())
                .name(null)
                .migrationEngine(migration)
                .build()
                .getConnection();
    }

    @Test
    public void startEmpty() throws Exception {
        migration.start(conn.getWritableDatabase(), conn.getSchemas());
        migration.start(conn.getWritableDatabase(), conn.getSchemas());
        migration.start(conn.getWritableDatabase(), conn.getSchemas());
    }
}

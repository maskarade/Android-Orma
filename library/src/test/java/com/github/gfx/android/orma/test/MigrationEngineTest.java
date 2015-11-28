package com.github.gfx.android.orma.test;

import com.github.gfx.android.orma.BuildConfig;
import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.migration.SchemaDiffMigration;
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

    SchemaDiffMigration migration;

    OrmaConnection conn;

    Context getContext() {
        return RuntimeEnvironment.application;
    }

    @Before
    public void setUp() throws Exception {
        conn = OrmaDatabase.builder(getContext())
                .name(null)
                .build()
                .getConnection();
        migration = new SchemaDiffMigration(getContext(), false);
    }

    @Test
    public void startEmpty() throws Exception {
        migration.start(conn.getWritableDatabase(), conn.getNamedDdls());
    }

}

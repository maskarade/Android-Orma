package com.github.gfx.android.orma.migration.test;

import com.github.gfx.android.orma.migration.BuildConfig;
import com.github.gfx.android.orma.migration.ManualStepMigration;
import com.github.gfx.android.orma.migration.NamedDdl;
import com.github.gfx.android.orma.migration.OrmaMigration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.util.ArrayList;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE)
public class OrmaMigrationTest {

    static int VERSION = 100;

    SQLiteDatabase db;

    OrmaMigration engine;

    static Context getContext() {
        return RuntimeEnvironment.application;
    }

    @Before
    public void setUp() throws Exception {
        db = SQLiteDatabase.create(null);

        engine = new OrmaMigration(getContext(), VERSION, BuildConfig.DEBUG);
        engine.addStep(2, new ManualStepMigration.ChangeStep() {
            @Override
            public void change(@NonNull ManualStepMigration.Helper helper) {
                helper.execSQL("CREATE TABLE step_2 (id INTEGER PRIMARY KEY)");
            }
        });
        engine.addStep(4, new ManualStepMigration.ChangeStep() {
            @Override
            public void change(@NonNull ManualStepMigration.Helper helper) {
                helper.execSQL("CREATE TABLE step_4 (id INTEGER PRIMARY KEY)");
            }
        });
        engine.addStep(8, new ManualStepMigration.ChangeStep() {
            @Override
            public void change(@NonNull ManualStepMigration.Helper helper) {
                helper.execSQL("CREATE TABLE step_8 (id INTEGER PRIMARY KEY)");
            }
        });

        engine.addStep(16, new ManualStepMigration.ChangeStep() {
            @Override
            public void change(@NonNull ManualStepMigration.Helper helper) {
                helper.execSQL("CREATE TABLE step_16 (id INTEGER PRIMARY KEY)");
            }
        });
    }

    @Test
    public void testIdempotenceWithNop() throws Exception {
        engine.start(db, new ArrayList<NamedDdl>());
        engine.start(db, new ArrayList<NamedDdl>());
        engine.start(db, new ArrayList<NamedDdl>());
    }
}

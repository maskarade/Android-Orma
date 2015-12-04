package com.github.gfx.android.orma.test;

import com.github.gfx.android.orma.migration.BuildConfig;
import com.github.gfx.android.orma.migration.ManualStepMigration;
import com.github.gfx.android.orma.migration.NamedDdl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE)
public class ManualStepMigrationTest {

    static int VERSION = 100;

    SQLiteDatabase db;

    ManualStepMigration engine;

    List<StepContext> seq;


    @Before
    public void setUp() throws Exception {
        db = SQLiteDatabase.create(null);

        engine = new ManualStepMigration(VERSION, BuildConfig.DEBUG);
        engine.createMigrationHistoryTable(db);
        engine.imprintStep(db, 1, null);

        seq = new ArrayList<>();

        engine.addStep(2, new ManualStepMigration.Step() {
            @Override
            public void change(@NonNull ManualStepMigration.Helper helper) {
                seq.add(new StepContext(helper.version, helper.upgrade));

                helper.execSQL("CREATE TABLE step_2 (id INTEGER PRIMARY KEY)");
            }
        });
        engine.addStep(4, new ManualStepMigration.Step() {
            @Override
            public void change(@NonNull ManualStepMigration.Helper helper) {
                seq.add(new StepContext(helper.version, helper.upgrade));

                helper.execSQL("CREATE TABLE step_4 (id INTEGER PRIMARY KEY)");
            }
        });
        engine.addStep(8, new ManualStepMigration.Step() {
            @Override
            public void change(@NonNull ManualStepMigration.Helper helper) {
                seq.add(new StepContext(helper.version, helper.upgrade));

                helper.execSQL("CREATE TABLE step_8 (id INTEGER PRIMARY KEY)");
            }
        });

        engine.addStep(16, new ManualStepMigration.Step() {
            @Override
            public void change(@NonNull ManualStepMigration.Helper helper) {
                seq.add(new StepContext(helper.version, helper.upgrade));

                helper.execSQL("CREATE TABLE step_16 (id INTEGER PRIMARY KEY)");
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        db.close();
    }

    @Test
    public void testIdempotenceWithNop() throws Exception {
        engine.start(db, new ArrayList<NamedDdl>());
        engine.start(db, new ArrayList<NamedDdl>());
        engine.start(db, new ArrayList<NamedDdl>());
    }

    @Test
    public void upgradeFull() throws Exception {
        engine.upgrade(db, 1, 100);

        assertThat(engine.getDbVersion(db), is(16));

        assertThat(seq.size(), is(4));

        assertThat(seq.get(0).version, is(2));
        assertThat(seq.get(0).upgrade, is(true));

        assertThat(seq.get(1).version, is(4));
        assertThat(seq.get(1).upgrade, is(true));

        assertThat(seq.get(2).version, is(8));
        assertThat(seq.get(2).upgrade, is(true));

        assertThat(seq.get(3).version, is(16));
        assertThat(seq.get(3).upgrade, is(true));
    }

    @Test
    public void upgradeBoundary() throws Exception {
        engine.upgrade(db, 2, 8);

        assertThat(engine.getDbVersion(db), is(8));

        assertThat(seq.size(), is(2));

        assertThat(seq.get(0).version, is(4));
        assertThat(seq.get(0).upgrade, is(true));

        assertThat(seq.get(1).version, is(8));
        assertThat(seq.get(1).upgrade, is(true));
    }

    @Test
    public void downgradeFull() throws Exception {
        engine.downgrade(db, 100, 1);

        assertThat(engine.getDbVersion(db), lessThan(4));

        assertThat(seq.size(), is(4));

        assertThat(seq.get(0).version, is(16));
        assertThat(seq.get(0).upgrade, is(false));

        assertThat(seq.get(1).version, is(8));
        assertThat(seq.get(1).upgrade, is(false));

        assertThat(seq.get(2).version, is(4));
        assertThat(seq.get(2).upgrade, is(false));

        assertThat(seq.get(3).version, is(2));
        assertThat(seq.get(3).upgrade, is(false));
    }

    @Test
    public void downgradeBoundary() throws Exception {
        engine.downgrade(db, 8, 2);

        assertThat(engine.getDbVersion(db), lessThan(4));

        assertThat(seq.size(), is(2));

        assertThat(seq.get(0).version, is(8));
        assertThat(seq.get(0).upgrade, is(false));

        assertThat(seq.get(1).version, is(4));
        assertThat(seq.get(1).upgrade, is(false));
    }

    static class StepContext {

        final int version;

        final boolean upgrade;

        public StepContext(int version, boolean upgrade) {
            this.version = version;
            this.upgrade = upgrade;
        }
    }
}

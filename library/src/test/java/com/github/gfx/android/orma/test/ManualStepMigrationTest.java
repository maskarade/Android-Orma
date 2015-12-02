package com.github.gfx.android.orma.test;

import com.github.gfx.android.orma.migration.BuildConfig;
import com.github.gfx.android.orma.migration.ManualStepMigration;
import com.github.gfx.android.orma.migration.NamedDdl;

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
import static org.hamcrest.core.Is.is;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE)
public class ManualStepMigrationTest {

    static int VERSION = 10;

    SQLiteDatabase db;

    ManualStepMigration engine;

    @Before
    public void setUp() throws Exception {
        db = SQLiteDatabase.create(null);
        engine = new ManualStepMigration(VERSION, BuildConfig.DEBUG);
    }

    @Test
    public void testIdempotenceWithNop() throws Exception {
        engine.start(db, new ArrayList<NamedDdl>());
        engine.start(db, new ArrayList<NamedDdl>());
        engine.start(db, new ArrayList<NamedDdl>());
    }

    @Test
    public void upgradeTwo() throws Exception {
        final List<MigrationVersions> seq = new ArrayList<>();

        engine.addStep(1, 2, new ManualStepMigration.Step() {
            @Override
            public void run(@NonNull ManualStepMigration.Helper helper) {
                seq.add(new MigrationVersions(helper.oldVersion, helper.newVersion));
            }
        });
        engine.addStep(2, 3, new ManualStepMigration.Step() {
            @Override
            public void run(@NonNull ManualStepMigration.Helper helper) {
                seq.add(new MigrationVersions(helper.oldVersion, helper.newVersion));
            }
        });

        engine.upgrade(db, 1, 10);

        assertThat(seq.get(0).oldVersion, is(1));
        assertThat(seq.get(0).newVersion, is(2));

        assertThat(seq.get(1).oldVersion, is(2));
        assertThat(seq.get(1).newVersion, is(3));
    }

    @Test
    public void downgradeTwo() throws Exception {
        final List<MigrationVersions> seq = new ArrayList<>();

        engine.addStep(1, 2, new ManualStepMigration.Step() {
            @Override
            public void run(@NonNull ManualStepMigration.Helper helper) {
                seq.add(new MigrationVersions(helper.oldVersion, helper.newVersion));
            }
        });
        engine.addStep(2, 3, new ManualStepMigration.Step() {
            @Override
            public void run(@NonNull ManualStepMigration.Helper helper) {
                seq.add(new MigrationVersions(helper.oldVersion, helper.newVersion));
            }
        });

        engine.downgrade(db, 10, 1);

        assertThat(seq.get(0).oldVersion, is(3));
        assertThat(seq.get(0).newVersion, is(2));

        assertThat(seq.get(1).oldVersion, is(2));
        assertThat(seq.get(1).newVersion, is(1));
    }

    @Test
    public void testStart_1_to_10() throws Exception {
        final List<MigrationVersions> seq = new ArrayList<>();

        engine.addStep(1, 2, new ManualStepMigration.Step() {
            @Override
            public void run(@NonNull ManualStepMigration.Helper helper) {
                seq.add(new MigrationVersions(helper.oldVersion, helper.newVersion));

                helper.execSQL("CREATE TABLE step_1_2 (id INTEGER PRIMARY KEY)");
            }
        });
        engine.addStep(2, 3, new ManualStepMigration.Step() {
            @Override
            public void run(@NonNull ManualStepMigration.Helper helper) {
                seq.add(new MigrationVersions(helper.oldVersion, helper.newVersion));

                helper.execSQL("CREATE TABLE step_2_3 (id INTEGER PRIMARY KEY)");
            }
        });

        engine.createMigrationHistoryTable(db);

        engine.imprintStep(db, 1, null);

        assertThat(engine.getDbVersion(db), is(1));

        engine.start(db, new ArrayList<NamedDdl>());

        assertThat(seq.get(0).oldVersion, is(1));
        assertThat(seq.get(0).newVersion, is(2));

        assertThat(seq.get(1).oldVersion, is(2));
        assertThat(seq.get(1).newVersion, is(3));

        assertThat(engine.getDbVersion(db), is(3));
    }

    static class MigrationVersions {

        int oldVersion;

        int newVersion;

        public MigrationVersions(int oldVersion, int newVersion) {
            this.oldVersion = oldVersion;
            this.newVersion = newVersion;
        }
    }
}

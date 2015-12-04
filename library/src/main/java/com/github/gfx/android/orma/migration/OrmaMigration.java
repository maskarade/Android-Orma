package com.github.gfx.android.orma.migration;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.util.List;

public class OrmaMigration implements MigrationEngine {

    final ManualStepMigration manualStepMigration;

    final SchemaDiffMigration schemaDiffMigration;

    public OrmaMigration(@NonNull Context context, boolean trace) {
        manualStepMigration = new ManualStepMigration(context, trace);
        schemaDiffMigration = new SchemaDiffMigration(context, trace);
    }

    public void addStep(int newVersion, @NonNull ManualStepMigration.Step step) {
        manualStepMigration.addStep(newVersion, step);
    }

    @Override
    public int getVersion() {
        return schemaDiffMigration.getVersion();
    }

    @Override
    public void start(@NonNull SQLiteDatabase db, @NonNull List<NamedDdl> schemas) {
        manualStepMigration.start(db, schemas);
        schemaDiffMigration.start(db, schemas);
    }
}

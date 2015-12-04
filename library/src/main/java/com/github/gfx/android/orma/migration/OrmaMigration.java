package com.github.gfx.android.orma.migration;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.util.List;

public class OrmaMigration implements MigrationEngine {

    final ManualStepMigration manualStepMigration;

    final SchemaDiffMigration schemaDiffMigration;

    public OrmaMigration(@NonNull Context context, int version, boolean trace) {
        manualStepMigration = new ManualStepMigration(version, trace);
        schemaDiffMigration = new SchemaDiffMigration(context, trace);
    }

    public OrmaMigration(@NonNull Context context, int version) {
        this(context, version, extractDebuggable(context));
    }

    static boolean extractDebuggable(Context context) {
        return (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)
                == ApplicationInfo.FLAG_DEBUGGABLE;
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

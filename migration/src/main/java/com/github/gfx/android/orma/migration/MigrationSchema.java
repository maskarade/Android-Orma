package com.github.gfx.android.orma.migration;

import java.util.List;

public interface MigrationSchema {

    String getTableName();

    String getCreateTableStatement();

    List<String> getCreateIndexStatements();
}

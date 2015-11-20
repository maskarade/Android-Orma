package com.github.gfx.android.orma.migration;

import java.util.ArrayList;
import java.util.List;

public class SQLiteMaster {

    public String type;

    public String name;

    public String tableName;

    public String sql;

    public List<SQLiteMaster> indexes = new ArrayList<>();

    public SQLiteMaster() {
    }

    public SQLiteMaster(String type, String name, String tableName, String sql) {
        this.type = type;
        this.name = name;
        this.tableName = tableName;
        this.sql = sql;
    }
}

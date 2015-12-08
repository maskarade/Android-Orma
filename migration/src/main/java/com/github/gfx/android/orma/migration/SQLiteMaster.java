/*
 * Copyright (c) 2015 FUJI Goro (gfx).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

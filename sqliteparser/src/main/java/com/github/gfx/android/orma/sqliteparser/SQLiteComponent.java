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

package com.github.gfx.android.orma.sqliteparser;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class of SQLite components
 */
public class SQLiteComponent {

    protected final List<String> tokens = new ArrayList<>();

    public List<String> getTokens() {
        return tokens;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SQLiteComponent)) {
            return false;
        }

        SQLiteComponent that = (SQLiteComponent) o;
        return tokens.equals(that.tokens);

    }

    @Override
    public int hashCode() {
        return tokens.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (String token : tokens) {
            if (sb.length()!= 0) {
                sb.append(' ');
            }
            sb.append(token);
        }

        return sb.toString();
    }
}

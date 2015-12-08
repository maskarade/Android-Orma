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
package com.github.gfx.android.orma.adapter;

import org.json.JSONArray;
import org.json.JSONException;

import android.support.annotation.NonNull;

import java.util.HashSet;
import java.util.Set;

public class StringSetAdapter extends AbstractTypeAdapter<Set<String>> {

    @NonNull
    @Override
    public String serialize(@NonNull Set<String> source) {
        JSONArray array = new JSONArray();
        for (String s : source) {
            array.put(s);
        }
        return array.toString();
    }

    @NonNull
    @Override
    public Set<String> deserialize(@NonNull String serialized) {
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(serialized);
            Set<String> set = new HashSet<>(jsonArray.length());

            for (int i = 0; i < jsonArray.length(); i++) {
                set.add(jsonArray.getString(i));
            }
            return set;
        } catch (JSONException e) {
            return new HashSet<>();
        }
    }
}

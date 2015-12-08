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

import java.util.ArrayList;
import java.util.List;

public class StringListAdapter extends AbstractTypeAdapter<List<String>> {

    @NonNull
    @Override
    public String serialize(@NonNull List<String> source) {
        JSONArray array = new JSONArray();
        for (String s : source) {
            array.put(s);
        }
        return array.toString();
    }

    @NonNull
    @Override
    public List<String> deserialize(@NonNull String serialized) {
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(serialized);
            List<String> list = new ArrayList<>(jsonArray.length());

            for (int i = 0; i < jsonArray.length(); i++) {
                list.add(jsonArray.getString(i));
            }
            return list;
        } catch (JSONException e) {
            return new ArrayList<>();
        }
    }
}

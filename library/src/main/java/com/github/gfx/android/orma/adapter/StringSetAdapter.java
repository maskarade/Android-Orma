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

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

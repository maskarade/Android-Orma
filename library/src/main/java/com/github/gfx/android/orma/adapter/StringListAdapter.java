package com.github.gfx.android.orma.adapter;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class StringListAdapter extends AbstractTypeAdapter<List<String>> {

    @SuppressWarnings("unchecked")
    @Override
    public String serialize(List<String> source) {
        JSONArray array = new JSONArray();
        for (String s : source) {
            array.put(s);
        }
        return array.toString();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> deserialize(String serialized) {
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

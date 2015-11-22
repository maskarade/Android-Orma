package com.github.gfx.android.orma.adapter;

import android.support.annotation.NonNull;

import java.util.Date;

public class DateAdapter extends AbstractTypeAdapter<Date> {

    @NonNull
    @Override
    public String serialize(@NonNull Date source) {
        return Long.toString(source.getTime());
    }

    @NonNull
    @Override
    public Date deserialize(@NonNull String serialized) {
        return new Date(Long.parseLong(serialized));
    }
}

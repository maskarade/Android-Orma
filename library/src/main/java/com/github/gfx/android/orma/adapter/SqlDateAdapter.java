package com.github.gfx.android.orma.adapter;

import android.support.annotation.NonNull;

import java.sql.Date;

/**
 * Handles {@link Date} as a string representation like {@code "2015-12-23"}.
 */
public class SqlDateAdapter extends AbstractTypeAdapter<Date> {

    @NonNull
    @Override
    public String serialize(@NonNull Date source) {
        return source.toString();
    }

    @NonNull
    @Override
    public Date deserialize(@NonNull String serialized) {
        return Date.valueOf(serialized);
    }
}

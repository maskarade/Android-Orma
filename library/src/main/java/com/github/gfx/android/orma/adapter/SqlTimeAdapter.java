package com.github.gfx.android.orma.adapter;

import android.support.annotation.NonNull;

import java.sql.Time;

/**
 * Handles {@link Time} as a string representation like {@code "12:30:45"}.
 */
public class SqlTimeAdapter extends AbstractTypeAdapter<Time> {

    @NonNull
    @Override
    public String serialize(@NonNull Time source) {
        return source.toString();
    }

    @NonNull
    @Override
    public Time deserialize(@NonNull String serialized) {
        return Time.valueOf(serialized);
    }
}

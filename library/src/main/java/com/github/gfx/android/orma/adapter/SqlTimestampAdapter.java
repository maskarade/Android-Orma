package com.github.gfx.android.orma.adapter;

import android.support.annotation.NonNull;

import java.sql.Timestamp;

/**
 * Handles {@link Timestamp} as a string representation like {@code "2015-12-23 18:23:45"}.
 */
public class SqlTimestampAdapter extends AbstractTypeAdapter<Timestamp> {

    @NonNull
    @Override
    public String serialize(@NonNull Timestamp source) {
        return source.toString();
    }

    @NonNull
    @Override
    public Timestamp deserialize(@NonNull String serialized) {
        return Timestamp.valueOf(serialized);
    }
}

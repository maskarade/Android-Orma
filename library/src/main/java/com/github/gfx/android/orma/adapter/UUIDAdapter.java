package com.github.gfx.android.orma.adapter;

import android.support.annotation.NonNull;

import java.util.UUID;

public class UUIDAdapter extends AbstractTypeAdapter<UUID> {

    @NonNull
    @Override
    public String serialize(@NonNull UUID source) {
        return source.toString();
    }

    @NonNull
    @Override
    public UUID deserialize(@NonNull String serialized) {
        return UUID.fromString(serialized);
    }
}

package com.github.gfx.android.orma;

import android.support.annotation.NonNull;

public interface ModelBuilder<T> {

    @NonNull
    T build();
}

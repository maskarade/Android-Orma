package com.github.gfx.android.orma;

import android.support.annotation.NonNull;

public interface ModelFactory<T> {

    @NonNull
    T create();
}

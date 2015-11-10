package com.github.gfx.android.orma;

import android.content.ContentValues;

public interface ModelBuilder<T> {

    T build();

    ContentValues buildContentValues();
}

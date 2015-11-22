package com.github.gfx.android.orma.adapter;

import android.net.Uri;
import android.support.annotation.NonNull;

public class UriAdapter extends AbstractTypeAdapter<Uri> {

    @NonNull
    @Override
    public String serialize(@NonNull Uri source) {
        return source.toString();
    }

    @NonNull
    @Override
    public Uri deserialize(@NonNull String serialized) {
        return Uri.parse(serialized);
    }
}

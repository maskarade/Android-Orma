package com.github.gfx.android.orma.adapter;

import android.net.Uri;

public class UriAdapter extends AbstractTypeAdapter<Uri> {

    @Override
    public String serialize(Uri source) {
        return source.toString();
    }

    @Override
    public Uri deserialize(String serialized) {
        return Uri.parse(serialized);
    }
}

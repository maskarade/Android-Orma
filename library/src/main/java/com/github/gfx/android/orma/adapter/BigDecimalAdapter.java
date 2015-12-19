package com.github.gfx.android.orma.adapter;

import android.support.annotation.NonNull;

import java.math.BigDecimal;

public class BigDecimalAdapter extends AbstractTypeAdapter<BigDecimal> {

    @NonNull
    @Override
    public String serialize(@NonNull BigDecimal source) {
        return source.toString();
    }

    @NonNull
    @Override
    public BigDecimal deserialize(@NonNull String serialized) {
        return new BigDecimal(serialized);
    }
}

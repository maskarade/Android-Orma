package com.github.gfx.android.orma.adapter;

import android.support.annotation.NonNull;

import java.math.BigInteger;

public class BigIntegerAdapter extends AbstractTypeAdapter<BigInteger> {

    @NonNull
    @Override
    public String serialize(@NonNull BigInteger source) {
        return source.toString();
    }

    @NonNull
    @Override
    public BigInteger deserialize(@NonNull String serialized) {
        return new BigInteger(serialized);
    }
}

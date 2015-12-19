package com.github.gfx.android.orma.adapter;

import android.support.annotation.NonNull;

import java.util.Currency;

public class CurrencyAdapter extends AbstractTypeAdapter<Currency> {

    @NonNull
    @Override
    public String serialize(@NonNull Currency source) {
        return source.getCurrencyCode();
    }

    @NonNull
    @Override
    public Currency deserialize(@NonNull String serialized) {
        return Currency.getInstance(serialized);
    }
}

package com.github.gfx.android.orma.test.model;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.Table;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

@Table
public class ModelWithConditionHelpers {

    @Column(indexed = true)
    @Nullable
    public String nullableText;

    @Column(indexed = true)
    @NonNull
    public String nonNullText = "";

    @Column(indexed = true)
    public boolean booleanValue;

    @Column(indexed = true)
    public byte byteValue;

    @Column(indexed = true)
    public short shortValue;

    @Column(indexed = true)
    public int intValue;

    @Column(indexed = true)
    public long longValue;

    @Column(indexed = true)
    public float floatValue;

    @Column(indexed = true)
    public double doubleValue;

}

package com.github.gfx.android.orma.test.model;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.Table;

@Table
public class ModelWithPrimitives {

    @Column
    public boolean booleanValue;

    @Column
    public byte byteValue;

    @Column
    public short shortValue;

    @Column
    public int intValue;

    @Column
    public long longValue;

    @Column
    public float floatValue;

    @Column
    public double doubleValue;

}

package com.github.gfx.android.orma.test.model;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.Table;

@Table
public class ModelWithBoxTypes {

    @Column
    public Boolean booleanValue;

    @Column
    public Byte byteValue;

    @Column
    public Short shortValue;

    @Column
    public Integer intValue;

    @Column
    public Long longValue;

    @Column
    public Float floatValue;

    @Column
    public Double doubleValue;

}

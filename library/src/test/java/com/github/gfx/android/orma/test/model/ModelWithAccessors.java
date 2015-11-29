package com.github.gfx.android.orma.test.model;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.Getter;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Setter;
import com.github.gfx.android.orma.annotation.Table;

@Table
public class ModelWithAccessors {
     static final String kId = "id";
    static final String kKey = "key";
    static final String kValue = "value";


    @PrimaryKey(kId)
    private long id;

    @Column(kKey)
    private String key;

    @Column(kValue)
    private String value;


    @Getter(kId)
    public long getId() {
        return id;
    }

    @Setter(kId)
    public void setId(long id) {
        this.id = id;
    }

    @Getter(kKey)
    public String getKey() {
        return key;
    }

    @Setter(kKey)
    public void setKey(String key) {
        this.key = key;
    }

    @Getter(kValue)
    public String getValue() {
        return value;
    }

    @Setter(kValue)
    public void setValue(String value) {
        this.value = value;
    }
}

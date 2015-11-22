package com.github.gfx.android.orma.test.model;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.Getter;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Setter;
import com.github.gfx.android.orma.annotation.Table;

@Table
public class ModelWithAccessors {

    @PrimaryKey
    private long id;

    @Column
    private String key;

    @Column
    private String value;


    @Getter("id")
    public long getId() {
        return id;
    }

    @Setter("id")
    public void setId(long id) {
        this.id = id;
    }

    @Getter("key")
    public String getKey() {
        return key;
    }

    @Setter("key")
    public void setKey(String key) {
        this.key = key;
    }

    @Getter("value")
    public String getValue() {
        return value;
    }

    @Setter("value")
    public void setValue(String value) {
        this.value = value;
    }
}

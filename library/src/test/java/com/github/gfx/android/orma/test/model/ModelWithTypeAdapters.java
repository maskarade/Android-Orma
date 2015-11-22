package com.github.gfx.android.orma.test.model;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Table;

import android.net.Uri;
import android.support.annotation.Nullable;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Table
public class ModelWithTypeAdapters {

    @PrimaryKey
    public long id;

    @Column
    public List<String> list;

    @Column
    public Set<String> set;

    @Column
    public Uri uri;

    @Column
    public Date date;

    @Nullable
    @Column
    public List<String> nullableList;

    @Nullable
    @Column
    public Set<String> nullableSet;

    @Nullable
    @Column
    public Uri nullableUri;

    @Nullable
    @Column
    public Date nullableDate;

}

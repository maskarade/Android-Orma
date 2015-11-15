package com.github.gfx.android.orma.example.dbflow;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import android.support.annotation.Nullable;

@Table(databaseName = BenchmarkDatabase.NAME)
public class FlowTodo extends BaseModel {

    @PrimaryKey(autoincrement = true)
    @Column
    public long id;

    @Column
    public String title;

    @Column
    @Nullable
    public String content;

    @Column public long createdTimeMillis;
}

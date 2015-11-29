package com.github.gfx.android.orma.example.dbflow;

import com.raizlabs.android.dbflow.annotation.Database;

@Database(name = BenchmarkDatabase.NAME, version = BenchmarkDatabase.VERSION)
public class BenchmarkDatabase {

    public static final String NAME = "FlowBenchmark";

    public static final int VERSION = 1;
}

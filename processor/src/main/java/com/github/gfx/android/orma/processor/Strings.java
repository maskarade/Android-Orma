package com.github.gfx.android.orma.processor;

public class Strings {

    public static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static String toUpperFirst(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}

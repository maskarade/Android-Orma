# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# To make debug easier
-keepattributes SourceFile,LineNumberTable

# Retrolambda
-dontwarn java.lang.invoke.**

# RxJava
-dontwarn rx.internal.util.unsafe.**
-keep class rx.schedulers.Schedulers {
    public static <methods>;
}
-keep class rx.schedulers.ImmediateScheduler {
    public <methods>;
}
-keep class rx.schedulers.TestScheduler {
    public <methods>;
}
-keep class rx.schedulers.Schedulers {
    public static ** test();
}

# Android Support Library
-keep class android.support.** { *; }
-keep class android.databinding.** { *; }

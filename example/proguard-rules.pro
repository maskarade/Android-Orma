# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# To make debug easier
-keepattributes SourceFile,LineNumberTable

-dontwarn java.lang.instrument.**
-dontwarn java.lang.invoke.**

# Android Support Library
-keep class android.support.** { *; }
-keep class android.databinding.** { *; }

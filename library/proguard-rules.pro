# ProGuard configuration for Orma

# to use ParameterizedType
-keepattributes Signature

# antlr4 4.7 refers java.nio.file.* that doesn't exist on older Android
-dontwarn org.antlr.v4.runtime.CharStreams

# Can't find referenced class com.google.gson.Gson, but it is intended
-dontwarn com.github.gfx.android.orma.gson.**
-dontwarn com.github.gfx.android.orma.SingleAssociation

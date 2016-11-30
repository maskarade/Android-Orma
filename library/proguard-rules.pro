# ProGuard configuration for Orma

# to use ParameterizedType
-keepattributes Signature

# Can't find referenced class com.google.gson.Gson, but it is intended
-dontwarn com.github.gfx.android.orma.gson.**
-dontwarn com.github.gfx.android.orma.SingleAssociation

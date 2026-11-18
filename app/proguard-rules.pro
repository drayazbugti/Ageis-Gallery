# Add project specific ProGuard rules here.
-keep class com.example.data.model.** { *; }
-keep class com.example.data.local.** { *; }
-keep class com.example.security.** { *; }
-dontwarn com.google.devtools.ksp.**
-keepattributes *Annotation*

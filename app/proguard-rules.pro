-keep class com.mutindo.highwaytraficdodging.** { *; }
-keepclasseswithmembernames class * {
    native <methods>;
}
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-dontwarn com.android.**
-dontwarn androidx.**
-optimizationpasses 5
-verbose
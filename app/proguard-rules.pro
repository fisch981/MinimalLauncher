# Proguard / R8 rules for MinimalLauncher
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,SourceFile,LineNumberTable
-dontwarn javax.annotation.**

# Keep Android entry points & NotificationListenerService exact class names (used with ComponentName)
-keep class de.fisch981.minimallauncher.MinimalLauncherApp { *; }
-keep class de.fisch981.minimallauncher.MainActivity { *; }
-keep class de.fisch981.minimallauncher.data.service.LauncherNotificationService { *; }

# Keep ViewModels instantiated reflectively by ViewModelProvider.AndroidViewModelFactory
-keep class * extends androidx.lifecycle.AndroidViewModel {
    public <init>(android.app.Application);
}
-keep class * extends androidx.lifecycle.ViewModel {
    public <init>();
}

# Keep Enum methods for preference serialization & Kotlin entries
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep AndroidX DataStore Preferences protobuf fields accessed reflectively by MessageSchema
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}

# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html
#
# Room, Hilt, WorkManager and Firebase ship their own consumer rules.
# Firestore documents are mapped by hand (TaskFirestoreMapper), so no model classes need keeping.

# keep line numbers so stack traces from Play Console are readable after deobfuscation
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# strip debug and verbose logs from release builds
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
}

# Credential Manager loads its Google Play services provider by reflection
-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** {
  *;
}

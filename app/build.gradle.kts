import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

// Firebase config - downloaded from the Firebase console (not committed). Without it the app runs
// in local-only mode (no Google sign in and no backup), which is enough for local development
val hasFirebaseConfig = file("google-services.json").exists()
if (hasFirebaseConfig) {
    apply(plugin = libs.plugins.google.services.get().pluginId)
}

// release signing - read from keystore.properties (not committed) or from environment variables (CI)
val keystoreProperties = Properties().apply {
    val keystoreFile = rootProject.file("keystore.properties")
    if (keystoreFile.exists()) keystoreFile.inputStream().use { load(it) }
}
fun signingValue(key: String, envKey: String): String? =
    keystoreProperties.getProperty(key) ?: System.getenv(envKey)

val releaseStoreFile = signingValue("storeFile", "TASKFLOW_KEYSTORE_FILE")

android {
    namespace = "com.jel.taskflow"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.jel.taskflow"
        minSdk = 29
        targetSdk = 36
        // must be increased for every upload to Google Play
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (releaseStoreFile != null) {
            create("release") {
                storeFile = rootProject.file(releaseStoreFile)
                storePassword = signingValue("storePassword", "TASKFLOW_KEYSTORE_PASSWORD")
                keyAlias = signingValue("keyAlias", "TASKFLOW_KEY_ALIAS")
                keyPassword = signingValue("keyPassword", "TASKFLOW_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.findByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    // exported Room schemas are used by the migration tests
    sourceSets {
        getByName("androidTest").assets.directories.add("$projectDir/schemas")
    }
}

if (releaseStoreFile == null) {
    logger.warn("TaskFlow: no release keystore configured - release builds will be unsigned (see docs/PLAY_RELEASE.md)")
}

if (!hasFirebaseConfig) {
    logger.warn("TaskFlow: app/google-services.json is missing - debug builds run in local-only mode (see docs/PLAY_RELEASE.md)")
    // a release without Firebase would silently ship without sign in and backup
    tasks.named { it == "preReleaseBuild" }.configureEach {
        doFirst {
            throw GradleException("app/google-services.json is required for release builds (see docs/PLAY_RELEASE.md)")
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs = listOf("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.ui.graphics)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.common.jvm)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.kotlinx.coroutines.play.services)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

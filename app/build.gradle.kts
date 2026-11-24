import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val keystoreProps = Properties().apply {
    val propsFile = rootProject.file("keystore.properties")
    if (propsFile.isFile) {
        FileInputStream(propsFile).use { load(it) }
    }
}

val configuredStoreFileName = (keystoreProps.getProperty("storeFile") ?: System.getenv("KEYSTORE_FILE") ?: "").trim()
val configuredStoreFile = if (configuredStoreFileName.isNotEmpty()) {
    file(configuredStoreFileName).takeIf { it.isFile } ?: rootProject.file(configuredStoreFileName).takeIf { it.isFile }
} else {
    null
}
val configuredStorePassword = keystoreProps.getProperty("storePassword") ?: System.getenv("KEYSTORE_PASSWORD")
val configuredKeyAlias = keystoreProps.getProperty("keyAlias") ?: System.getenv("KEY_ALIAS")
val configuredKeyPassword = keystoreProps.getProperty("keyPassword") ?: System.getenv("KEY_PASSWORD")
val hasReleaseSigning = configuredStoreFile != null &&
    !configuredStorePassword.isNullOrBlank() &&
    !configuredKeyAlias.isNullOrBlank() &&
    !configuredKeyPassword.isNullOrBlank()

android {
    namespace = "de.fisch981.minimallauncher"
    compileSdk = 35

    defaultConfig {
        applicationId = "de.fisch981.minimallauncher"
        minSdk = 26
        targetSdk = 35
        versionCode = 37
        versionName = "1.0.36"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("shared") {
                storeFile = configuredStoreFile
                storePassword = configuredStorePassword
                keyAlias = configuredKeyAlias
                keyPassword = configuredKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("shared")
            }
        }
        release {
            isMinifyEnabled = false
            signingConfig = if (hasReleaseSigning) {
                signingConfigs.getByName("shared")
            } else {
                signingConfigs.getByName("debug")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    applicationVariants.all {
        if (buildType.name == "release") {
            outputs.all {
                val output = this as? com.android.build.gradle.internal.api.BaseVariantOutputImpl
                output?.outputFileName = "MinimalLauncher-release.apk"
            }
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")
    implementation("androidx.activity:activity-compose:1.9.2")

    // Jetpack Compose
    val composeBom = platform("androidx.compose:compose-bom:2024.09.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // DataStore Preferences
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Test
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.xerial:sqlite-jdbc:3.36.0.3")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

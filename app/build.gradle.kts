plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.bugarku.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.bugarku.app"
        minSdk = 21 // Android 5.0 ke atas
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    // Kunci tanda tangan tetap: update APK cukup ditimpa, riwayat & catatan tidak hilang
    signingConfigs {
        create("bugarku") {
            storeFile = file("bugarku.keystore")
            storePassword = "bugarku123"
            keyAlias = "bugarku"
            keyPassword = "bugarku123"
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
            signingConfig = signingConfigs.getByName("bugarku")
        }
        debug {
            signingConfig = signingConfigs.getByName("bugarku")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        // java.time (jadwal pengingat & tanggal) dipakai juga di Android 5-7
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.2")

    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
}

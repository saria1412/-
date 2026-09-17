plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.hbc.telegramgifcleaner"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.hbc.telegramgifcleaner"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    sourceSets["main"].jniLibs.srcDirs("src/main/jniLibs")
}

dependencies {
    // No analytics, ads, trackers, networking SDKs, or third-party runtime dependencies.
    // Telegram connectivity is provided only by the official TDLib JNI library copied at build time.
}

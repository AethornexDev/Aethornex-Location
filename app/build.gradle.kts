plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

dependencies {
    implementation("com.google.mediapipe:tasks-vision:1.1.1")
}

android {
    namespace = "com.aethornex.location"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.aethornex.location"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

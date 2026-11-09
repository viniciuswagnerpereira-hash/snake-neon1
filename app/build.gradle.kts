plugins {
    id("com.android.application")
}

android {
    namespace = "com.serpenteneon.game"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.serpenteneon.game"
        minSdk = 21
        targetSdk = 36
        versionCode = 2
        versionName = "1.0.1"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("androidx.webkit:webkit:1.14.0")
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "io.github.mhe1239.dnsswitcher"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.mhe1239.dnsswitcher"
        minSdk = 28 // Private DNS was added in Android 9
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    buildFeatures { compose = true }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    testImplementation("junit:junit:4.13.2")
}

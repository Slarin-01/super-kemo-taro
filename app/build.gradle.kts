plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.super_kemo_taro3"
    compileSdk = 37  // ← シンプルに数値だけ

    defaultConfig {
        applicationId = "com.example.super_kemo_taro3"
        minSdk = 24
        targetSdk = 37  // ← こちらも37に
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false  // ← ここも正しい書き方に
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
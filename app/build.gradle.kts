plugins {
    alias(libs.plugins.android.application)
    // AGP内蔵のKotlinに合わせたComposeコンパイラーで、@Composable関数をコンパイルする。
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10"
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
    // Android Studioとビルド処理でJetpack Composeを有効にする。
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation("androidx.activity:activity-compose:1.13.0")
    // BOMでComposeライブラリ同士のバージョンをそろえる（activity-composeは別途指定）。
    implementation(platform("androidx.compose:compose-bom:2025.08.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}

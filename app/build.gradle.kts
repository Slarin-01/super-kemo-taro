plugins {
    alias(libs.plugins.android.application)
    // AGP内蔵のKotlinに合わせたComposeコンパイラーで、@Composable関数をコンパイルする。
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10"
}

android {
    namespace = "com.example.super_kemo_taro3"
    // コンパイル時に参照するAndroid APIの版。対応端末の下限はminSdkで指定する。
    compileSdk = 37

    defaultConfig {
        // 端末がアプリと保存済みデータを識別するID。
        applicationId = "com.example.super_kemo_taro3"
        // Android 7.0以降の端末で利用できる。
        minSdk = 24
        // この版のAndroidの動作規則に合わせてアプリを実行する。
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // 現在はコードの縮小・難読化を行わず、そのままRelease版を作る。
            isMinifyEnabled = false
        }
    }
    // Javaコードで使用する言語機能と出力形式を11にそろえる。
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
    // @Previewの定義と、Debug版での画面表示確認に使うツール。
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    // 単体テストと、Android端末で実行するテストを別々に用意する。
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}

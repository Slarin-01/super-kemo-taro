# build.gradle.kts 仕様書

対象: [build.gradle.kts](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/build.gradle.kts)  
確認日: 2026-10-11

## 役割

appモジュールのAndroid設定、Composeコンパイラー、依存ライブラリ、テスト環境を定義する。バージョンカタログのlibs参照は親プロジェクトのgradle/libs.versions.tomlから供給される。

## プラグインとAndroid設定

| 項目 | 現在値 |
| --- | --- |
| Androidプラグイン | alias(libs.plugins.android.application) |
| Composeコンパイラープラグイン | org.jetbrains.kotlin.plugin.compose / 2.2.10 |
| namespace / applicationId | com.example.super_kemo_taro3 |
| compileSdk / targetSdk | 37 / 37 |
| minSdk | 24 |
| versionCode / versionName | 1 / 1.0 |
| Java sourceCompatibility / targetCompatibility | JavaVersion.VERSION_11 |
| buildFeatures.compose | true |
| release.isMinifyEnabled | false |
| testInstrumentationRunner | androidx.test.runner.AndroidJUnitRunner |

## 依存ライブラリ

| 定義 | 用途 |
| --- | --- |
| libs.androidx.activity.ktx / appcompat / core.ktx | Activity・AppCompat・Android拡張 |
| androidx.activity:activity-compose:1.13.0 | ActivityのsetContent |
| androidx.compose:compose-bom:2025.08.01 | Composeライブラリのバージョン整合 |
| androidx.compose.ui:ui | Compose UI基盤 |
| androidx.compose.material3:material3 | Material 3のボタン・ダイアログ・時刻入力など |
| androidx.compose.ui:ui-tooling-preview | Composeプレビュー関連 |
| debugのui-tooling | 開発時のComposeツール |
| libs.material | XMLで指定するMaterial系アプリテーマ |
| libs.junit | ローカル単体テスト |
| libs.androidx.espresso.core / androidx.junit | Android端末上のテスト |

activity-composeとComposeコンパイラープラグインのバージョンはBOM管理対象として宣言していない。foundationやruntimeはこのファイルに個別のimplementation宣言がなく、Compose依存関係を通じて解決される。

## ビルド確認

プロジェクトルートのGradle Wrapperで :app:assembleDebug、:app:assembleRelease、:app:testDebugUnitTest、:app:lintDebug を使用する。 :app:compileDebugAndroidTestKotlin は端末テストコードのコンパイル確認で、端末上のテスト実行とは別。

ライブラリやプラグインの設定変更後はAndroid StudioのGradle同期でIDEへ反映する。releaseの縮小は無効。Javaのコンパイル対象11と、Gradle起動に使用するJDKのバージョンは別の設定である。


# SuperKemoTaroApp.kt 仕様書

対象: [src/main/java/com/example/super_kemo_taro3/SuperKemoTaroApp.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/SuperKemoTaroApp.kt)  
確認日: 2026-10-11  
パッケージ: `com.example.super_kemo_taro3`

## 役割

Applicationを継承し、Activity表示前に前回の明暗モードを復元する。AndroidManifest.xmlのapplication android:nameに登録される。

## onCreateの流れ

1. super.onCreateを呼ぶ。
2. theme_prefsのSharedPreferencesを開く。
3. is_dark_modeキーが存在するときだけBooleanを読む。
4. trueならAppCompatDelegate.MODE_NIGHT_YES、falseならMODE_NIGHT_NOをsetDefaultNightModeへ渡す。

## 入出力と依存

読むデータはtheme_prefs / is_dark_modeのみ。設定を書き込むのはMainActivity.toggleTheme。保存キーがない初回起動では、ここから明暗設定を上書きしない。Compose側の配色はAppUi.ktのKemoThemeが現在の設定に合わせる。

画面表示、アラーム登録、通知フィルター変更は行わない。このファイルにActivity遷移やUI状態はない。


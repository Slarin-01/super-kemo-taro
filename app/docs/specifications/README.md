# ファイル別仕様書

確認日: 2026-10-11  
対象モジュール: app / アプリ名: 言十ベル

## この仕様書について

現在のコードを基に、ファイルごとの役割、入出力、状態、保存データ、処理の流れ、依存関係、境界条件を整理した。将来の設計案ではなく、確認日時点の実装仕様を記載する。

対象はapp直下のbuild.gradle.kts・.gitignoreとsrc内の全45ファイル。Kotlin・テスト・Manifest・保持ルール・XMLリソース・画像を含む。生成物のbuild・release、親プロジェクトの設定、IDEやGradleのキャッシュ、仕様書自体はファイル別対象から除外する。

画像も1ファイルずつ仕様書を用意する。画面UIのレイアウトはComposeで記述し、XMLリソースはテーマやアイコンなどのために残っている。

## 全体を読む資料

- [共通データ・処理仕様](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/common-data.md) — 画面の担当、保存キー、曜日・時限、アラームの受け渡し、一時停止。
- [現実装の注意点](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/implementation-notes.md) — FRIを日曜日に動かすテスト設定と反映手順、5限の初期値の差異、権限・日時・テストの制約。

## 読み始める順序

ホームはMainActivity.ktとAppUi.kt、曜日のミュート設定はTimetableActivity.kt、時刻設定はMainActivity2.kt・TimeSettings.kt・TimeSettingsScreen.kt、実際の通知切替はDndReceiver.kt、再起動復元はBootReceiver.ktを読む。

## 画面・共通UI・通知処理

| 対象ファイル | 主な役割 | 仕様書 |
| --- | --- | --- |
| [src/main/java/com/example/super_kemo_taro3/AppUi.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/AppUi.kt) | 共通テーマ・ホーム・時間割・確認ダイアログ | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/AppUi.kt.md) |
| [src/main/java/com/example/super_kemo_taro3/BootReceiver.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/BootReceiver.kt) | 端末再起動後のアラーム復元 | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/BootReceiver.kt.md) |
| [src/main/java/com/example/super_kemo_taro3/DndReceiver.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/DndReceiver.kt) | アラーム受信・通知フィルター変更・翌週予約 | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/DndReceiver.kt.md) |
| [src/main/java/com/example/super_kemo_taro3/editmode.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/editmode.kt) | 未使用の空クラス | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/editmode.kt.md) |
| [src/main/java/com/example/super_kemo_taro3/MainActivity.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/MainActivity.kt) | ホーム画面・一時停止・テーマ・権限確認 | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/MainActivity.kt.md) |
| [src/main/java/com/example/super_kemo_taro3/MainActivity2.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/MainActivity2.kt) | 時限カスタムの編集状態・保存・アラーム更新 | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/MainActivity2.kt.md) |
| [src/main/java/com/example/super_kemo_taro3/MainActivity3.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/MainActivity3.kt) | メニューの遷移・クレジット本文 | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/MainActivity3.kt.md) |
| [src/main/java/com/example/super_kemo_taro3/notificationtest.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/notificationtest.kt) | 通知フィルターの手動切替Activity | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/notificationtest.kt.md) |
| [src/main/java/com/example/super_kemo_taro3/SecondaryScreens.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/SecondaryScreens.kt) | メニュー・クレジット・通知テストのCompose UI | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/SecondaryScreens.kt.md) |
| [src/main/java/com/example/super_kemo_taro3/SuperKemoTaroApp.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/SuperKemoTaroApp.kt) | アプリ起動時のテーマ復元 | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/SuperKemoTaroApp.kt.md) |
| [src/main/java/com/example/super_kemo_taro3/TimeSettings.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/TimeSettings.kt) | 時刻の定義・整形・検証 | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/TimeSettings.kt.md) |
| [src/main/java/com/example/super_kemo_taro3/TimeSettingsScreen.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/TimeSettingsScreen.kt) | 時限カスタムのCompose画面と時刻選択 | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/TimeSettingsScreen.kt.md) |
| [src/main/java/com/example/super_kemo_taro3/TimetableActivity.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/TimetableActivity.kt) | ミュートする曜日・時限の選択と予約 | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/TimetableActivity.kt.md) |

## テスト

| 対象ファイル | 主な役割 | 仕様書 |
| --- | --- | --- |
| [src/androidTest/java/com/example/super_kemo_taro3/ExampleInstrumentedTest.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/androidTest/java/com/example/super_kemo_taro3/ExampleInstrumentedTest.kt) | 端末上でのパッケージ確認テスト | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/ExampleInstrumentedTest.kt.md) |
| [src/test/java/com/example/super_kemo_taro3/ExampleUnitTest.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/test/java/com/example/super_kemo_taro3/ExampleUnitTest.kt) | 雛形の単体テスト | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/ExampleUnitTest.kt.md) |
| [src/test/java/com/example/super_kemo_taro3/TimeSettingsTest.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/test/java/com/example/super_kemo_taro3/TimeSettingsTest.kt) | 時刻定義・入力検証の単体テスト | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/TimeSettingsTest.kt.md) |

## ビルド・アプリ登録・管理設定

| 対象ファイル | 主な役割 | 仕様書 |
| --- | --- | --- |
| [.gitignore](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/.gitignore) | ビルド出力のGit除外 | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/gitignore.md) |
| [build.gradle.kts](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/build.gradle.kts) | appモジュールのビルド・Compose依存設定 | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/build.gradle.kts.md) |
| [src/main/AndroidManifest.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/AndroidManifest.xml) | 権限・Application・Activity・Receiverの登録 | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/AndroidManifest.xml.md) |
| [src/main/keepRules/rules.keep](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/keepRules/rules.keep) | R8保持ルールの雛形 | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/rules.keep.md) |

## テーマ・文字列・XMLリソース

| 対象ファイル | 主な役割 | 仕様書 |
| --- | --- | --- |
| [src/main/res/drawable/ic_launcher_background.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/drawable/ic_launcher_background.xml) | アダプティブアイコンの背景ベクター | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/resources/drawable/ic_launcher_background.xml.md) |
| [src/main/res/drawable/ic_launcher_foreground.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/drawable/ic_launcher_foreground.xml) | 前景ベクターの残存リソース | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/resources/drawable/ic_launcher_foreground.xml.md) |
| [src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml) | 丸型のアダプティブアイコン | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/resources/mipmap-anydpi-v26/ic_launcher_round.xml.md) |
| [src/main/res/mipmap-anydpi-v26/ic_launcher.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml) | 通常のアダプティブアイコン | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/resources/mipmap-anydpi-v26/ic_launcher.xml.md) |
| [src/main/res/values-night/colors.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/values-night/colors.xml) | 夜間表示用の色上書き | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/resources/values-night/colors.xml.md) |
| [src/main/res/values-night/themes.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/values-night/themes.xml) | Activityの夜間テーマ | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/resources/values-night/themes.xml.md) |
| [src/main/res/values/colors.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/values/colors.xml) | 通常表示用の色リソース | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/resources/values/colors.xml.md) |
| [src/main/res/values/strings.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/values/strings.xml) | アプリ表示名 | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/resources/values/strings.xml.md) |
| [src/main/res/values/themes.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/values/themes.xml) | Activityの基本テーマ | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/resources/values/themes.xml.md) |
| [src/main/res/xml/backup_rules.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/xml/backup_rules.xml) | バックアップルールの雛形 | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/resources/xml/backup_rules.xml.md) |
| [src/main/res/xml/data_extraction_rules.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/xml/data_extraction_rules.xml) | データ抽出・移行ルールの雛形 | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/resources/xml/data_extraction_rules.xml.md) |

## 画像素材（ファイル別）

| 対象ファイル | 主な役割 | 仕様書 |
| --- | --- | --- |
| [src/main/ic_launcher-playstore.png](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/ic_launcher-playstore.png) | 配布用のアイコン画像 | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/assets/ic_launcher-playstore.png.md) |
| [src/main/res/mipmap-hdpi/ic_launcher_foreground.webp](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/mipmap-hdpi/ic_launcher_foreground.webp) | hdpi向けのアイコン前景 | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/assets/mipmap-hdpi/ic_launcher_foreground.webp.md) |
| [src/main/res/mipmap-hdpi/ic_launcher_round.webp](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/mipmap-hdpi/ic_launcher_round.webp) | hdpi向けの丸型アイコン | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/assets/mipmap-hdpi/ic_launcher_round.webp.md) |
| [src/main/res/mipmap-hdpi/ic_launcher.webp](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/mipmap-hdpi/ic_launcher.webp) | hdpi向けの通常アイコン | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/assets/mipmap-hdpi/ic_launcher.webp.md) |
| [src/main/res/mipmap-mdpi/ic_launcher_foreground.webp](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/mipmap-mdpi/ic_launcher_foreground.webp) | mdpi向けのアイコン前景 | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/assets/mipmap-mdpi/ic_launcher_foreground.webp.md) |
| [src/main/res/mipmap-mdpi/ic_launcher_round.webp](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/mipmap-mdpi/ic_launcher_round.webp) | mdpi向けの丸型アイコン | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/assets/mipmap-mdpi/ic_launcher_round.webp.md) |
| [src/main/res/mipmap-mdpi/ic_launcher.webp](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/mipmap-mdpi/ic_launcher.webp) | mdpi向けの通常アイコン | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/assets/mipmap-mdpi/ic_launcher.webp.md) |
| [src/main/res/mipmap-xhdpi/ic_launcher_foreground.webp](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/mipmap-xhdpi/ic_launcher_foreground.webp) | xhdpi向けのアイコン前景 | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/assets/mipmap-xhdpi/ic_launcher_foreground.webp.md) |
| [src/main/res/mipmap-xhdpi/ic_launcher_round.webp](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/mipmap-xhdpi/ic_launcher_round.webp) | xhdpi向けの丸型アイコン | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/assets/mipmap-xhdpi/ic_launcher_round.webp.md) |
| [src/main/res/mipmap-xhdpi/ic_launcher.webp](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/mipmap-xhdpi/ic_launcher.webp) | xhdpi向けの通常アイコン | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/assets/mipmap-xhdpi/ic_launcher.webp.md) |
| [src/main/res/mipmap-xxhdpi/ic_launcher_foreground.webp](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/mipmap-xxhdpi/ic_launcher_foreground.webp) | xxhdpi向けのアイコン前景 | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/assets/mipmap-xxhdpi/ic_launcher_foreground.webp.md) |
| [src/main/res/mipmap-xxhdpi/ic_launcher_round.webp](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/mipmap-xxhdpi/ic_launcher_round.webp) | xxhdpi向けの丸型アイコン | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/assets/mipmap-xxhdpi/ic_launcher_round.webp.md) |
| [src/main/res/mipmap-xxhdpi/ic_launcher.webp](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/mipmap-xxhdpi/ic_launcher.webp) | xxhdpi向けの通常アイコン | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/assets/mipmap-xxhdpi/ic_launcher.webp.md) |
| [src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.webp](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.webp) | xxxhdpi向けのアイコン前景 | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/assets/mipmap-xxxhdpi/ic_launcher_foreground.webp.md) |
| [src/main/res/mipmap-xxxhdpi/ic_launcher_round.webp](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.webp) | xxxhdpi向けの丸型アイコン | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/assets/mipmap-xxxhdpi/ic_launcher_round.webp.md) |
| [src/main/res/mipmap-xxxhdpi/ic_launcher.webp](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp) | xxxhdpi向けの通常アイコン | [読む](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/assets/mipmap-xxxhdpi/ic_launcher.webp.md) |

## 更新と検証

ソース変更時は対応する仕様書と共通データ資料も更新する。特にSharedPreferencesのキー、曜日対応、初期時刻、PendingIntentの識別方法は複数ファイルに影響する。

初回作成はMarkdown仕様書のみの追加。現在はテスト用にFRIの内部曜日を日曜日へ統一し、対応する仕様書を更新している。ビルド設定・画面表示・保存キーは変更していない。アプリのコンパイル結果と、端末での通知・アラームの実動作の確認は区別する。

2026-10-11のテスト用変更後、:app:assembleDebug・:app:assembleRelease・:app:testDebugUnitTestを実行し、BUILD SUCCESSFULを確認した。単体テストは7件成功、失敗・エラーは0件。端末での日曜日のアラーム実発火は未確認。


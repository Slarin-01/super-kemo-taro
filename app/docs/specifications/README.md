# appフォルダーの仕様書

更新日: 2026-10-13

「言十ベル」は、選んだ曜日・授業時刻に通知をミュートし、授業終了時に解除するアプリです。

対象は、プロジェクトのapp内にあるコード・設定・テスト・画像です。自動生成されるbuild・releaseの中身は個別の説明から除きます。

## 最初に読むもの

1. [全体の仕組み：5つの担当と処理の流れ](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/architecture.md)
2. [アプリの動作：曜日・時刻・一時停止・保存](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/common-data.md)

各ファイルでは、役割、主要なクラス・関数の機能、扱う情報と結果、つながり、変更する場所を説明しています。クラスは処理をまとめる単位、関数はそこで行う操作です。下のファイル名を押すと、その仕様書を開けます。

## 変更する場所の早見表

| 変えたいこと | 読む仕様書 |
| --- | --- |
| ホーム画面のボタンや文章 | [HomePage.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/frontend/screens/HomePage.kt.md) |
| 時刻の入力画面 | [LessonTimeEditorPage.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/frontend/screens/LessonTimeEditorPage.kt.md) |
| 金曜日を動かす曜日 | [TimetableScheduling.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/backend/service/TimetableScheduling.kt.md) |
| 授業の初期時刻 | [LessonTimeSettings.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/backend/service/LessonTimeSettings.kt.md) |
| 一時停止の動き | [NotificationAutomation.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/backend/service/NotificationAutomation.kt.md) |
| 保存する設定 | [AppSettings.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/backend/repository/AppSettings.kt.md) |
| クレジットの文章 | [MenuState.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/frontend/viewmodel/MenuState.kt.md) |

## フロントエンド・画面

画面を表示する部分です。

| ファイル | 役割 |
| --- | --- |
| [HomePage.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/frontend/screens/HomePage.kt.md) | ホーム画面 |
| [LessonTimeEditorPage.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/frontend/screens/LessonTimeEditorPage.kt.md) | 授業時刻の編集画面 |
| [NotificationTestPage.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/frontend/screens/NotificationTestPage.kt.md) | 通知の切り替えテスト画面 |
| [SettingsMenuPage.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/frontend/screens/SettingsMenuPage.kt.md) | 設定メニュー |
| [SharedScreenComponents.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/frontend/screens/SharedScreenComponents.kt.md) | 画面で共通して使う部品 |
| [TimetableEditorPage.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/frontend/screens/TimetableEditorPage.kt.md) | 曜日・時限の選択画面 |

## フロントエンド・状態管理

入力や選択を覚え、画面の操作を処理へ渡す部分です。

| ファイル | 役割 |
| --- | --- |
| [LessonTimeEditorState.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/frontend/viewmodel/LessonTimeEditorState.kt.md) | 時刻編集の表示状態 |
| [MenuState.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/frontend/viewmodel/MenuState.kt.md) | クレジットの表示状態 |
| [NotificationTestState.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/frontend/viewmodel/NotificationTestState.kt.md) | 通知テストの表示状態 |
| [ScreenStateFactory.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/frontend/viewmodel/ScreenStateFactory.kt.md) | 画面の状態管理を用意する部分 |
| [TimetableState.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/frontend/viewmodel/TimetableState.kt.md) | 時間割とホームの表示状態 |

## バックエンド・設定管理

選択枠、時刻、表示モードなどを保存する部分です。

| ファイル | 役割 |
| --- | --- |
| [AppSettings.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/backend/repository/AppSettings.kt.md) | 設定の保存と読み込み |

## バックエンド・処理

通知の切り替えや時刻予約を行う部分です。

| ファイル | 役割 |
| --- | --- |
| [AlarmScheduling.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/backend/service/AlarmScheduling.kt.md) | Androidへの時刻予約 |
| [LessonTimeSettings.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/backend/service/LessonTimeSettings.kt.md) | 授業時刻のルールと保存 |
| [NotificationAutomation.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/backend/service/NotificationAutomation.kt.md) | 一時停止と自動切り替え |
| [NotificationControl.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/backend/service/NotificationControl.kt.md) | 通知のミュートと解除 |
| [TimetableScheduling.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/backend/service/TimetableScheduling.kt.md) | 曜日・時限と予約のまとめ役 |

## バックエンド・受信処理

Androidからの知らせを受け取る部分です。

| ファイル | 役割 |
| --- | --- |
| [AlarmReceivers.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/backend/receiver/AlarmReceivers.kt.md) | Androidからの知らせを受け取る部分 |

## 起動時の準備

アプリが使う処理を準備する共通部分です。

| ファイル | 役割 |
| --- | --- |
| [ApplicationSetup.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/app/ApplicationSetup.kt.md) | アプリ起動の準備 |

## テスト

機能が予定どおり動くか確認する部分です。

| ファイル | 役割 |
| --- | --- |
| [ApplicationStartupTest.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/ANDROIDtests/ApplicationStartupTest.kt.md) | 端末上での起動確認テスト |
| [BackendTestDoubles.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/tests/backend/service/BackendTestDoubles.kt.md) | テスト用の代役 |
| [BackendWorkflowTest.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/tests/backend/service/BackendWorkflowTest.kt.md) | 保存と通知予約のテスト |
| [LessonTimeRulesTest.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/tests/backend/service/LessonTimeRulesTest.kt.md) | 授業時刻の入力テスト |
| [ScheduleCalculatorTest.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/tests/backend/service/ScheduleCalculatorTest.kt.md) | 予約日時の計算テスト |
| [LessonTimeEditorViewModelTest.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/tests/frontend/viewmodel/LessonTimeEditorViewModelTest.kt.md) | 時刻編集の状態テスト |

## アプリを作る・起動するための設定

アプリの作成、画面の登録、必要な権限などの設定です。

| ファイル | 役割 |
| --- | --- |
| [.gitignore](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/gitignore.md) | 変更管理から外すファイル |
| [build.gradle.kts](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/build.gradle.kts.md) | アプリを組み立てる設定 |
| [AndroidManifest.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/AndroidManifest.xml.md) | 画面・権限・受信処理の登録 |
| [rules.keep](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/rules.keep.md) | コードを圧縮するときの設定 |

## 画像・色・表示名

アプリ名、色、基本テーマ、アイコンなどです。

| ファイル | 役割 |
| --- | --- |
| [ic_launcher-playstore.png](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/assets/ic_launcher-playstore.png.md) | 配布用のアイコン画像 |
| [drawable/ic_launcher_background.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/resources/drawable/ic_launcher_background.xml.md) | アイコンの背景 |
| [drawable/ic_launcher_foreground.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/resources/drawable/ic_launcher_foreground.xml.md) | 以前のアイコン前景 |
| [mipmap-anydpi-v26/ic_launcher_round.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/resources/mipmap-anydpi-v26/ic_launcher_round.xml.md) | 丸型アイコンの組み合わせ |
| [mipmap-anydpi-v26/ic_launcher.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/resources/mipmap-anydpi-v26/ic_launcher.xml.md) | 通常アイコンの組み合わせ |
| [mipmap-hdpi/ic_launcher_foreground.webp](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/assets/mipmap-hdpi/ic_launcher_foreground.webp.md) | アイコンの手前に描く画像（mipmap-hdpi） |
| [mipmap-hdpi/ic_launcher_round.webp](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/assets/mipmap-hdpi/ic_launcher_round.webp.md) | 丸型のアイコン（mipmap-hdpi） |
| [mipmap-hdpi/ic_launcher.webp](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/assets/mipmap-hdpi/ic_launcher.webp.md) | 通常のアイコン（mipmap-hdpi） |
| [mipmap-mdpi/ic_launcher_foreground.webp](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/assets/mipmap-mdpi/ic_launcher_foreground.webp.md) | アイコンの手前に描く画像（mipmap-mdpi） |
| [mipmap-mdpi/ic_launcher_round.webp](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/assets/mipmap-mdpi/ic_launcher_round.webp.md) | 丸型のアイコン（mipmap-mdpi） |
| [mipmap-mdpi/ic_launcher.webp](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/assets/mipmap-mdpi/ic_launcher.webp.md) | 通常のアイコン（mipmap-mdpi） |
| [mipmap-xhdpi/ic_launcher_foreground.webp](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/assets/mipmap-xhdpi/ic_launcher_foreground.webp.md) | アイコンの手前に描く画像（mipmap-xhdpi） |
| [mipmap-xhdpi/ic_launcher_round.webp](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/assets/mipmap-xhdpi/ic_launcher_round.webp.md) | 丸型のアイコン（mipmap-xhdpi） |
| [mipmap-xhdpi/ic_launcher.webp](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/assets/mipmap-xhdpi/ic_launcher.webp.md) | 通常のアイコン（mipmap-xhdpi） |
| [mipmap-xxhdpi/ic_launcher_foreground.webp](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/assets/mipmap-xxhdpi/ic_launcher_foreground.webp.md) | アイコンの手前に描く画像（mipmap-xxhdpi） |
| [mipmap-xxhdpi/ic_launcher_round.webp](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/assets/mipmap-xxhdpi/ic_launcher_round.webp.md) | 丸型のアイコン（mipmap-xxhdpi） |
| [mipmap-xxhdpi/ic_launcher.webp](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/assets/mipmap-xxhdpi/ic_launcher.webp.md) | 通常のアイコン（mipmap-xxhdpi） |
| [mipmap-xxxhdpi/ic_launcher_foreground.webp](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/assets/mipmap-xxxhdpi/ic_launcher_foreground.webp.md) | アイコンの手前に描く画像（mipmap-xxxhdpi） |
| [mipmap-xxxhdpi/ic_launcher_round.webp](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/assets/mipmap-xxxhdpi/ic_launcher_round.webp.md) | 丸型のアイコン（mipmap-xxxhdpi） |
| [mipmap-xxxhdpi/ic_launcher.webp](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/assets/mipmap-xxxhdpi/ic_launcher.webp.md) | 通常のアイコン（mipmap-xxxhdpi） |
| [values-night/colors.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/resources/values-night/colors.xml.md) | 暗い表示の色 |
| [values-night/themes.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/resources/values-night/themes.xml.md) | 暗い表示の基本テーマ |
| [values/colors.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/resources/values/colors.xml.md) | 通常表示の色 |
| [values/strings.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/resources/values/strings.xml.md) | アプリ名 |
| [values/themes.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/resources/values/themes.xml.md) | 画面起動時の基本テーマ |
| [xml/backup_rules.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/resources/xml/backup_rules.xml.md) | バックアップ設定のひな形 |
| [xml/data_extraction_rules.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/resources/xml/data_extraction_rules.xml.md) | データ移行設定のひな形 |

## コードのコメントについて

ファイル冒頭で担当を示し、処理のまとまりや、保存・復元・許可不足などの判断理由をコメントで説明しています。単純な代入の説明は増やさず、各ファイルの仕様書と対応させています。

## 補足

本体のコードは19ファイルです。5つの担当区分に18ファイル、起動準備に1ファイルがあります。テストや画像を含め、56ファイルを個別に説明しています。

- [現在の注意点](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/implementation-notes.md)
- [構成を整理した記録](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/migration.md)
- [確認済みのこと](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/verification.md)


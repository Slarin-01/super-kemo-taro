# SecondaryScreens.kt 仕様書

対象: [src/main/java/com/example/super_kemo_taro3/SecondaryScreens.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/SecondaryScreens.kt)  
確認日: 2026-10-11  
パッケージ: `com.example.super_kemo_taro3`

## 役割

メニューと通知モード手動テストの画面を描画する。Activityの起動やNotificationManagerの操作は行わず、コールバックへ委譲する。

## MenuScreen

引数はcreditMessage:Stringと、onMain・onTimetable・onTimeSettings・onNotificationTestの4個の操作コールバック。

| ボタン表示 | 動作 |
| --- | --- |
| 時間割を見る | onMainを呼ぶ |
| 時刻設定 | onTimetableを呼ぶ |
| 時限カスタム | onTimeSettingsを呼ぶ |
| 通知切り替えテスト | onNotificationTestを呼ぶ |
| クレジット | showCreditsをtrueにする |

showCreditsはrememberSaveableで保持し、Activity再作成時にも開閉状態を復元する。クレジットはAppUi.ktのConfirmationDialogを使い、title「クレジット」、confirmText「閉じる」、showCancel=falseで表示する。確定・dismissのどちらもshowCreditsをfalseにする。

縦スクロール可能なColumnで余白24dp、間隔16dp。遷移先はMainActivity3が決める。

## NotificationTestScreen

引数はstatus:String、onToggle:() -> Unit、onBack:() -> Unit。「通知切り替えテスト」のタイトル、「通知モード 切替」ボタン、現在のstatus、「メニューに戻る」を表示する。

切替ボタンはonToggle、戻るボタンはonBackを呼ぶ。statusの意味・更新・保存はこのUIでは決めない。画面は縦スクロール、余白24dp、間隔24dp、中央配置。NotificationTestActivityから呼び出される。

## 永続データ

このファイルのSharedPreferencesへのアクセスはない。クレジット表示状態は画面復元用の一時状態であり、アプリ設定として保存しない。


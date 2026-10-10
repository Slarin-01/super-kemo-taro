# AppUi.kt 仕様書

対象: [src/main/java/com/example/super_kemo_taro3/AppUi.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/AppUi.kt)  
確認日: 2026-10-11  
パッケージ: `com.example.super_kemo_taro3`

## 役割

各Activityが使う共通のCompose UIを定義する。保存処理や端末APIの操作は呼び出し側へ委譲する。

## 関数と契約

| 関数 | 入力・表示 |
| --- | --- |
| KemoTheme(content) | 現在の明暗設定に応じてMaterialThemeのlightColorScheme / darkColorSchemeを適用 |
| AppScreen(content) | 画面全体のSurfaceとBox。safeDrawingPaddingとimePaddingでシステムバー・キーボードの領域に配慮 |
| TimetableGrid(blockedSlots, onSlotClick=null) | 月〜金、1〜8限の40枠を表示。コールバック省略時は閲覧専用 |
| MainScreen(blockedSlots, paused, onTogglePause, onToggleTheme, onEdit) | 停止・テーマ・編集ボタン、停止状態、閲覧用時間割 |
| TimetableScreen(blockedSlots, onSlotClick, onBack) | 編集可能な時間割と「メニューに戻る」 |
| ConfirmationDialog(title="確認", message, confirmText, onConfirm, onDismiss, showCancel=true) | スクロール可能な本文、確定ボタン、任意のキャンセルボタン |

## 時間割表示

曜日キーはMON・TUE・WED・THU・FRI、表示は月・火・水・木・金。セルの保存キーは曜日キーと時限をアンダースコアで結合する（例: MON_1）。

テスト用の内部対応ではFRIのアラームを日曜日に登録するが、この画面の金曜日表示と保存キーは変更しない。曜日変換はTimetableActivity・MainActivity2・BootReceiverが担当する。

blockedSlotsに含まれるセルをColor.Green、その他をColor.LightGrayで表示する。セル文字はColor.Black。列は等幅、セル高さは最低48dp、セル間は4dp。操作可能な場合はRole.Buttonを設定する。読み上げ用のcontentDescriptionとselectedも設定する。

## ホームと編集画面

ホームの一時停止ボタン文言は「日曜日まで機能を停止」。状態表示は「日曜日まで停止: ON/OFF」で、ONをテーマのerror色で示す。テーマボタンは現在の明暗に応じて「ホワイトモードへ」「ダークモードへ」を表示する。

ホームは16dp、編集画面は8dpの余白で、どちらも縦スクロールできる。MainScreenの時間割にはクリック処理を渡さない。

## 状態・副作用

時間割・停止状態の所有者はActivity。UIは渡された値を表示し、操作時にコールバックを呼ぶ。ConfirmationDialogで確定後に閉じる処理はonConfirmの呼び出し元が行う。showCancel=falseでもonDismissRequestはonDismissへ通知する。

Compose配色は本ファイルのMaterialThemeから決まり、res/values/colors.xmlのbutton_strokeは参照しない。固定のセル色は明暗モードでも同じ。


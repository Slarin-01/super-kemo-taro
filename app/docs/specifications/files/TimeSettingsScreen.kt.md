# TimeSettingsScreen.kt 仕様書

対象: [src/main/java/com/example/super_kemo_taro3/TimeSettingsScreen.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/TimeSettingsScreen.kt)  
確認日: 2026-10-11  
パッケージ: `com.example.super_kemo_taro3`

## 役割と入力契約

1〜8限の開始・終了時刻を表示・編集するCompose UI。timesは16要素を前提とし、保存・初期化・画面遷移はMainActivity2のコールバックへ委譲する。このファイルでは永続保存やアラーム登録を行わない。

| 引数 | 内容 |
| --- | --- |
| times: List<String> | 開始・終了を交互に並べた表示値 |
| onTimeChange: (Int, String) -> Unit | 入力欄の添字と確定時刻を通知 |
| onSave / onReset / onDiscard | 確認後の保存・初期化・破棄を通知 |

## 関数

| 関数 | 処理 |
| --- | --- |
| TimeSettingsScreen | 8行の時刻欄、エラー説明、保存・初期化・破棄ボタンと確認ダイアログを描画 |
| TimeField（private） | 時刻選択を開くOutlinedButton。エラー時は赤字と「!」を付ける |
| ComposeTimeDialog（private） | 24時間制のTimePickerとTimeInputを切り替える時刻選択ダイアログ |

## 画面状態

selectedIndexはrememberSaveableのIntで、−1がダイアログなし、0〜15が編集中の欄。confirmationはrememberSaveableのnullable Stringで、save・reset・discard・errorを区別する。errorsはinvalidTimeIndices(times)で描画時に再計算する。

key(selectedIndex)で時刻選択の状態を欄ごとに分ける。ComposeTimeDialogのkeyboardInputはrememberSaveableのBoolean。時刻はrememberTimePickerStateに保持する。

## 操作

| 操作 | 確認後の動作 |
| --- | --- |
| 時刻欄をタップ | 対象値で時刻選択を開く。不正値ならTimeSettingの初期値を使用 |
| 時刻選択のOK | formatTimeの結果をonTimeChangeへ渡して閉じる |
| 時刻選択のキャンセル・dismiss | timesを変更せず閉じる |
| 保存して戻る | エラーなしなら確認後onSave。エラーありなら入力エラーダイアログ |
| 初期化 | 確認後onReset。永続保存は別途「保存して戻る」が必要 |
| 保存せず戻る | 確認後onDiscard |

UIは縦スクロール、余白16dp、時刻行の間隔8dp。共通ダイアログはAppUi.ktを使用する。エラーがある場合も保存ボタンは押せるが、保存コールバックへは進まない。TimePicker関連はExperimentalMaterial3ApiをOptInして使用する。16要素未満のtimesは添字アクセスで失敗し得るため、呼び出し側で要素数を保証する。


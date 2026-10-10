# MainActivity.kt 仕様書

対象: [src/main/java/com/example/super_kemo_taro3/MainActivity.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/MainActivity.kt)  
確認日: 2026-10-11  
パッケージ: `com.example.super_kemo_taro3`

## 役割

アプリの起動入口となるホーム画面を管理する。時間割を閲覧し、日曜日までの一時停止、明暗モードの変更、編集メニューへの遷移を行う。AppCompatActivityを継承し、画面はAppUi.ktのAppScreenとMainScreenで表示する。

## 状態と保存データ

| 名前 | 型・保存先 | 内容 |
| --- | --- | --- |
| blockedSlots | ComposeのSet<String> | 選択された曜日・時限の集合 |
| paused | ComposeのBoolean | 現在時刻が一時停止期限より前か |
| blocked_slots | timetable_settings / StringSet | 読み込みのみ。空集合が既定値 |
| pause_until_sunday_time | timetable_settings / Long | 停止期限。未設定は0 |
| is_dark_mode | theme_prefs / Boolean | 選択した明暗モード |

## メソッド

| メソッド | 処理 |
| --- | --- |
| onCreate | edge-to-edgeを有効にし、保存設定の読み込み、Compose画面の設定、authchkを実行 |
| onResume | refreshSettingsで保存内容を再読み込み |
| refreshSettings | blocked_slotsを別の集合にコピーし、pausedを更新 |
| toggleTheme | 現在の明暗モードを反転して保存し、AppCompatDelegateへ適用 |
| getNextSundayEndTime | 直近の日曜日の23:59:59.999を端末のCalendarで計算。当日が日曜日なら当日末 |
| isPausedUntilSunday | 現在時刻 < 保存された停止期限を返す |
| togglePauseUntilSunday | 停止中なら期限キーを削除。停止していなければ期限を保存し、権限がある場合は通知フィルターをALLへ変更 |
| authchk | 通知モード変更権限と、API 31以上で正確なアラームの利用可否を確認。不足時は対応する端末設定を開く |

## 操作と画面遷移

一時停止ボタンは停止状態を切り替えて表示を更新する。テーマボタンは明暗を切り替える。編集ボタンはMainActivity3を起動する。時間割は閲覧専用で、ここから枠の選択は変更できない。

## 境界条件

一時停止しても登録済みアラームは削除しない。DndReceiverが期限を読んで通知フィルターの変更を抑止する。期限経過後は保存キーが残っていても停止解除扱いになる。解除ボタンでは現在の時間割から通知モードを即時再計算しない。

onResumeは設定を読み直すだけで、権限許可後のアラーム再登録は行わない。ホーム画面の停止表示はonResumeまたはボタン操作時に更新され、期限経過を常時監視するタイマーはない。


# MainActivity2.kt 仕様書

対象: [src/main/java/com/example/super_kemo_taro3/MainActivity2.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/MainActivity2.kt)  
確認日: 2026-10-11  
パッケージ: `com.example.super_kemo_taro3`

## 役割

1〜8限の開始・終了時刻を管理し、TimeSettingsScreenへ渡す。保存時に時刻を書き込み、選択済みの枠のアラームを登録し直す。

## 入力・状態・保存

timesは開始・終了を交互に並べた16要素のList<String>。復元優先順位は、要素数16のBundle「editing_times」、timetable_prefsの保存値、TimeSettings.ktの初期値の順。

時刻の編集と初期化はtimesだけを変更する。保存を確定するまでSharedPreferencesは変更しない。onSaveInstanceStateは未保存のtimesもArrayListとしてBundleへ格納する。

## メソッド

| メソッド | 処理 |
| --- | --- |
| onCreate | timesの復元、Compose画面の設定、入力・保存・初期化・破棄のコールバック登録 |
| onSaveInstanceState | editing_timesへ編集中の16時刻を格納 |
| returnToMain | MainActivityを起動して自身をfinish |
| saveAllTimes | timeSettingsの保存キーで全16時刻をapplyし、reschedulAllAlarmsを実行 |
| reschedulAllAlarms | blocked_slotsの全選択枠に開始・終了アラームを再登録 |

メソッド名reschedulAllAlarmsは現コードの綴りを記載している。

## 保存と再登録の流れ

1. UI側の確認後、Activity側でもinvalidTimeIndicesが空であることを確認する。
2. timetable_prefsへtime_start_1〜8とtime_end_1〜8を保存する。
3. API 31以上で正確なアラームを登録できなければ設定画面を開き、再登録処理を終了する。
4. timetable_settingsのblocked_slotsを読み、曜日と時限を分解する。
5. MON〜THUを月〜木、FRIをテスト用の日曜日へ変換し、保存時刻から開始・終了日時を個別に計算する。過去ならそれぞれ1週間進める。画面の「金曜日」と保存キーFRIは維持する。
6. DndReceiver宛てにRTC_WAKEUPの正確なアラームを登録し、MainActivityへ戻る。

アラームのaction、識別子、extrasは共通データ仕様書を参照。再登録用の時刻・キーが解析できない枠は読み飛ばす。

## 境界条件と依存

保存はアラーム権限確認より先に行う。権限不足でも保存内容は残り、onSaveの呼び出し元はその後ホームへ戻る。権限許可後に自動で再登録する結果受信処理はない。

再登録処理は未保存の時刻を初期値で補わない。時刻範囲の検証は保存前のinvalidTimeIndicesに依存する。システムの戻る操作に独自の保存確認処理はなく、未保存の編集は永続化しない。UIと初期値はTimeSettingsScreen.kt・TimeSettings.kt、アラーム実行はDndReceiver.ktに依存する。


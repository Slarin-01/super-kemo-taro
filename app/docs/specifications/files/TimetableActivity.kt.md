# TimetableActivity.kt 仕様書

対象: [src/main/java/com/example/super_kemo_taro3/TimetableActivity.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/TimetableActivity.kt)  
確認日: 2026-10-11  
パッケージ: `com.example.super_kemo_taro3`

## 役割

曜日・時限ごとのミュート対象を選択し、その場でアラーム登録・取消と選択状態の保存を行う。AppScreenとTimetableScreenを使用する。

## データとメソッド

| 定義 | 内容 |
| --- | --- |
| slotTimes | カスタム時刻が未保存のときの1〜8限の開始・終了時刻 |
| dayMap | 曜日キーからCalendarの曜日定数への対応 |
| blockedSlots | Compose状態のSet<String>。例: MON_1 |
| preferences | timetable_settingsのSharedPreferences |
| onCreate / onResume | loadBlockedSlotsで保存済み選択を読み込む。onCreateは画面も設定 |
| loadBlockedSlots | blocked_slotsをtoSetでコピーして状態に反映 |
| toggleSlot | 選択済みなら除去・cancelSlot、未選択なら追加・scheduleSlot。その後集合を保存 |
| scheduleSlot | 対象枠の時刻から開始・終了アラームを予約 |
| cancelSlot | 登録時と同じaction・requestCodeのPendingIntentで両アラームを取消 |

## 曜日対応（現在の実装）

| キー | 画面の表示 | dayMapの登録曜日 |
| --- | --- | --- |
| MON | 月 | Calendar.MONDAY |
| TUE | 火 | Calendar.TUESDAY |
| WED | 水 | Calendar.WEDNESDAY |
| THU | 木 | Calendar.THURSDAY |
| FRI | 金 | **Calendar.SUNDAY（日曜日）** |

テスト用に、画面では金曜日と表示したままFRIを日曜日として登録する。MainActivity2とBootReceiverも同じ対応なので、時刻変更や端末再起動後も日曜日に動作する。

## 登録処理

API 31以上で正確なアラームを登録できない場合は端末設定を開いて終了する。曜日・時限キーを分解し、timetable_prefsのtime_start_N・time_end_Nを優先する。未設定または数値に変換できない部分はslotTimesで補う。

Calendarで対象曜日・時刻を設定し、秒・ミリ秒を0にする。過去なら開始・終了それぞれを1週間進める。DndReceiverへの開始ON・終了OFFをsetExactAndAllowWhileIdleで登録する。共通の識別形式は共通データ仕様書に記載。

## 保存と境界条件

枠の操作は即時保存で、保存確認ボタンはない。「メニューに戻る」はfinishする。権限不足で登録が中断されても選択集合は保存される。onResumeは集合の読み込みだけで、アラームの登録を再試行しない。

scheduleSlotのキーは画面が生成する形式を前提にしており、任意の不正キーへの防御はない。保存時刻の時・分の範囲検証もここでは行わない。未保存時の5限は13:25〜14:15で、TimeSettings.ktの初期値13:15〜14:25と異なる。


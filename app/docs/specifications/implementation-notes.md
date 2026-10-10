# 現実装の注意点

確認日: 2026-10-11。以下は現在のテスト用設定と、実際のコードにある差異・制約の記録。

## テスト用に金曜日の枠を日曜日へ割り当てる

| 箇所 | FRIの意味 |
| --- | --- |
| AppUi.ktの曜日表示 | 金曜日 |
| TimetableActivity.dayMap | Calendar.SUNDAY（日曜日） |
| MainActivity2.reschedulAllAlarms | Calendar.SUNDAY（日曜日） |
| BootReceiver.dayMap | Calendar.SUNDAY（日曜日） |

金曜日の表示とFRIキーを維持し、選択・時刻保存・端末再起動のいずれでも日曜日に予約する。DndReceiverは登録された日時の7日後へ再登録するため、翌週も日曜日になる。関連仕様: [TimetableActivity.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/TimetableActivity.kt.md)。

コード変更だけでは端末内の既存アラームの日時は更新されない。反映するには「時限カスタム」で時刻を保存し直すか、対象の金曜日の枠を一度解除して再選択する。テスト時はホームの「日曜日まで停止」をOFFにする。すでに過ぎた開始・終了時刻はそれぞれ翌週に予約する。

## 5限の初期値が異なる

TimeSettings.ktは13:15〜14:25。TimetableActivityとBootReceiverのslotTimesは13:25〜14:15。カスタム時刻がない状態での予約と、編集画面の初期値を保存した後の予約で時刻が変わる。関連仕様: [TimeSettings.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/files/TimeSettings.kt.md)。

## 権限を許可しても予約が自動的に再開されない場合がある

選択・時刻保存はアラーム権限不足でも保存されることがある。一方、設定画面から戻った際のonResumeでは保存値の読み込みのみを行い、全枠の再登録は行わない。

DndReceiverは通知権限がないと次週予約も行わない。後で権限を許可しても、その受信で途切れた予約を復元する処理はない。これらは保存済み選択があることと、実際のアラームが存在することが同じではない理由になる。

## 日時・通知状態の扱い

開始・終了の予約は個別に次回日時を求める。時限の途中で登録しても、直ちにミュートを有効にする処理はない。DndReceiverは固定の7日分を加算し、時刻・タイムゾーン変更を受信して予約を再計算する仕組みはない。

解除はALLへの変更要求で、利用者が変更前に使っていた通知フィルターへ戻す方式ではない。画面の「通知モードON」はミュート側を意味する。実際の端末の通知挙動は仕様書作成時に操作検証していない。

## リソースとテスト

backup_rules.xml・data_extraction_rules.xmlはManifestから参照されていない。colors.xmlのbutton_strokeとdrawableのic_launcher_foreground.xmlは現UI・アダプティブアイコンから直接参照されていない。editmodeは空の未使用クラス。

時刻の単体テストはあるが、曜日対応・アラーム実発火・権限の復帰処理を検証するテストはない。ExampleInstrumentedTestはパッケージ名の確認のみ。日曜日のアラームの実発火は端末で確認する必要がある。


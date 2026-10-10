# notificationtest.kt 仕様書

対象: [src/main/java/com/example/super_kemo_taro3/notificationtest.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/notificationtest.kt)  
確認日: 2026-10-11  
パッケージ: `com.example.super_kemo_taro3`

## 定義と役割

NotificationTestActivityはAppCompatActivityを継承し、通知フィルターを手動で切り替えるテスト画面を管理する。旧クラス名用にtypealias notificationtest = NotificationTestActivityを定義する。Manifestに登録する実クラス名はNotificationTestActivity。

## メソッドと状態

| 名前 | 処理 |
| --- | --- |
| status | ComposeのString状態。初期表示は「通知モード：確認中」 |
| onCreate | edge-to-edge、状態読み込み、AppScreenとNotificationTestScreenの設定、権限確認 |
| onResume | getDndStatusで実際の状態を読み直す |
| checkNotificationPolicyAccess | 権限がなければ通知ポリシーアクセスの端末設定を開く |
| toggleDnd | 権限確認後、ALLならNONE、それ以外ならALLへ変更し、表示文字列を返す |
| getDndStatus | 権限がなければ確認できない旨、ALLならOFF、それ以外ならONを返す |

## 操作

切替ボタンの処理結果をstatusへ代入して画面を更新する。権限がない状態で押すと設定画面を開き、許可が必要である旨を表示する。戻るボタンはfinishする。

ONはミュート側、OFFはALL側の表示。getDndStatusはNONE・PRIORITY・ALARMSなどを区別せず、ALL以外を一括してONと表示する。

## 保存と他機能との関係

SharedPreferencesを変更しない。時間割の選択、カスタム時刻、一時停止期限、アラーム予約も変更しない。操作はNotificationManagerへのフィルター変更要求であり、表示は現フィルターまたは操作時の結果文字列から作る。

この画面で変更した状態に対して、後続の時間割アラームが発火すればDndReceiverの処理が実行される。通知フィルター変更前の状態を保存して復元する処理はない。


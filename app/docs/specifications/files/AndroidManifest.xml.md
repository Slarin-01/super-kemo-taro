# AndroidManifest.xml 仕様書

対象: [src/main/AndroidManifest.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/AndroidManifest.xml)  
確認日: 2026-10-11

## 役割

Androidへアプリの権限、起動入口、画面、ブロードキャスト受信クラス、テーマ・アイコンを宣言する。

## 権限

| 権限 | コードでの用途 |
| --- | --- |
| ACCESS_NOTIFICATION_POLICY | NotificationManagerによる通知フィルター変更 |
| SCHEDULE_EXACT_ALARM | 正確なアラームの利用 |
| USE_EXACT_ALARM | 正確なアラーム用の宣言。BootReceiverで許可状況も確認 |
| RECEIVE_BOOT_COMPLETED | 端末再起動後のアラーム復元 |

権限を宣言するだけでコード側の許可確認を省略しているわけではない。MainActivity等が通知ポリシーアクセスや正確なアラームの利用可否を確認する。

## application属性

| 属性 | 値 |
| --- | --- |
| name | .SuperKemoTaroApp |
| allowBackup | true |
| icon / roundIcon | @mipmap/ic_launcher / @mipmap/ic_launcher_round |
| label | @string/app_name |
| supportsRtl | true |
| theme | @style/Theme.Superkemotaro3 |

## コンポーネント

| クラス | exported | 起動・受信条件 |
| --- | --- | --- |
| MainActivity | true | MAIN / LAUNCHER。ホーム画面 |
| MainActivity2 | false | 時限カスタム。windowSoftInputModeはadjustResize |
| MainActivity3 | false | メニュー |
| TimetableActivity | false | 曜日・時限の選択 |
| NotificationTestActivity | false | 通知フィルター手動テスト |
| DndReceiver | false | com.example.super_kemo_taro3.DND_ON / DND_OFF |
| BootReceiver | true | android.intent.action.BOOT_COMPLETED |

## 他ファイルとの関係

全画面はActivity内のsetContentでCompose UIを構築する。このManifestにレイアウトXMLの指定はない。typealiasのnotificationtestではなく、実クラスNotificationTestActivityを登録する。

res/xml/backup_rules.xmlとdata_extraction_rules.xmlは存在するが、applicationにfullBackupContent・dataExtractionRulesの参照属性がない。この2ファイルの独自ルールをManifestから適用する設定にはなっていない。


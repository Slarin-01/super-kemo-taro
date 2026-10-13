# AndroidManifest.xml：画面・権限・受信処理の登録

更新日: 2026-10-13  
対象: [AndroidManifest.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/AndroidManifest.xml)

## 役割

アプリ起動の準備、5画面、2つの受信処理を登録します。通知変更、時刻予約、再起動後の復元に必要な権限も宣言します。

## 変更したいとき

画面や受信処理を追加したり、必要な権限を変えたりするときに編集します。

## 主な機能

| 部分 | 担当すること |
| --- | --- |
| uses-permission | 通知変更、時刻予約、再起動の受信に使う権限を宣言する |
| application | 起動準備、アプリ名、アイコン、基本テーマを登録する |
| activity / activity-alias | 5画面とホーム画面を開く起動名を登録する |
| receiver | 予約時刻・再起動・アプリ更新を受け取る2つの入口を登録する |

## 扱う情報と結果

Androidがこの設定を読み、起動する画面や届ける知らせを決めます。端末側の許可を確認する処理はKotlinコードが担当します。

関連する場所: [起動の準備](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/app/ApplicationSetup.kt) / [知らせの受信](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/backend/receiver/AlarmReceivers.kt)

[仕様書の一覧へ](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/README.md)


# ApplicationSetup.kt：アプリ起動の準備

更新日: 2026-10-13  
対象: [ApplicationSetup.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/app/ApplicationSetup.kt)

## 役割

保存・予約・通知の処理を組み立て、画面と受信処理が同じ窓口を使えるようにします。5区分をつなぐ共通の準備部分です。

## 主な部分と機能

| 部分 | 担当すること |
|---|---|
| `BellApplication.onCreate` | アプリ起動時に、保存された明るい表示・暗い表示を適用します。 |
| `BellApplication.services` | 共通の処理を最初の利用時に一度だけ作ります。 |
| `AppServices` | 設定、通知、アラーム、一時停止、授業時刻などの処理を接続します。 |
| `Context.appServices` | 画面や受信入口から共通の処理を取り出す窓口です。 |

## 扱うデータと結果

Androidから渡されるアプリの情報を使って、設定保存と端末操作の窓口を作ります。起動時は保存済みの表示モードを読み取ります。表示モードが未設定なら、端末の設定を引き継ぎます。

作った処理の組はアプリの起動中に共用します。画面を開き直したり、アラームを受け取ったりしても同じ組を使います。

## つながり・変更したいとき

[AppSettings.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/backend/repository/AppSettings.kt)とバックエンドの処理を組み立てます。画面や受信処理が個別に保存・端末操作の窓口を作る必要はありません。

新しい処理の追加や、処理同士のつなぎ方を変えるときに編集します。`BellApplication`は[AndroidManifest.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/AndroidManifest.xml)に登録されている必要があります。

[仕様書の一覧へ](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/README.md)

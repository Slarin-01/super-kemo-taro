# HomePage.kt：ホーム画面

更新日: 2026-10-13  
対象: [HomePage.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/frontend/screens/HomePage.kt)

## 役割

選んだ時間割と、一時停止の状態を表示します。停止・明暗切り替え・編集の入口です。

## ファイル内の主な部分

| コードの部分 | 担当 |
|---|---|
| `HomeActivity` | 状態管理と画面をつなぎ、必要な許可の設定画面やメニューを開きます。 |
| `HomeScreen` | ボタン、停止状態、閲覧用の時間割を表示します。 |
| `HomeScreenPreview` | ライト・ダークの表示例をAndroid Studioへ渡します。 |

## 扱う情報と動き

- 入力は、選択済みの枠名と一時停止中かどうかです。枠名の例は `MON_1`（月曜1限）です。
- ボタン操作を状態管理へ渡します。停止期限の判断や設定保存は、この画面では行いません。
- 編集や端末設定から戻ると、最新の状態を読み直します。

状態管理: [TimetableState.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/frontend/viewmodel/TimetableState.kt)  
共通の時間割・配色・許可案内: [SharedScreenComponents.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/frontend/screens/SharedScreenComponents.kt)

## 変更したいとき

ホームのボタン名、配置、表示文を変えるときに編集します。
プレビューは固定の例を表示し、設定保存・通知制御・アラーム予約は実行しません。

[仕様書の一覧へ](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/README.md)

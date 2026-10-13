# TimetableState.kt：時間割とホームの表示状態

更新日: 2026-10-13  
対象: [TimetableState.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/frontend/viewmodel/TimetableState.kt)

## 役割

ホームと時間割編集で使う選択枠・停止状態を持ち、ボタン操作を処理側へ渡します。

## ファイル内の主な部分

| コードの部分 | 担当 |
|---|---|
| `HomeUiState` | ホームが表示する選択済みの枠と、一時停止中かどうかを持ちます。 |
| `HomeViewModel.refresh` | 必要な旧予約の移行を処理側へ依頼し、選択と停止状態を読み直します。 |
| `HomeViewModel.togglePause`・`toggleTheme` | 停止を切り替え、表示テーマの選択を保存します。 |
| `TimetableEditorViewModel` | 編集用の選択枠を読み込み、枠の選択・解除後に表示を更新します。 |

## 扱う情報と動き

- 枠は `MON_1` などの名前の一覧で扱います。曜日の変換やアラームの予約は処理側の担当です。
- 停止を切り替えた後は、最新の選択枠と停止状態を画面へ渡します。
- テーマの選択は保存し、新しい明暗を返します。実際に画面へ適用するのは画面側です。
- 枠を押すと保存・予約を処理側へ依頼し、その場で選択表示を更新します。予約用の許可が必要なら `true` を返します。

表示: [HomePage.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/frontend/screens/HomePage.kt)、[TimetableEditorPage.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/frontend/screens/TimetableEditorPage.kt)  
選択の保存・予約: [TimetableScheduling.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/backend/service/TimetableScheduling.kt)  
一時停止: [NotificationAutomation.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/backend/service/NotificationAutomation.kt)

## 変更したいとき

選択や停止状態の画面への反映、ボタン操作の受け渡しを変えるときに編集します。

[仕様書の一覧へ](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/README.md)

# .gitignore 仕様書

対象: [.gitignore](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/.gitignore)  
確認日: 2026-10-11

## 定義

有効なパターンは `/build` の1行。app直下のbuildディレクトリをGitの追跡対象から除外する。

## 利用関係

ビルド結果・中間生成物はソースとして管理しない。releaseディレクトリ、docs、srcはこのファイルのパターンでは除外しない。ただし親プロジェクトの除外設定が別途影響する可能性がある。

## 入出力

Gitの追跡対象を制御する設定で、Android実行時の処理・保存データ・Compose画面には影響しない。


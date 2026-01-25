# 1on1管理システム — API仕様（詳細）

作成日: 2026-01-25
バージョン: 1.0

概要: 主要エンドポイントのHTTPメソッド、URL、認可、リクエスト/レスポンスのJSONスキーマ、ステータスコード例を示す。

共通
- 認証: Bearer トークン（OIDC発行のアクセストークン）
- Content-Type: application/json
- エラーフォーマット例:

```json
{ "error": "INVALID_REQUEST", "message": "詳細メッセージ" }
```

1. 予定（OneOnOne）

- GET /api/oneonones
  - 説明: 予定一覧を取得（検索/フィルタ）
  - クエリ:
    - start (ISO8601)
    - end (ISO8601)
    - participant (user_id)
    - tag
    - page, size
  - 認可: 自分の予定またはマネージャーは自チーム
  - レスポンス 200:

```json
{
  "items": [
    {"id":"uuid","organizer_id":"uuid","participant_id":"uuid","start_at":"2026-01-25T10:00:00Z","end_at":"2026-01-25T10:30:00Z","location":"Zoom","status":"scheduled","tags":[]}
  ],
  "page":0,"size":20,"total":1
}
```

- POST /api/oneonones
  - 説明: 予定作成
  - ボディ:

```json
{
  "organizer_id":"uuid",
  "participant_id":"uuid",
  "start_at":"2026-01-25T10:00:00Z",
  "end_at":"2026-01-25T10:30:00Z",
  "location":"Zoom",
  "tags":["weekly"]
}
```

  - レスポンス 201:

```json
{ "id":"uuid", "message":"created" }
```

- GET /api/oneonones/{id}
  - 説明: 予定詳細取得
  - レスポンス 200: 上記の1件オブジェクトに加え `topics`, `minutes`, `actions` の短縮リストを含める

- PUT /api/oneonones/{id}
  - 説明: 予定更新（部分/全体）
  - ボディ: 更新可能なフィールド（start_at, end_at, location, status, tags）
  - レスポンス 200: 更新後オブジェクト

- DELETE /api/oneonones/{id}
  - 説明: 予定キャンセル
  - レスポンス 204

2. 議題（Topic）

- POST /api/topics
  - ボディ:

```json
{ "one_on_one_id":"uuid", "title":"進捗確認", "description":"〜について", "priority":1 }
```

  - レスポンス 201:
```json
{ "id":"uuid" }
```

- PUT /api/topics/{id}
  - 編集: title, description, priority
  - レスポンス 200: 更新オブジェクト

- GET /api/topics?one_on_one_id={id}
  - レスポンス 200: topics配列

3. 議事録（Minutes）

- POST /api/minutes
  - ボディ:
```json
{ "one_on_one_id":"uuid", "content":"議事録本文" }
```
  - レスポンス 201: {"id":"uuid"}

- GET /api/minutes?one_on_one_id={id}
  - レスポンス 200: minutes配列

4. アクション（ActionItem）

- POST /api/actions
  - ボディ:
```json
{ "one_on_one_id":"uuid", "title":"調査する", "assignee_id":"uuid", "due_date":"2026-02-01" }
```
  - レスポンス 201: {"id":"uuid"}

- PUT /api/actions/{id}
  - 更新可能: title, assignee_id, due_date, status
  - レスポンス 200: 更新オブジェクト

- GET /api/actions?assignee_id=&status=
  - レスポンス 200: action_items配列

5. レポート

- GET /api/reports/summary?start=&end=&group_by=team
  - 説明: 集計データ（JSON）
  - 例レスポンス:
```json
{ "total_oneonones":100, "completion_rate":0.85 }
```

- GET /api/reports/export?format=csv
  - 説明: CSVダウンロード（Content-Type: text/csv）

6. 認証・ユーザー管理

- GET /api/users/me — ログインユーザー情報
- GET /api/users?team_id= — ユーザー一覧（マネージャー/管理者向け）

7. リクエスト/レスポンス共通スキーマ（要約）

- UserSummary
```json
{ "id":"uuid", "name":"名前", "email":"a@b.c" }
```

- OneOnOneDetail
```json
{
  "id":"uuid",
  "organizer":{ "id":"uuid","name":"" },
  "participant":{ "id":"uuid","name":"" },
  "start_at":"ISO8601",
  "end_at":"ISO8601",
  "location":"",
  "status":"scheduled",
  "tags":[],
  "topics":[],
  "minutes":[],
  "actions":[]
}
```

8. エラーコード（例）
- 400 INVALID_REQUEST — リクエスト検証エラー
- 401 UNAUTHORIZED — 認証失敗
- 403 FORBIDDEN — 権限不足
- 404 NOT_FOUND — 該当リソースなし
- 409 CONFLICT — 競合（重複/タイムコンフリクト）

9. ロギング/監査
- 重要操作（作成/更新/削除/エクスポート）は監査ログへ記録（user_id, operation, resource_id, timestamp）

10. 次の詳細化タスク
- 各エンドポイントのJSON Schema（OpenAPI 3.0）化
- レスポンス例とエラーハンドリング詳細化

---
このAPI仕様はプロジェクト開始時点の初期案です。必要に応じてOpenAPI化してCIでスキーマ検証することを推奨します。

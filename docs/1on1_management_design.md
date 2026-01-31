# 1on1管理システム 基本設計書

作成日: 2026-01-25
バージョン: 1.0

このドキュメントは、`docs/1on1_management_requirements.md` の要件を基にした基本設計書（概観・主要コンポーネント・データ設計・API設計・運用方針）である。

## 1. アーキテクチャ概観
- タイプ: Webアプリケーション（SPAフロントエンド + REST/GraphQLバックエンド + RDBMS）
- コンポーネント:
  - フロントエンド: React (or Vue) SPA、認証はSSO/OIDC経由。
  - バックエンドAPI: Java(Spring Boot) でREST APIを提供。
  - データベース: PostgreSQL（主データ）、Redis（キャッシュ/ワークキュー用）
  - 認証・認可: OIDC/SAML連携 + RBAC
  - 通知: SMTP / Slack API / Webhook
  - カレンダー同期: Google Calendar & Microsoft Graph 経由のOAuth連携
---
## 15. Dockerでの起動

開発環境向けにDocker Composeで主要コンポーネントを起動する手順を示す。前提として `Docker` と `docker compose` がインストールされていること。

- 概要: PostgreSQL（db）・バックエンド（backend）・フロントエンド（frontend）をコンテナで立ち上げる想定。

1. ルートに `docker-compose.yml` を置く（例）:

```yaml
version: '3.8'
services:
  db:
    image: postgres:15
    environment:
      POSTGRES_USER: app
      POSTGRES_PASSWORD: password
      POSTGRES_DB: oneonone
    volumes:
      - db-data:/var/lib/postgresql/data

  backend:
    build: ./awareness-campaign
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/oneonone
      SPRING_DATASOURCE_USERNAME: app
      SPRING_DATASOURCE_PASSWORD: password
    ports:
      - '8080:8080'
    depends_on:
      - db

  frontend:
    build: ./frontend
    ports:
      - '3000:3000'
    depends_on:
      - backend

volumes:
  db-data:
```

2. イメージをビルドして起動する:

```bash
docker compose up --build
```

3. 停止・クリーンアップ:

```bash
docker compose down
```

- 注意事項:
  - 実際の環境ではパスワード等のシークレットは平文で置かず、`.env` や Docker Secrets / Kubernetes Secrets を利用すること。
  - カレンダー連携や外部APIのOAuth認証情報はローカル開発ではモックまたは開発用資格情報を利用する。
  - 本番運用はKubernetes等のオーケストレーション環境を想定し、スケール・冗長化・シークレット管理を実施すること。

この基本設計書は初期案です。DBの正規化、APIの詳細スキーマ（JSONスキーマ）やER図の作成、UIの具体的ワイヤーフレーム作成は次のステップで詳細化します。

## 2. 運用構成（想定）
- 環境: Dev / Staging / Production
- デプロイ: コンテナ（Docker） → Kubernetes（EKS/GKE等）
- CI/CD: GitHub Actions / GitLab CI によるビルド・テスト・デプロイ
- モニタリング: Prometheus + Grafana、ログは ELK/Opensearch

## 3. 主要モジュール設計
- AuthService: SSO連携、アクセストークン発行、ユーザー同期
- OneOnOneService: 予定の作成・編集・検索、カレンダー連携
- TopicService: 議題管理（履歴含む）
- MinutesService: 議事録保存・検索
- ActionService: アクションアイテム管理（担当・期限・状態・通知）
- ReportService: 集計・CSVエクスポート
- NotificationService: メール・Slack通知・リマインダー

## 4. データ設計（テーブル）
- users
  - id (UUID, PK)
  - name (VARCHAR)
  - email (VARCHAR, UNIQUE)
  - role (ENUM: user/manager/admin)
  - department (VARCHAR)
  - created_at, updated_at

- one_on_ones
  - id (UUID, PK)
  - organizer_id (FK -> users.id)
  - participant_id (FK -> users.id)
  - start_at (TIMESTAMP)
  - end_at (TIMESTAMP)
  - location (VARCHAR)
  - status (ENUM: scheduled/cancelled/completed)
  - tags (JSONB)
  - created_at, updated_at

- topics
  - id (UUID, PK)
  - one_on_one_id (FK -> one_on_ones.id)
  - title (VARCHAR)
  - description (TEXT)
  - priority (INT)
  - created_by (FK -> users.id)
  - created_at, updated_at

- minutes
  - id (UUID, PK)
  - one_on_one_id (FK -> one_on_ones.id)
  - content (TEXT)
  - created_by (FK -> users.id)
  - created_at, updated_at

- action_items
  - id (UUID, PK)
  - one_on_one_id (FK -> one_on_ones.id)
  - title (VARCHAR)
  - assignee_id (FK -> users.id)
  - due_date (DATE)
  - status (ENUM: todo,in_progress,done)
  - created_at, updated_at

インデックス: users.email, one_on_ones(start_at), action_items.assignee_id

## 5. API設計（代表例）
- 認証
  - POST /auth/sso/callback — SSOコールバック（OIDC/SAML）

- 予定（OneOnOne）
  - GET /api/oneonones?start=&end=&participant=&tag= — 予定一覧（検索/フィルタ）
  - POST /api/oneonones — 予定作成 (payload: organizer_id, participant_id, start_at, end_at, location, tags)
  - GET /api/oneonones/{id} — 予定詳細
  - PUT /api/oneonones/{id} — 予定更新
  - DELETE /api/oneonones/{id} — 予定キャンセル

- 議題（Topic）
  - POST /api/topics — 議題作成 (one_on_one_id, title, description, priority)
  - PUT /api/topics/{id} — 編集
  - GET /api/topics?one_on_one_id=

- 議事録（Minutes）
  - POST /api/minutes — 議事録作成 (one_on_one_id, content)
  - GET /api/minutes?one_on_one_id=

- アクション（ActionItem）
  - POST /api/actions — アクション作成 (one_on_one_id, title, assignee_id, due_date)
  - PUT /api/actions/{id} — 状態更新
  - GET /api/actions?assignee_id=&status=

- レポート
  - GET /api/reports/summary?start=&end=&group_by=team — 集計データ
  - GET /api/reports/export?format=csv — CSV出力

認可: 各APIはRBACチェックを実施（例: マネージャーは自チームのデータアクセス可）。

## 6. シーケンス例（予定作成 → カレンダー同期）
1. フロントエンドが `POST /api/oneonones` を呼ぶ。
2. バックエンドはバリデーション後にDBへ保存。
3. NotificationService がカレンダー同期キューへジョブ登録（非同期）。
4. ワーカーが外部カレンダーAPIへOAuthトークンで同期。
5. 成功/失敗をDBに記録し、必要であればユーザーへ通知。

## 7. UI/UX（主要画面の挙動）
- ダッシュボード: 次回の1on1カード、未完了アクション一覧、最近の議事録ハイライト
- 予定作成モーダル: 参加者自動補完、推奨時刻（空き時間提示）、タグ選択
- 予定詳細: 議題一覧（ドラッグで順序変更）、議事録編集エリア、アクション作成ショートカット
- レポート画面: 期間選択、集計グラフ（折れ線・棒グラフ）、CSVエクスポートボタン

## 8. セキュリティ設計
- 通信は常にTLSを使用。
- 機密性の高い議事録/コメントは保存時にフィールド単位で暗号化（KMS利用を推奨）。
- RBACによる最小権限付与。
- 監査ログ: 重要操作（閲覧・エクスポート・削除）を監査ログに記録。
- CSRF/XSS対策: フロントエンドでのサニタイズ、HTTPOnlyセッションクッキーまたはBearerトークン利用。

## 9. パフォーマンス・スケーリング
- DB: リードレプリカを利用し読み取り負荷を分散。
- キャッシュ: 頻出データはRedisでキャッシュ。
- バックグラウンド処理: 重い同期・集計処理はワーカーで非同期実行。

## 10. テスト戦略
- 単体テスト: 各サービスのロジック単位
- 結合テスト: DB・外部APIモックを使ったAPIレベル
- E2Eテスト: 主要ユースケース（認証、予定作成、議事録、アクション、通知）
- 受入テスト: 受入基準に沿った手順を自動化

## 11. 運用・監視
- 可観測性: メトリクス（リクエスト遅延、エラー率）、トレース（分散トレーシング）
- アラート: エラー率やレイテンシの閾値超過でSlack通知
- バックアップ: DBの定期スナップショットとポイントインタイム復旧計画

## 12. 移行/インポート計画
- 既存データCSVインポート用のバッチAPIを用意
- インポート前にデータクレンジング・マッピング画面を提供

## 13. 非機能要件のマッピング（要件→設計対応）
- 可用性 99.5% → Kubernetes HA構成、Pod自動再起動、ヘルスチェック
- セキュリティ TLS/KMS → OIDC/SAML + KMS暗号化
- パフォーマンス 100ms → DBインデックス、キャッシュ、ページネーション

## 14. 今後の拡張候補
- MLによる議題カテゴリ自動タグ付け、要約生成
- 1on1のテンプレート管理と共有
- 組織横断の匿名フィードバック集計


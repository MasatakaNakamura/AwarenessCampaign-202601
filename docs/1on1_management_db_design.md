# 1on1管理システム — DB設計（詳細）

作成日: 2026-01-25
バージョン: 1.0

概要: `docs/1on1_management_requirements.md` と `docs/1on1_management_design.md` を基に、主要テーブル定義、制約、インデックス、サンプルSQL、保持方針などを詳細化する。

## 1. ER概略

users 1───n one_on_ones
one_on_ones 1───n topics
one_on_ones 1───n minutes
one_on_ones 1───n action_items
users 1───n action_items (assignee)

## 2. テーブル定義（代表）

- users
  - id UUID PRIMARY KEY DEFAULT gen_random_uuid()
  - name TEXT NOT NULL
  - email TEXT NOT NULL UNIQUE
  - role TEXT NOT NULL -- ENUM 値をアプリで管理または DB CHECK で制限
  - department TEXT
  - created_at TIMESTAMPTZ DEFAULT now()
  - updated_at TIMESTAMPTZ DEFAULT now()

- one_on_ones
  - id UUID PRIMARY KEY DEFAULT gen_random_uuid()
  - organizer_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT
  - participant_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT
  - start_at TIMESTAMPTZ NOT NULL
  - end_at TIMESTAMPTZ
  - location TEXT
  - status TEXT NOT NULL DEFAULT 'scheduled'
  - tags JSONB DEFAULT '[]'::jsonb
  - created_at TIMESTAMPTZ DEFAULT now()
  - updated_at TIMESTAMPTZ DEFAULT now()

- topics
  - id UUID PRIMARY KEY DEFAULT gen_random_uuid()
  - one_on_one_id UUID NOT NULL REFERENCES one_on_ones(id) ON DELETE CASCADE
  - title TEXT NOT NULL
  - description TEXT
  - priority INT DEFAULT 0
  - created_by UUID REFERENCES users(id)
  - created_at TIMESTAMPTZ DEFAULT now()
  - updated_at TIMESTAMPTZ DEFAULT now()

- minutes
  - id UUID PRIMARY KEY DEFAULT gen_random_uuid()
  - one_on_one_id UUID NOT NULL REFERENCES one_on_ones(id) ON DELETE CASCADE
  - content TEXT NOT NULL
  - created_by UUID REFERENCES users(id)
  - created_at TIMESTAMPTZ DEFAULT now()
  - updated_at TIMESTAMPTZ DEFAULT now()

- action_items
  - id UUID PRIMARY KEY DEFAULT gen_random_uuid()
  - one_on_one_id UUID NOT NULL REFERENCES one_on_ones(id) ON DELETE CASCADE
  - title TEXT NOT NULL
  - assignee_id UUID REFERENCES users(id)
  - due_date DATE
  - status TEXT NOT NULL DEFAULT 'todo'
  - created_at TIMESTAMPTZ DEFAULT now()
  - updated_at TIMESTAMPTZ DEFAULT now()

## 3. インデックス案
- users(email) UNIQUE
- one_on_ones(start_at) -- 予定一覧の検索高速化
- one_on_ones(participant_id)
- action_items(assignee_id, status)
- topics(one_on_one_id)

## 4. 制約・整合性
- 外部キーで参照整合性を担保。ユーザー削除は通常 `ON DELETE RESTRICT` とし、退職ユーザーは `disabled` フラグで扱う。
- ステータスやロールはアプリ側で定義するか、DB側で `CHECK` 制約を用いる。

## 5. サンプルCREATE文（Postgres）

```sql
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name TEXT NOT NULL,
  email TEXT NOT NULL UNIQUE,
  role TEXT NOT NULL,
  department TEXT,
  created_at TIMESTAMPTZ DEFAULT now(),
  updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE one_on_ones (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  organizer_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  participant_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  start_at TIMESTAMPTZ NOT NULL,
  end_at TIMESTAMPTZ,
  location TEXT,
  status TEXT NOT NULL DEFAULT 'scheduled',
  tags JSONB DEFAULT '[]'::jsonb,
  created_at TIMESTAMPTZ DEFAULT now(),
  updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_oneonones_start_at ON one_on_ones(start_at);
CREATE INDEX idx_oneonones_participant ON one_on_ones(participant_id);
```

## 6. 保持・アーカイブ方針
- 議事録やアクションはデフォルトで7年保持を想定（法令・組織ルールで変更可）。古いデータは年月単位でパーティショニングしてアーカイブに移行。
- パーティショニング例: `one_on_ones` を `start_at` 年/月でレンジパーティション。

## 7. バックアップ・リストア
- 日次スナップショット + PITR（Point-in-time recovery）を有効にする。
- 重要データの暗号化（バックアップ先でも暗号化）を考慮。

## 8. マイグレーション方針
- Flyway/Liquibase を利用しバージョン管理する。
- マイグレーションはトランザクション化し、ロールバック手順を明記する。

## 9. 運用上の注意点
- 大量エクスポート時はストレージIOに注意し、バックグラウンド処理で実行する。
- フルテキスト検索が必要な場合は `pg_trgm` や ElasticSearch の導入を検討。

## 10. 次の作業（推奨）
- ER図（図形式）作成（draw.io/plantuml）
- JSON Schema / OpenAPI に合わせた API レスポンスの DB マッピング表作成
- パーティションとインデックスの負荷テスト

---
このドキュメントは初期の詳細化案です。要件や想定トラフィックに応じて調整してください。

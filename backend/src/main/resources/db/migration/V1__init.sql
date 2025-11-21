-- notes
CREATE TABLE IF NOT EXISTS notes (
  id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id       BIGINT       NOT NULL,
  title         VARCHAR(200) NOT NULL,
  content_md    TEXT         NOT NULL,
  content_text  TEXT         NOT NULL,
  status        VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
  tags          VARCHAR(255)          DEFAULT '',
  created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- summaries
CREATE TABLE IF NOT EXISTS summaries (
  id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  note_id        BIGINT       NOT NULL,
  user_id        BIGINT       NOT NULL,
  model          VARCHAR(100) NOT NULL,
  style          VARCHAR(50)  NOT NULL,
  one_line       TEXT         NOT NULL,
  paragraph      TEXT         NOT NULL,
  tokens_prompt  INT          NOT NULL,
  tokens_output  INT          NOT NULL,
  cost           DECIMAL(18,6) NOT NULL DEFAULT 0,
  created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notes_user_created ON notes(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notes_user_title   ON notes(user_id, title);
CREATE INDEX IF NOT EXISTS idx_notes_user_tags    ON notes(user_id, tags);
CREATE INDEX IF NOT EXISTS idx_sum_note_created   ON summaries(note_id, created_at DESC);

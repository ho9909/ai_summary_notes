CREATE TABLE notes (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  title VARCHAR(200) NOT NULL,
  content TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE summaries (
  id BIGSERIAL PRIMARY KEY,
  note_id BIGINT NOT NULL REFERENCES notes(id),
  style VARCHAR(20) NOT NULL,
  one_line VARCHAR(300) NOT NULL,
  paragraph TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 권장 인덱스
CREATE INDEX idx_notes_user_title ON notes(user_id, title);
CREATE INDEX idx_summaries_note_created ON summaries(note_id, created_at);
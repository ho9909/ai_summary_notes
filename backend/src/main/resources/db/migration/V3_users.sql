CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY,
  nickname VARCHAR(50) NOT NULL
);

MERGE INTO users KEY(id) VALUES (1, 'demo'); -- 기본 사용자

# AI Summary Notes

AI Summary Notes 는 **노트 작성 + AI 요약**을 한 번에 할 수 있는 작은 웹 앱입니다.

- 노트를 Markdown 으로 작성
- 태그, 상태(DRAFT/PUBLISHED) 관리
- 버튼 한 번으로 AI 요약(한 줄 요약 + 자세한 요약) 생성
- 생성된 요약 / 사용 모델 / 토큰 수 / 비용 기록

백엔드는 **Spring Boot 3 + JPA + Flyway + PostgreSQL**,  
프론트엔드는 **React + Vite + TypeScript** 로 구성되어 있습니다.

---

## 1. 기술 스택

### Backend

- Java 21
- Spring Boot 3.x
- Spring Web / Spring Data JPA
- Flyway (DB 마이그레이션)
- H2 (로컬 in-memory) / PostgreSQL (Docker)
- Gradle

### Frontend

- React 19
- TypeScript
- Vite 5
- React Router
- react-markdown + remark-gfm

### Infra

- Docker / Docker Compose
- PostgreSQL 16
- Nginx (프론트 정적 파일 서빙)

---

## 2. 디렉터리 구조

대략적인 구조는 다음과 같습니다.

```text
ai-summary-notes/
├─ backend/          # Spring Boot 백엔드 (도커에서 사용)
├─ frontend/         # React 프론트엔드
├─ docker-compose.yml
└─ Dockerfile        # (루트용, 필요 시)
```
실제 로컬 폴더 이름은 환경에 따라 조금 다를 수 있습니다.
Docker 기준으로는 ./backend, ./frontend 를 바라보는 구조입니다.

## 3. 실행 방법
3-1. Docker Compose 로 한 번에 실행 (추천)
### 1) 필수 준비
Docker Desktop (또는 Docker + docker compose)

이 프로젝트를 클론한 상태

### 2) 환경 변수(선택)
루트(docker-compose.yml 있는 위치)에 .env 파일을 만들 수 있습니다.

#### 예시: 가장 기본 mock 요약기
AI_PROVIDER=mock
· OpenAI / Gemini 등을 붙였다면 여기에 키 추가
· OPENAI_API_KEY=...
· GEMINI_API_KEY=...

docker-compose.yml 안에서는 대략 이런 식으로 사용합니다.
services:
  db:
    image: postgres:16
    ...

  backend:
    build:
      context: ./backend
    environment:
      SPRING_PROFILES_ACTIVE: postgres
      DB_HOST: db
      DB_PORT: 5432
      DB_NAME: ainote
      DB_USER: ainote
      DB_PASS: ainote
      AI_PROVIDER: ${AI_PROVIDER:-mock}
      # OPENAI_API_KEY: ${OPENAI_API_KEY}
    ports:
      - "8080:8080"
    depends_on:
      - db

  frontend:
    build:
      context: ./frontend
    ports:
      - "5173:80"
    depends_on:
      - backend
### 3) 실행
루트 디렉터리에서:

docker compose up --build
성공하면:
백엔드 API: http://localhost:8080
프론트 UI: http://localhost:5173
브라우저에서 http://localhost:5173 접속해서 사용하면 됩니다.

#### 3-2. 로컬 개발용 실행 (Docker 없이)
백엔드는 기본적으로 H2 in-memory DB 를 사용하도록 application.yml 이 설정되어 있습니다.
Postgres 없이 간단히 개발하고 싶을 때 이 방법을 쓰면 됩니다.

Backend (Spring Boot)
cd backend   # 또는 실제 백엔드 폴더
./gradlew bootRun        # Mac/Linux
./gradlew.bat bootRun    # Windows
정상 기동되면 콘솔에 비슷한 로그가 보입니다.

Tomcat started on port 8080
Started AiSummaryNotesApplication in X.XXX seconds
기본 포트: 8080

기본 DB: H2 (in-memory, 애플리케이션 종료 시 데이터 초기화)

Frontend (React + Vite)
cd frontend
npm install      # 최초 1회
npm run dev

성공하면:
VITE v5.x.x  ready in XXX ms
Local:  http://localhost:5173/
브라우저에서 http://localhost:5173 접속해서 개발하면 됩니다.

프론트는 기본적으로 VITE_API_BASE 가 비어 있으면
http://localhost:8080 을 API 서버로 사용합니다.

## 4. 환경 변수 / 설정
4-1. 백엔드 (AI 요약 설정)
src/main/resources/application.yml 의 일부:

ai:
  provider: ${AI_PROVIDER:mock}      # mock | openai | (직접 확장한 경우 gemini/ollama 등)
  timeout-ms: ${AI_TIMEOUT_MS:20000}
  fallbackOnError: ${AI_FALLBACK_ON_ERROR:true}
  cacheMinutes: ${AI_CACHE_MINUTES:10}

  openai:
    api-key: ${OPENAI_API_KEY:}
    model: ${OPENAI_MODEL:gpt-4o-mini}
    base-url: ${OPENAI_BASE_URL:https://api.openai.com}

  ollama:
    base-url: ${OLLAMA_BASE_URL:http://localhost:11434}
    model: ${OLLAMA_MODEL:llama3.1}

  costs:
    prompt_per_1k: ${AI_COST_PROMPT:0.0}
    output_per_1k: ${AI_COST_OUTPUT:0.0}

· AI_PROVIDER=mock
    · 내부 MockSummarizer 사용 (외부 API 호출 없음, 테스트용)
· AI_PROVIDER=openai
    · OPENAI_API_KEY, OPENAI_MODEL, OPENAI_BASE_URL 필요

· 직접 Gemini, Ollama 등을 붙였다면 Summarizer 구현체와 설정만 추가하면 됩니다.

### 4-2. 백엔드 (DB 설정, Docker용)
docker-compose.yml 에서 아래 환경 변수를 통해 설정합니다.

SPRING_PROFILES_ACTIVE=postgres
DB_HOST=db
DB_PORT=5432
DB_NAME=ainote
DB_USER=ainote
DB_PASS=ainote
spring.datasource.* 는 postgres 프로파일에서 이 값들을 사용하도록 설정되어 있습니다.

### 4-3. 프론트엔드
frontend/.env.sample:
VITE_API_BASE=http://localhost:8080
VITE_DEFAULT_USER_ID=1
원하는 경우 복사해서 사용:
cd frontend
cp .env.sample .env
· VITE_API_BASE
    · API 서버의 베이스 URL (기본: http://localhost:8080)

· VITE_DEFAULT_USER_ID
    · 로그인 없이 사용할 기본 사용자 ID (기본: 1)

프론트에서는 모든 요청에 X-USER-ID 헤더를 자동으로 붙입니다.
// frontend/src/api/client.ts
headers.set("X-USER-ID", currentUserId());
currentUserId() 는 localStorage.USER_ID 또는 VITE_DEFAULT_USER_ID 를 사용합니다.

## 5. 웹 UI 사용 방법
브라우저에서 http://localhost:5173 접속

상단 메뉴

목록: 노트 목록

새 노트: 신규 노트 작성

### 5-1. 새 노트 작성
상단 우측 “새 노트” 클릭

제목, 태그(쉼표로 구분), 본문(Markdown) 입력

하단의 “저장” 버튼 클릭

저장 후 자동으로 상세 화면으로 이동하거나 목록에서 확인 가능

### 5-2. 노트 편집
목록에서 제목을 클릭 → 상세 화면

상세 화면에서 편집 모드를 켜고 제목/본문/태그 수정

일정 시간마다 자동 저장(디바운스) + 수동 저장 버튼 제공

오른쪽에 Markdown 미리보기를 켜거나 끌 수 있음

### 5-3. 요약 생성
노트 상세 화면에서 “요약 생성” 버튼 클릭

옵션(예: 간단/자세히 등)을 선택 후 실행

백엔드에서 Summarizer(OpenAI/Mock/기타) 호출

성공 시 화면 아래에 요약 카드가 나타납니다.

한 줄 요약

전체 요약 본문

사용 모델 (model)

프롬프트/출력 토큰 수

비용(cost)을 설정한 경우 금액 표기

에러가 나면:

화면에 JSON 형태의 에러 메시지가 표시되거나

브라우저 콘솔 / 백엔드 로그에 원인이 찍힙니다.

## 6. 자주 만나는 문제 & 확인 포인트
### 6-1. 저장이 안 되고 콘솔에 ERR_CONNECTION_REFUSED
백엔드 컨테이너/프로세스가 죽어 있었을 가능성

확인 방법:
docker compose ps          # backend 상태 확인
docker compose logs backend --tail=100
에러 메시지에 따라:

Flyway 마이그레이션 오류 (스키마 타이핑 문제)

Summarizer 설정 충돌

DB 연결 오류 등

을 하나씩 해결해주면 됩니다.

### 6-2. 요약 버튼 누르면 500 / INTERNAL_ERROR
DB 스키마와 엔티티 불일치 (예: summaries.user_id NOT NULL 인데 엔티티에 필드가 없는 경우)

AI_PROVIDER 설정은 했지만 API 키가 비어 있는 경우

백엔드 로그에서 Caused by: 아래 첫 번째 에러를 보고 대응하세요.

## 7. 개발 메모
노트는 notes 테이블, 요약은 summaries 테이블에 저장됩니다.

요약 생성 시:

notes에서 원문 로딩

Summarizer 구현체 호출 (Mock / OpenAI / 기타)

결과를 summaries로 저장

최신 1건을 노트 상세 화면에서 보여줌

커스텀 요약기, 다른 LLM 등을 붙이고 싶으면 Summarizer 인터페이스를 구현하고
설정(SummarizerConfig)에서 ai.provider 값에 따라 원하는 구현체를 선택하도록 확장하면 됩니다.
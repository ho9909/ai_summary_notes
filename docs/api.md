# AI Summary Notes API

노트 CRUD와 요약(Mock/AI) 기능을 제공하는 백엔드 API입니다. 프리코스 범위에서는 **헤더 기반 식별(X-USER-ID)** 를 사용합니다.

- **Base URL**: `/`
- **공통 헤더**
  - `X-USER-ID: <number>` (예: `1`)
  - `Content-Type: application/json; charset=utf-8`

> 선택: JWT를 사용할 경우 `Authorization: Bearer <token>`를 함께 보낼 수 있습니다(프로젝트 선택사항).

---

## 에러 포맷

요청이 실패하면 다음 형식으로 응답합니다.

```json
{
  "timestamp": "2025-11-08T12:00:00Z",
  "path": "/notes",
  "error": "Bad Request",
  "code": "VALIDATION_FAILED",
  "messages": ["title must not be blank"]
}
code 예시: VALIDATION_FAILED, FORBIDDEN, NOT_FOUND, INTERNAL_ERROR 등

## 페이징 공통 규칙
쿼리 파라미터: page(기본 0), size(기본 10, 최대 100)

페이징 응답 필드: content, totalElements, totalPages, size, number, first, last, empty

노트(Notes)
목록 조회
GET /api/notes

헤더: X-USER-ID (필수)

쿼리:

query(선택) — 제목 부분 검색

tag(선택) — 태그 포함 검색 (쉼표로 구분된 문자열 중 일부 포함)

page, size(선택)

query와 tag를 동시에 주면 교집합(AND)으로 필터됩니다.

예시

bash
curl -s -H "X-USER-ID: 1" "http://localhost:8080/api/notes?query=회의&tag=ai&page=0&size=10"
성공(200) 응답 예시

json
{
  "content": [
    {
      "id": 12,
      "title": "회의 메모",
      "contentMd": "# 회의 메모\n안건 정리...",
      "contentText": "회의 메모 안건 정리...",
      "status": "DRAFT",
      "tags": "ai,spring"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0,
  "first": true,
  "last": true,
  "empty": false
}
생성
POST /api/notes

헤더: X-USER-ID (필수)

바디:

json
{
  "title": "주간 리포트",
  "contentMd": "# 보고서\n이번 주 진행 상황...",
  "tags": "ai,report"
}

성공(200) 응답
json
15
상세
GET /api/notes/{id}

헤더: X-USER-ID (필수)

bash
코드 복사
curl -s -H "X-USER-ID: 1" "http://localhost:8080/api/notes/15"
성공(200) 응답

json
코드 복사
{
  "id": 15,
  "title": "주간 리포트",
  "contentMd": "# 보고서\n이번 주 진행 상황...",
  "contentText": "보고서 이번 주 진행 상황...",
  "status": "DRAFT",
  "tags": "ai,report"
}
수정(부분)
PATCH /api/notes/{id}

헤더: X-USER-ID (필수)

바디: 변경할 필드만 포함

json
코드 복사
{
  "title": "주간 리포트(수정)",
  "tags": "ai,report,summary"
}
성공(200) 응답 본문 없음

삭제
DELETE /api/notes/{id}

헤더: X-USER-ID (필수)

성공(200) 응답 본문 없음

요약(Summaries)
요약 생성(Mock/AI)
POST /api/notes/{id}/summary

헤더: X-USER-ID (필수)

바디(선택): style = brief | detailed (기본값: brief)

json
코드 복사
{ "style": "detailed" }
성공(200) 응답

json
코드 복사
{
  "oneLine": "이번 주 진행 현황과 다음 액션 요약",
  "paragraph": "- 핵심 진행 3건\n- 리스크 1건\n- 다음 주 계획...",
  "model": "gpt-4o-mini",
  "style": "detailed",
  "tokensPrompt": 420,
  "tokensOutput": 95,
  "cost": 0.00315
}
D11 캐시/폴백이 켜져 있으면 TTL 내 동일 스타일 재요청 시 기존 결과를 재사용할 수 있습니다.

최신 요약 1개 조회
GET /api/notes/{id}/summary

헤더: X-USER-ID (필수)

있음(200) 응답

json
코드 복사
{
  "oneLine": "이번 주 진행 현황과 다음 액션 요약",
  "paragraph": "- 핵심 진행 3건\n- 리스크 1건\n- 다음 주 계획...",
  "model": "gpt-4o-mini",
  "style": "detailed",
  "tokensPrompt": 420,
  "tokensOutput": 95,
  "createdAt": "2025-11-17T05:22:11Z",
  "cost": 0.00315
}
없음(204) 본문 없음

요약 이력(페이징)
GET /api/notes/{id}/summary/list

헤더: X-USER-ID (필수)

쿼리: page, size

성공(200) 응답

json
코드 복사
{
  "content": [
    {
      "id": 33,
      "oneLine": "이번 주 진행 현황과 다음 액션 요약",
      "model": "gpt-4o-mini",
      "style": "detailed",
      "tokensPrompt": 420,
      "tokensOutput": 95,
      "createdAt": "2025-11-17T05:22:11Z",
      "cost": 0.00315
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0,
  "first": true,
  "last": true,
  "empty": false
}
사용자(Users)
내 프로필
GET /api/users/me

헤더: X-USER-ID (필수)

성공(200)

json
코드 복사
{ "id": 1, "nickname": "demo" }
단일 조회(디버그)
GET /api/users/{id}

헤더: X-USER-ID (필수)

성공(200)

json
코드 복사
{ "id": 1, "nickname": "demo" }
인증(Auth) — Header-Auth(기본)
로그인(간단 발급/재사용)
POST /api/auth/login

헤더: (없어도 됨)

바디:

기존 계정 재사용: { "userId": 1 }

새 계정 생성: { "nickname": "alice" }

성공(200)

json
코드 복사
{
  "userId": 2,
  "nickname": "alice",
  "howToUseHeader": "Send header: X-USER-ID: 2"
}
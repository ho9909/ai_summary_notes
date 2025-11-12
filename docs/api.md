
# 공통

Base URL: /

공통 헤더: X-USER-ID: <number> (예: 1)

Content-Type: application/json; charset=utf-8

에러 포맷

{
  "timestamp": "2025-11-08T12:00:00Z",
  "path": "/notes",
  "error": "Bad Request",
  "code": "VALIDATION_FAILED",
  "messages": ["title must not be blank"]
}
## 1) 노트 생성

POST /notes

요청 바디

{ "title": "오늘 배운 내용", "content": "스프링 DI, 빈 등록 방식 학습" }

규칙: title, content 둘 다 공백 불가

응답 바디
{ "id": 1 }

예시(cURL)
curl -X POST http://localhost:8080/notes \
  -H 'Content-Type: application/json' -H 'X-USER-ID: 1' \
  -d '{"title":"오늘 배운 내용","content":"스프링 DI"}'

## 2) 노트 목록(검색+페이지네이션)

GET /notes?query=&page=&size=

쿼리 파라미터

query(선택): 제목 부분 일치

page(기본 0), size(기본 10, 1~100)

응답 바디
{
  "content": [
    { "id": 1, "title": "오늘 배운 내용", "content": "스프링 DI...", "createdAt": "2025-11-08T12:00:00Z", "updatedAt": "2025-11-08T12:00:00Z" }
  ],
  "totalElements": 1, "totalPages": 1, "size": 10, "number": 0
}

예시(cURL)
curl -H 'X-USER-ID: 1' 'http://localhost:8080/notes?query=스프링&size=5'

## 3) 노트 상세

GET /notes/{id}

응답 바디
{
  "id": 1,
  "title": "오늘 배운 내용",
  "content": "스프링 DI...",
  "createdAt": "2025-11-08T12:00:00Z",
  "updatedAt": "2025-11-08T12:00:00Z"
}

예시(cURL)
curl -H 'X-USER-ID: 1' http://localhost:8080/notes/1

## 4) 노트 수정

PATCH /notes/{id}

요청 바디(둘 중 하나 이상)

{ "title": "수정된 제목", "content": "내용 업데이트" }

응답: 200 OK (바디 없음 또는 { "id": 1 })

예시(cURL)
curl -X PATCH http://localhost:8080/notes/1 \
  -H 'Content-Type: application/json' -H 'X-USER-ID: 1' \
  -d '{"title":"수정된 제목"}'

## 5) 노트 삭제

DELETE /notes/{id}

응답: 200 OK


## 6) 요약 생성(Mock)
POST /notes/{id}/summary

요청 바디
{ "style": "brief" }  // "brief" | "detailed" (기본 "brief")

응답 바디
{
  "oneLine": "스프링 DI 학습 내용을 한 문장으로 요약.",
  "paragraph": "- DI 핵심 개념 정리...\n- Bean 등록 방식 비교...",
  "createdAt": "2025-11-08T12:05:00Z"
}

예시
curl -X POST http://localhost:8080/notes/1/summary \
  -H 'Content-Type: application/json' -H 'X-USER-ID: 1' \
  -d '{"style":"brief"}'

##  검증/예외 규칙 요약
X-USER-ID 헤더 누락 → 400 INVALID_ARGUMENT
title/content 빈 문자열 → 400 VALIDATION_FAILED
존재하지 않는 id/다른 사용자 노트 접근 → 400 ("note not found" / "forbidden")
요약 생성: 본문 길이 너무 짧으면 → 400 "content too short"
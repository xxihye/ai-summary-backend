# AI Summary Backend
Redis Stream 기반 비동기 요약 처리 시스템 (Spring Boot + MySQL + Redis)
- Java/Spring Boot 기반 텍스트 요약 서비스 백엔드 포트폴리오
- API 서버와 Worker를 분리하고, Redis Streams 기반 비동기 처리, 재시도/상태 전이, 결과 재사용(캐시 히트), 사용자 일일 쿼터를 운영 관점으로 설계


## 시스템 구성 요약
- API 서버: 작업 생성/조회 API 제공
- Worker: Redis Stream에서 작업을 소비해 외부 LLM API(OpenAI 등) 기반의 요약 처리 후 결과 저장
- MySQL: 작업/상태/결과 영속화
- Redis: 작업 큐(Streams), 사용자 쿼터(일일 제한), 캐시


## 핵심 흐름
1) 사용자 요청 → job 생성(DB, PENDING)
2) API가 jobId를 Redis Stream에 발행
3) Worker가 소비 → 요약 처리(LLM) → DB 업데이트(SUCCESS/FAILED)
4) 사용자는 job 상태/결과 조회


## Key Features
- API / Worker 분리 (확장, 장애 격리)
- Redis Streams + Consumer Group 기반 비동기 Job 처리
- 상태 전이 기반 Job 처리 (PENDING/RUNNING/RETRYING/SUCCESS/FAILED)
- 재시도 정책 (retryable error → RETRYING, 최대 시도 초과 → RETRY_EXCEEDED)
- 결과 재사용(캐시 히트): 동일 입력은 요약 결과 재사용
- 사용자 일일 쿼터: Redis 카운터 + 자정 만료 TTL
- Spring Security + JWT 기반 인증


## Tech Stack & Versions

- **Java**: 17
- **Spring Boot**: 3.3.1
- **Spring Framework**: 6.x (Spring Boot 3.3.x 기반)
- **Spring Dependency Management Plugin**: 1.1.5
- **Swagger / OpenAPI** : `springdoc-openapi-starter-webmvc-ui:2.6.0`
- **Spring Security**
- **JWT (JSON Web Token)**
- **Spring Data JPA**
- **Database** : MySQL
- **Redis** : Redis Streams (Consumer Group 기반 비동기 처리)
- **Gradle**
- **Lombok**

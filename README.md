# AI Summary Backend

Redis Stream 기반 비동기 요약 처리 시스템 (Spring Boot + MySQL + Redis)

## 시스템 구성 요약
- API 서버: 작업 생성/조회 API 제공
- Worker: Redis Stream에서 작업을 소비해 요약 처리 후 결과 저장
    - 현재: 더미 요약 생성
    - 예정: 외부 LLM API(OpenAI 등) 연동
- MySQL: 작업/상태/결과 영속화
- Redis: 작업 큐(Streams)
- - 예정 : 사용자 쿼터(일일 제한), 캐시

## 핵심 흐름
1) 사용자 요청 → job 생성(DB, PENDING)
2) API가 jobId를 Redis Stream에 발행
3) Worker가 소비 → 요약 처리(현재 더미 / 예정 LLM) → DB 업데이트(SUCCESS/FAILED)
4) 사용자는 job 상태/결과 조회

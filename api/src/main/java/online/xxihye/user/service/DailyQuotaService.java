package online.xxihye.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.xxihye.common.exception.DailyQuotaExceededException;
import online.xxihye.common.exception.ErrorCode;
import online.xxihye.infra.redis.UserQuotaClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailyQuotaService {

    @Value("${quota.daily-limit}")
    private int dailyLimit;
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

    private final UserQuotaClient quotaClient;


    public void consumeOrThrow(Long userNo) {
        String key = buildKey(userNo);

        long count = quotaClient.increment(key);
        quotaClient.expireIfFirstHit(key, count, ttlUntilEndOfDay());

        if (count > dailyLimit) {
            log.info("quota consumed. userNo={}, count={}", userNo, count);
            throw new DailyQuotaExceededException(ErrorCode.DAILY_QUOTA_EXCEEDED);
        }
    }

    private String buildKey(Long userNo) {
        LocalDate today = LocalDate.now(ZONE_ID);
        return "quota:%d:%s".formatted(userNo, today);
    }

    private Duration ttlUntilEndOfDay() {
        ZonedDateTime now = ZonedDateTime.now(ZONE_ID);
        ZonedDateTime endOfDay = now.toLocalDate().plusDays(1).atStartOfDay(ZONE_ID);
        return Duration.between(now, endOfDay);
    }
}

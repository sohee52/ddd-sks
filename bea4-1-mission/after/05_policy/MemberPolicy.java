package com.back.boundedContext.member.domain;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class MemberPolicy {
    private static final int PASSWORD_CHANGE_DAYS = 90;

    public Duration getNeedToChangePasswordPeriod() {
        return Duration.ofDays(PASSWORD_CHANGE_DAYS);
    }

    public int getNeedToChangePasswordDays() {
        return PASSWORD_CHANGE_DAYS;
    }

    public boolean isNeedToChangePassword(LocalDateTime lastChangedAt) {
        if (lastChangedAt == null) return true;

        return lastChangedAt.plusDays(PASSWORD_CHANGE_DAYS)
                .isBefore(LocalDateTime.now());
    }
}

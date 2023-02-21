package com.dmdev.service;

import com.dmdev.dao.SubscriptionDao;
import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import com.dmdev.integration.IntegrationTestBase;
import com.dmdev.mapper.CreateSubscriptionMapper;
import com.dmdev.validator.CreateSubscriptionValidator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class SubscriptionServiceIT extends IntegrationTestBase {

    private SubscriptionDao subscriptionDao;
    private SubscriptionService subscriptionService;

    @BeforeEach
    void init() {
        subscriptionDao = SubscriptionDao.getInstance();
        subscriptionService = new SubscriptionService(
                subscriptionDao,
                CreateSubscriptionMapper.getInstance(),
                CreateSubscriptionValidator.getInstance(),
                Clock.fixed(Instant.now(), ZoneId.systemDefault())
        );
    }

    @Test
    void upsert() {
        var s1 = subscriptionDao.insert(getSubscription(1));
        var s2 = subscriptionDao.insert(getSubscription(2));
        var s3 = subscriptionDao.insert(getSubscription(3));

        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .provider(Provider.APPLE.name())
                .userId(3)
                .name("testName")
                .expirationDate(Instant.parse("2024-02-03T11:25:30.00Z"))
                .build();

        var actualResult = subscriptionService.upsert(dto);

        assertThat(actualResult).isEqualTo(s3);
    }

    @Test
    void cancel() {
        var subscription = getSubscription(1);
        subscriptionDao.insert(subscription);

        subscriptionService.cancel(subscription.getId());

        assertThat(subscriptionDao.findById(subscription.getId()).get().getStatus()).isEqualTo(Status.CANCELED);
    }

    private Subscription getSubscription(Integer userId) {
        return Subscription.builder()
                .userId(userId)
                .name("testName")
                .provider(Provider.APPLE)
                .status(Status.ACTIVE)
                .expirationDate(Instant.parse("2024-02-03T11:25:30.00Z"))
                .build();
    }
}
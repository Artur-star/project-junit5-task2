package com.dmdev.dao;

import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import com.dmdev.integration.IntegrationTestBase;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class SubscriptionDaoIT extends IntegrationTestBase {

    private final SubscriptionDao subscriptionDao = SubscriptionDao.getInstance();

    @Test
    void findAll() {
        var s1 = subscriptionDao.insert(getSubscription(1));
        var s2 = subscriptionDao.insert(getSubscription(2));
        var s3 = subscriptionDao.insert(getSubscription(3));

        var actualResult = subscriptionDao.findAll();

        assertThat(actualResult).hasSize(3);
        assertThat(actualResult).contains(s1, s2, s3);
    }

    @Test
    void findById() {
        var s1 = subscriptionDao.insert(getSubscription(1));

        var actualResult = subscriptionDao.findById(s1.getId());

        assertThat(actualResult).isPresent();
        assertThat(actualResult).isEqualTo(Optional.of(s1));
    }

    @Test
    void deleteExistingEntity() {
        var s = subscriptionDao.insert(getSubscription(1));

        var actualResult = subscriptionDao.delete(s.getId());

        assertThat(actualResult).isTrue();
    }

    @Test
    void deleteNotExistingEntity() {
        var s = subscriptionDao.insert(getSubscription(1));

        var actualResult = subscriptionDao.delete(2);

        assertThat(actualResult).isFalse();
    }

    @Test
    void update() {
        var subscription = getSubscription(1);
        subscriptionDao.insert(subscription);
        subscription.setName("updateName");

        var actualResult = subscriptionDao.update(subscription);
        var byId = subscriptionDao.findById(subscription.getId()).get();

        assertThat(actualResult.getId()).isEqualTo(byId.getId());
    }

    @Test
    void insert() {
        var subscription = getSubscription(1);

        var actualResult = subscriptionDao.insert(subscription);

        assertNotNull(actualResult.getId());
    }

    @Test
    void findByUserId() {
        var s1 = subscriptionDao.insert(getSubscription(1));
        var s2 = subscriptionDao.insert(getSubscription(2));
        var s3 = subscriptionDao.insert(getSubscription(3));

        var actualResult = subscriptionDao.findByUserId(s2.getUserId());

        assertThat(actualResult).contains(s2);
        assertThat(actualResult).doesNotContain(s1, s3);
    }

    @Test
    void shouldNotFindByUserIdIfSubscriptionDoesNotExist() {
        var s1 = subscriptionDao.insert(getSubscription(1));
        var s2 = subscriptionDao.insert(getSubscription(2));
        var s3 = subscriptionDao.insert(getSubscription(3));

        var actualResult = subscriptionDao.findByUserId(4);

        assertThat(actualResult).doesNotContain(s1, s2, s3);
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
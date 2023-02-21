package com.dmdev.service;

import com.dmdev.dao.SubscriptionDao;
import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import com.dmdev.exception.SubscriptionException;
import com.dmdev.mapper.CreateSubscriptionMapper;
import com.dmdev.validator.CreateSubscriptionValidator;
import com.dmdev.validator.ValidationResult;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionDao subscriptionDao;
    @Mock
    private CreateSubscriptionMapper createSubscriptionMapper;
    @Mock
    private CreateSubscriptionValidator createSubscriptionValidator;

    private SubscriptionService subscriptionService;

    private static final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

    @BeforeEach
    void setup() {
        subscriptionService = new SubscriptionService(subscriptionDao, createSubscriptionMapper, createSubscriptionValidator, clock);
    }


    @Test
    void upsert() {
        CreateSubscriptionDto createSubscriptionDto = getCreateSubscriptionDto();
        Subscription subscription = getSubscription();

        when(createSubscriptionValidator.validate(createSubscriptionDto)).thenReturn(new ValidationResult());
        when(subscriptionDao.findByUserId(createSubscriptionDto.getUserId())).thenReturn(Collections.emptyList());
        when(createSubscriptionMapper.map(createSubscriptionDto)).thenReturn(subscription);
        when(subscriptionDao.upsert(any())).thenReturn(subscription);

        var actualResult = subscriptionService.upsert(createSubscriptionDto);

        assertNotNull(actualResult);
        assertThat(actualResult).isEqualTo(subscription);
        verify(subscriptionDao).findByUserId(createSubscriptionDto.getUserId());
        verify(subscriptionDao).upsert(any());
    }

    @Test
    void cancel() {
        var subscription = getSubscription();
        when(subscriptionDao.findById(subscription.getId())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> subscriptionService.cancel(subscription.getId()));
        verifyNoMoreInteractions(subscriptionDao);
    }

    @Test
    void whenStatusActiveShouldThrowException() {
        var subscription = Subscription.builder().status(Status.EXPIRED).build();

        when(subscriptionDao.findById(1)).thenReturn(Optional.of(subscription));

        var exception = assertThrows(SubscriptionException.class, () -> subscriptionService.cancel(1));
        assertEquals("Only active subscription 1 can be canceled", exception.getMessage());
    }

    @Nested
    class TestExpire {
        private static final int SUB_ID = 1;

        @Test
        void whenNotFoundByIdShouldThrowException() {
            doReturn(Optional.empty()).when(subscriptionDao).findById(SUB_ID);
            assertThrows(IllegalArgumentException.class, () -> subscriptionService.expire(SUB_ID));
            verify(subscriptionDao).findById(SUB_ID);
            verifyNoMoreInteractions(subscriptionDao);
        }

        @Test
        void whenStatusExpiredShouldThrowException() {
            var subscription = Subscription.builder().status(Status.EXPIRED).build();
            doReturn(Optional.of(subscription)).when(subscriptionDao).findById(SUB_ID);
            var exception = assertThrows(SubscriptionException.class, () -> subscriptionService.expire(SUB_ID));
            assertEquals("Subscription 1 has already expired", exception.getMessage());
        }

        @Nested
        class WhenActive {
            private Subscription subscription;

            @BeforeEach
            void setUp() {
                subscription = mock(Subscription.class);
                when(subscriptionDao.findById(SUB_ID)).thenReturn(Optional.of(subscription));
                when(subscription.getStatus()).thenReturn(Status.ACTIVE);
            }

            @Test
            void shouldSetExpirationDate() {
                subscriptionService.expire(SUB_ID);
                verify(subscription).setExpirationDate(clock.instant());
            }

            @Test
            void shouldSetStatusExpired() {
                subscriptionService.expire(SUB_ID);
                verify(subscription).setStatus(Status.EXPIRED);
            }

            @Test
            void shouldUpdateSubscription() {
                subscriptionService.expire(SUB_ID);
                verify(subscriptionDao).update(subscription);
            }
        }
    }

    private CreateSubscriptionDto getCreateSubscriptionDto() {
        return CreateSubscriptionDto.builder()
                .expirationDate(Instant.parse("2024-02-03T11:25:30.00Z"))
                .name("testName")
                .provider(Provider.APPLE.name())
                .userId(1)
                .build();
    }
    private Subscription getSubscription() {
        return Subscription.builder()
                .id(99)
                .userId(1)
                .name("testName")
                .provider(Provider.APPLE)
                .status(Status.ACTIVE)
                .expirationDate(Instant.parse("2024-02-03T11:25:30.00Z"))
                .build();
    }
}
package com.dmdev.mapper;

import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CreateSubscriptionMapperTest {

    private final CreateSubscriptionMapper mapper = CreateSubscriptionMapper.getInstance();

    @Test
    void map() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .expirationDate(Instant.parse("2024-02-03T11:25:30.00Z"))
                .name("testName")
                .provider(Provider.APPLE.name())
                .userId(1)
                .build();

        var actualResult = mapper.map(dto);
        var expectedResult = Subscription.builder()
                .expirationDate(Instant.parse("2024-02-03T11:25:30.00Z"))
                .name("testName")
                .provider(Provider.APPLE)
                .status(Status.ACTIVE)
                .userId(1)
                .build();

        assertThat(actualResult).isEqualTo(expectedResult);
    }
}
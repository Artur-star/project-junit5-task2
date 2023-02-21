package com.dmdev.validator;

import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CreateSubscriptionValidatorTest {

    private final CreateSubscriptionValidator validator = CreateSubscriptionValidator.getInstance();

    @Test
    void shouldPassValidation() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .expirationDate(Instant.parse("2024-02-03T11:25:30.00Z"))
                .name("testName")
                .provider(Provider.APPLE.name())
                .userId(1)
                .build();

        var actualResult = validator.validate(dto);

        assertFalse(actualResult.hasErrors());
    }

    @Test
    void invalidExpirationDate() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .expirationDate(Instant.parse("2017-02-03T11:25:30.00Z"))
                .name("testName")
                .provider(Provider.APPLE.name())
                .userId(1)
                .build();

        var actualResult = validator.validate(dto);

        assertThat(actualResult.getErrors()).hasSize(1);
        assertThat(actualResult.getErrors().get(0).getCode()).isEqualTo(103);
    }

    @Test
    void invalidProvider() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .expirationDate(Instant.parse("2024-02-03T11:25:30.00Z"))
                .name("testName")
                .provider("invalidProvider")
                .userId(1)
                .build();

        var actualResult = validator.validate(dto);

        assertThat(actualResult.getErrors()).hasSize(1);
        assertThat(actualResult.getErrors().get(0).getCode()).isEqualTo(102);
    }

    @Test
    void invalidName() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .expirationDate(Instant.parse("2024-02-03T11:25:30.00Z"))
                .name(null)
                .provider(Provider.APPLE.name())
                .userId(1)
                .build();

        var actualResult = validator.validate(dto);

        assertThat(actualResult.getErrors()).hasSize(1);
        assertThat(actualResult.getErrors().get(0).getCode()).isEqualTo(101);
    }

    @Test
    void invalidUserId() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .expirationDate(Instant.parse("2024-02-03T11:25:30.00Z"))
                .name("testName")
                .provider(Provider.APPLE.name())
                .userId(null)
                .build();

        var actualResult = validator.validate(dto);

        assertThat(actualResult.getErrors()).hasSize(1);
        assertThat(actualResult.getErrors().get(0).getCode()).isEqualTo(100);
    }

    @Test
    void invalidExpirationDateNameProviderUserId() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .expirationDate(Instant.parse("2000-02-03T11:25:30.00Z"))
                .name(null)
                .provider("fakeProvider")
                .userId(null)
                .build();

        var actualResult = validator.validate(dto);

        var errorCodes = actualResult.getErrors().stream()
                .map(Error::getCode)
                .toList();
        assertThat(errorCodes).contains(100, 101, 102, 103);
    }
}
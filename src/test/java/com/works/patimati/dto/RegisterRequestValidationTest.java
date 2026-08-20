package com.works.patimati.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterRequestValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        // DTO üzerindeki validation anotasyonlarını çalıştıracak doğrulayıcı hazırlanır.
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void shouldAcceptPasswordThatMeetsRegisterPolicy() {
        RegisterRequest request = validRequestWithPassword("Guvenli123");

        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Kisa1",
            "buyukharfyok1",
            "KUCUKHARFYOK1",
            "RakamIcermez",
            "YirmiKarakterdenCokUzun123"
    })
    void shouldRejectPasswordThatDoesNotMeetRegisterPolicy(String password) {
        RegisterRequest request = validRequestWithPassword(password);

        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validate(request);

        // Test edilen hatanın özellikle password alanından geldiği doğrulanır.
        boolean hasPasswordViolation = violations.stream()
                .anyMatch(violation ->
                        "password".equals(
                                violation.getPropertyPath().toString()
                        )
                );

        assertThat(hasPasswordViolation).isTrue();
    }

    private RegisterRequest validRequestWithPassword(String password) {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Zahid");
        request.setLastName("Yavas");
        request.setEmail("zahid@example.com");
        request.setPhone("05551234567");
        request.setPassword(password);
        return request;
    }
}
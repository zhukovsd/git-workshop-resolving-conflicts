package com.itmentorcommunityplatform.profileservice.validator.impl;

import com.itmentorcommunityplatform.profileservice.domain.type.ProfileDetailType;
import com.itmentorcommunityplatform.profileservice.exception.ValidationException;
import com.itmentorcommunityplatform.profileservice.validator.ProfileDetailValidator;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

import static com.itmentorcommunityplatform.profileservice.domain.type.ProfileDetailType.TELEGRAM_URL;

@Component
public class TelegramUrlValidator implements ProfileDetailValidator {

    private static final Pattern TELEGRAM_PATTERN = Pattern.compile("^https://t\\.me/[^\\s/]+$");

    @Override
    public ProfileDetailType getSupportedProfileDetailType() {
        return TELEGRAM_URL;
    }

    @Override
    public void validate(String value) {
        if (value == null || !TELEGRAM_PATTERN.matcher(value).matches()) {
            throw new ValidationException("Telegram profile url incorrect");
        }
    }
}

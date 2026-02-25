package com.itmentorcommunityplatform.profileservice.validator.base;

import com.itmentorcommunityplatform.profileservice.exception.ValidationException;
import org.springframework.stereotype.Component;

/**
 * <p>Базовый валидатор.</p>
 * <p>
 * Основное назначение: <br>
 * - выполнение общих для всех detailName проверок
 * (например, ограничение максимальной длины строки).<br>
 * Если требуется добавить общие проверки для всех details,
 * их следует разместить здесь.
 */
@Component
public class BaseProfileDetailValidator {

    public void validate(String detailName, String value) {

        if (value.length() > 255) {
            throw new ValidationException("Value of '" + detailName + "' exceeds max length 255");
        }
    }
}

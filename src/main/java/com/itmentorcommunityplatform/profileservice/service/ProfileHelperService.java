package com.itmentorcommunityplatform.profileservice.service;

import com.itmentorcommunityplatform.profileservice.domain.ProfileDetail;
import com.itmentorcommunityplatform.profileservice.domain.type.ProfileDetailType;
import com.itmentorcommunityplatform.profileservice.exception.EmptyProfileDetailsException;
import com.itmentorcommunityplatform.profileservice.exception.InvalidProfileDetailsException;
import com.itmentorcommunityplatform.profileservice.validator.base.BaseProfileDetailValidator;
import com.itmentorcommunityplatform.profileservice.validator.registry.ProfileDetailValidatorRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileHelperService {

    private final BaseProfileDetailValidator baseDetailValidator;
    private final ProfileDetailValidatorRegistry detailValidatorRegistry;

    public static Set<ProfileDetail> mergeProfileDetails(
            Set<ProfileDetail> existingDetails,
            Map<String, String> newDetailsMap
    ) {
        Map<String, String> mergedMap = new HashMap<>();

        existingDetails.forEach(detail ->
                mergedMap.put(detail.getDetailName(), detail.getDetailValue()));

        mergedMap.putAll(newDetailsMap);

        return mergedMap.entrySet().stream()
                .map(e -> new ProfileDetail(e.getKey(), e.getValue()))
                .collect(Collectors.toSet());
    }

    public void validateDetails(Map<String, String> details) {
        if (details == null || details.isEmpty()) {
            throw new EmptyProfileDetailsException("Profile details should not be empty");
        }

        details.forEach((detailName, detailValue) -> {
            ProfileDetailType type = ProfileDetailType.fromName(detailName)
                    .orElseThrow(() -> {
                        log.warn("Unknown detail name: {}", detailName);
                        return new InvalidProfileDetailsException("Unknown detail name");
                    });

            baseDetailValidator.validate(detailName, detailValue);

            detailValidatorRegistry.getSpecificValidator(type)
                    .ifPresent(v -> v.validate(detailValue));
        });
    }
}

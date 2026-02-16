package com.itmentorcommunityplatform.profileservice.service;

import com.itmentorcommunityplatform.profileservice.domain.Profile;
import com.itmentorcommunityplatform.profileservice.domain.ProfileDetail;
import com.itmentorcommunityplatform.profileservice.domain.type.ProfileDetailType;
import com.itmentorcommunityplatform.profileservice.dto.event.ProjectCreatedEvent;
import com.itmentorcommunityplatform.profileservice.dto.event.UserCreatedEvent;
import com.itmentorcommunityplatform.profileservice.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.itmentorcommunityplatform.profileservice.service.ProfileHelperService.mergeProfileDetails;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;

    @Transactional
    public void createOrUpdateProfile(UserCreatedEvent event) {
        if (event == null || event.getTelegramUserId() == null) {
            log.warn("Received empty event or null telegramUserId. Skipping.");
            return;
        }

        Long telegramUserId = event.getTelegramUserId();
        log.info("Attempting to create profile for telegramUserId: {}", telegramUserId);

        Map<String, String> details = new HashMap<>();

        if (event.getFirstName() != null && !event.getFirstName().isBlank()) {
            details.put(ProfileDetailType.FIRST_NAME.getDetailName(), event.getFirstName());
        }
        if (event.getLastName() != null && !event.getLastName().isBlank()) {
            details.put(ProfileDetailType.LAST_NAME.getDetailName(), event.getLastName());
        }

        Optional<Profile> maybeProfile = profileRepository.findByTelegramUserId(telegramUserId);

        if (maybeProfile.isPresent()) {
            log.info("Profile for telegramUserId: {} exists. Updating details.", telegramUserId);
            Profile existingProfile = maybeProfile.get();

            Set<ProfileDetail> profileDetails = mergeProfileDetails(existingProfile.getDetails(), details);
            existingProfile.setDetails(profileDetails);

            profileRepository.save(existingProfile);
        } else {
            log.info("Creating new profile for telegramUserId: {}", telegramUserId);

            Set<ProfileDetail> newProfileDetails = details.entrySet().stream()
                    .map(e -> new ProfileDetail(e.getKey(), e.getValue()))
                    .collect(Collectors.toSet());

            Profile newProfile = Profile.builder()
                    .id(null)
                    .telegramUserId(telegramUserId)
                    .details(newProfileDetails)
                    .build();

            profileRepository.save(newProfile);
            log.info("Successfully created profile with telegramUserId: {}", telegramUserId);
        }
    }

    @Transactional(readOnly = true)
    public Optional<Profile> getProfileForEvent(ProjectCreatedEvent event) {
        if (event == null || event.getAuthorTelegramUserId() == null) {
            log.warn("Received empty event or null author ID");
            return Optional.empty();
        }

        Long telegramUserId = event.getAuthorTelegramUserId();

        Optional<Profile> maybeProfile = profileRepository.findByTelegramUserId(telegramUserId);

        if (maybeProfile.isEmpty()) {
            log.warn("Profile not found for telegramUserId {}", telegramUserId);
            return Optional.empty();
        }

        return maybeProfile;
    }

    @Transactional(readOnly = true)
    public Profile getProfileByTelegramIdOrThrow(Long telegramUserId) {
        return profileRepository.findByTelegramUserId(telegramUserId)
                .orElseThrow(() -> {
                    log.warn("Profile not found for telegramUserId: {}", telegramUserId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Profile with Telegram-User-Id %s does not exist".formatted(telegramUserId));
                });
    }
}
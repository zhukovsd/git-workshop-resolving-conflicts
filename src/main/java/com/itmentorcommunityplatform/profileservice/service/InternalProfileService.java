package com.itmentorcommunityplatform.profileservice.service;

import com.itmentorcommunityplatform.profileservice.domain.Profile;
import com.itmentorcommunityplatform.profileservice.domain.ProfileDetail;
import com.itmentorcommunityplatform.profileservice.dto.request.ProfileUpsertInternalRequestDto;
import com.itmentorcommunityplatform.profileservice.dto.response.ProfileWithTelegramIdResponseDto;
import com.itmentorcommunityplatform.profileservice.exception.MissingProfileDetailsException;
import com.itmentorcommunityplatform.profileservice.exception.MissingTelegramUserIdException;
import com.itmentorcommunityplatform.profileservice.exception.ProfileNotFoundException;
import com.itmentorcommunityplatform.profileservice.mapper.ProfileMapper;
import com.itmentorcommunityplatform.profileservice.repository.ProfileRepository;
import com.itmentorcommunityplatform.profileservice.validator.impl.GithubProfileUrlValidator;
import com.itmentorcommunityplatform.profileservice.validator.impl.TelegramProfileUrlValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.itmentorcommunityplatform.profileservice.service.ProfileHelperService.mergeProfileDetails;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternalProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileHelperService  profileHelperService;
    private final GithubProfileUrlValidator githubProfileUrlValidator;
    private final ProfileMapper profileMapper;
    private final TelegramProfileUrlValidator telegramProfileUrlValidator;

    @Transactional
    public boolean upsertProfile(ProfileUpsertInternalRequestDto dto) {

        Long telegramUserId = dto.telegramUserId();
        if (telegramUserId == null) {
            throw new MissingTelegramUserIdException("'telegram_user_id' must be provided");
        }

        if (dto.details() == null) {
            throw new MissingProfileDetailsException("'details' must be provided");
        }
        Map<String, String> newDetailsMap = dto.details().getMap();
        profileHelperService.validateDetails(newDetailsMap);

        Optional<Profile> foundProfile = profileRepository.findByTelegramUserId(telegramUserId);
        boolean isNewProfile = foundProfile.isEmpty();

        Set<ProfileDetail> existingDetails = foundProfile.map(Profile::getDetails).orElse(Collections.emptySet());

        Set<ProfileDetail> mergedDetails = mergeProfileDetails(existingDetails, newDetailsMap);

        Profile profile = Profile.builder()
                .id(foundProfile.map(Profile::getId).orElse(null))
                .telegramUserId(telegramUserId)
                .details(mergedDetails)
                .build();

        profileRepository.save(profile);
        return isNewProfile;
    }

    public ProfileWithTelegramIdResponseDto getProfileByGitHubUrl(String gitHubUrl) {

        githubProfileUrlValidator.validate(gitHubUrl);

        log.info("Searching profile by GitHub URL: {}", gitHubUrl);

        Profile profile = profileRepository.findProfileByGitHubUrl(gitHubUrl)
                .orElseThrow(() -> {
                    log.warn("Profile with URL: {} not found", gitHubUrl);
                    return new ProfileNotFoundException("Profile not found");
                });


        return profileMapper.mapToProfileWithTelegramIdDto(profile.getTelegramUserId(), profile.getDetails());
    }

    public ProfileWithTelegramIdResponseDto getProfileByTgUrl(String tgUrl) {

        telegramProfileUrlValidator.validate(tgUrl);

        log.info("Searching profile by Telegram URL: {}", tgUrl);

        Profile profile = profileRepository.findProfileByTgUrl(tgUrl)
                .orElseThrow(() -> {
                    log.warn("Profile with URL: {} not found", tgUrl);
                    return new ProfileNotFoundException("Profile not found");
                });

        return profileMapper.mapToProfileWithTelegramIdDto(profile.getTelegramUserId(), profile.getDetails());
    }
}

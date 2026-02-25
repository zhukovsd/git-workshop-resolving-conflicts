package com.itmentorcommunityplatform.profileservice.service;


import com.itmentorcommunityplatform.profileservice.domain.Profile;
import com.itmentorcommunityplatform.profileservice.domain.ProfileDetail;
import com.itmentorcommunityplatform.profileservice.domain.type.ProfileDetailType;
import com.itmentorcommunityplatform.profileservice.exception.ProfileNotFoundException;
import com.itmentorcommunityplatform.profileservice.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileDetailService {

    private final ProfileRepository profileRepository;

    public void upsertGithubProfileUrl(Long telegramUserId, String githubUrl) {
        Profile profile = profileRepository.findByTelegramUserId(telegramUserId)
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found"));
        Set<ProfileDetail> profileDetails = profile.getDetails();

        boolean githubAlreadyUpToDate = profileDetails.stream()
                .map(ProfileDetail::getDetailName)
                .anyMatch(ProfileDetailType.GITHUB_PROFILE_URL.getDetailName()::equals);

        if (githubAlreadyUpToDate) {
            log.info("Github profile for profile with telegramId #{}, already exists", telegramUserId);
            return;
        }

        profileDetails.add(new ProfileDetail(ProfileDetailType.GITHUB_PROFILE_URL.getDetailName(), githubUrl.replaceFirst("/+$", "")
                .replaceFirst("/[^/]+$", "")));

        profile.setDetails(profileDetails);
        profileRepository.save(profile);
        log.info("Github profile saved");

    }

}

package com.itmentorcommunityplatform.profileservice.controller;

import com.itmentorcommunityplatform.profileservice.docs.GetCurrentProfileDocs;
import com.itmentorcommunityplatform.profileservice.docs.GetUserProfileByIdDocs;
import com.itmentorcommunityplatform.profileservice.docs.UpdateCurrentProfileDocs;
import com.itmentorcommunityplatform.profileservice.dto.request.ProfileUpdateRequestDto;
import com.itmentorcommunityplatform.profileservice.dto.response.ProfileWithAchievementsResponseDto;
import com.itmentorcommunityplatform.profileservice.service.CurrentProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final CurrentProfileService currentProfileService;

    @GetMapping
    @GetCurrentProfileDocs
    public ResponseEntity<ProfileWithAchievementsResponseDto> getCurrentProfile(
            @RequestHeader("X-Telegram-User-Id") Long telegramUserId
    ) {
        ProfileWithAchievementsResponseDto profileWithAchievementsResponseDto = currentProfileService.getCurrentUserProfile(telegramUserId);
        return ResponseEntity.ok(profileWithAchievementsResponseDto);
    }

    @PatchMapping
    @UpdateCurrentProfileDocs
    public ResponseEntity<ProfileWithAchievementsResponseDto> updateCurrentProfile(
            @RequestHeader("X-Telegram-User-Id") Long telegramUserId,
            @RequestHeader("X-Telegram-Username") Optional<String> telegramUsername,
            @RequestBody ProfileUpdateRequestDto dto) {


        if (dto.getDetails() != null && dto.getDetails().containsKey("telegram_url")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "invalid telegram_url field in the body");
        }

        ProfileWithAchievementsResponseDto response = currentProfileService.updateCurrentProfile(telegramUserId, dto, telegramUsername.orElse(null));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @GetUserProfileByIdDocs
    public ResponseEntity<ProfileWithAchievementsResponseDto> getUserProfile(@PathVariable("id") Long profileId) {
        ProfileWithAchievementsResponseDto userProfile = currentProfileService.getUserProfile(profileId);
        return ResponseEntity.ok(userProfile);
    }
}
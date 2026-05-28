package com.ysocial.org.ysocialsite.controller;


import com.ysocial.org.ysocialsite.dto.ProfileDto;
import com.ysocial.org.ysocialsite.dto.ProfileShortDto;
import com.ysocial.org.ysocialsite.dto.request.UpdateProfileRequest;
import com.ysocial.org.ysocialsite.security.CustomUserDetails;
import com.ysocial.org.ysocialsite.service.ProfileService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;




@Controller
@RequestMapping("/profiles")
@Slf4j
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }


    @GetMapping
    public String getMyProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model) {
        ProfileDto profileDto = profileService.getMyProfile(userDetails);

        model.addAttribute("profile", profileDto);
        model.addAttribute("isOwnProfile", true);
        
        // Передаем аватарку специально для шапки сайта
        model.addAttribute("headerAvatarUrl", profileDto.avatarUrl()); 
        
        return "profile";
    }

    @GetMapping("/{id}")
    public String getProfileById(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @PathVariable Long id, Model model) {
                                    
        //Если пытаемся зайти на свой же профиль по ID -> редирект на чистый /profiles
        if (userDetails.getId().equals(id)) {
            return "redirect:/profiles";
        }

        ProfileDto profileDto = profileService.getProfileById(userDetails, id);
        
        //Получаем свой профиль, чтобы достать СВОЮ аватарку для шапки
        ProfileDto myProfile = profileService.getMyProfile(userDetails);

        model.addAttribute("status", profileDto.friendStatus());
        model.addAttribute("userId", profileDto.id());
        model.addAttribute("profile", profileDto);
        model.addAttribute("isOwnProfile", profileDto.isOwnProfile());
        
        // Передаем СВОЮ аватарку, а не аватарку просматриваемого пользователя
        model.addAttribute("headerAvatarUrl", myProfile.avatarUrl());

        return "profile";
    }

    @GetMapping("/edit-form")
    public String getEditProfileForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        ProfileDto profileDto = profileService.getMyProfile(userDetails);
        model.addAttribute("profile", profileDto);
        return "profile :: edit-modal-fragment";
    }

    @PutMapping
    public String updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute UpdateProfileRequest request,
            BindingResult bindingResult,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            Model model,
            HttpServletResponse response
    ) {
        if (bindingResult.hasErrors()) {
            response.setHeader("HX-Retarget", "body");
            response.setHeader("HX-Reswap", "beforeend");

            String errorMsg = bindingResult.getFieldErrors().getFirst().getDefaultMessage();
            model.addAttribute("message", errorMsg);

            return "fragments :: error-toast";
        }
        profileService.updateProfile(userDetails, request, avatar);
        response.setHeader("HX-Redirect", "/profiles");
        return null;
    }

    @GetMapping("/create-form")
    public String getCreatePostForm(@AuthenticationPrincipal CustomUserDetails userDetails,
                                    Model model) {
        ProfileDto profileDto = profileService.getMyProfile(userDetails);
        model.addAttribute("profile", profileDto);
        return "profile :: create-post-modal-fragment";
    }
    @GetMapping("/people")
    public String getPeople(@AuthenticationPrincipal CustomUserDetails userDetails,
                                Model model
    ) {
        ProfileDto profileDto = profileService.getMyProfile(userDetails);
        model.addAttribute("profile", profileDto);
        return "people";
    }


    @GetMapping("/people/find")
    public String findProfileByParams(@AuthenticationPrincipal  CustomUserDetails userDetails,
                                    @RequestParam(required = false) String name,
                                    @RequestParam(required = false) String city,
                                    @RequestParam(required = false) LocalDate birthDate,
                                    Model model
    ) {
        List<ProfileShortDto> results = profileService
                            .findProfileByParams(userDetails, name, city, birthDate); 
        model.addAttribute("results", results);
        model.addAttribute("profile", profileService.getMyProfile(userDetails));

        return "people";
    }
}

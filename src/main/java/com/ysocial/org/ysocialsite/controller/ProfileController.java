package com.ysocial.org.ysocialsite.controller;


import com.ysocial.org.ysocialsite.dto.ProfileDto;
import com.ysocial.org.ysocialsite.dto.request.UpdateProfileRequest;
import com.ysocial.org.ysocialsite.security.CustomUserDetails;
import com.ysocial.org.ysocialsite.service.ProfileService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
                               Model model
    ) {
        ProfileDto profileDto = profileService.getMyProfile(userDetails);

        model.addAttribute("profile", profileDto);
        model.addAttribute("isOwnProfile", true);
        return "html/profile";
    }

    @GetMapping("/{id}")
    public String getProfileById(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @PathVariable Long id, Model model) {
        ProfileDto profileDto = profileService.getProfileById(userDetails, id);
        model.addAttribute("status", profileDto.friendStatus());
        model.addAttribute("userId", profileDto.id());
        model.addAttribute("profile", profileDto);
        
        model.addAttribute("isOwnProfile", profileDto.isOwnProfile());
        return "html/profile";
    }

    @GetMapping("/edit-form")
    public String getEditProfileForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        ProfileDto profileDto = profileService.getMyProfile(userDetails);
        model.addAttribute("profile", profileDto);
        return "html/profile :: edit-modal-fragment";
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

            return "html/fragments :: error-toast";
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
        return "html/profile :: create-post-modal-fragment";
    }
}

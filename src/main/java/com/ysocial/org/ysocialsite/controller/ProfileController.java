package com.ysocial.org.ysocialsite.controller;


import com.ysocial.org.ysocialsite.dto.ProfileDto;
import com.ysocial.org.ysocialsite.dto.ProfileShortDto;
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


import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@Controller
@RequestMapping("/profiles")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final ProfileService profileService;


    @GetMapping
    public String getMyProfile(@AuthenticationPrincipal UserDetails userDetails,
                               Model model
    ) {
        ProfileDto profileDto = profileService.getMyProfile(userDetails);

        model.addAttribute("profile", profileDto);
        model.addAttribute("isOwnProfile", true);
        return "html/profile";
    }

    @GetMapping("/{id}")
    public String getProfileById(@AuthenticationPrincipal UserDetails userDetails,
                                @PathVariable UUID id, Model model) {
        ProfileDto profileDto = profileService.getProfileById(userDetails, id);
        model.addAttribute("status", profileDto.friendStatus());
        model.addAttribute("userId", profileDto.id());
        model.addAttribute("profile", profileDto);
        
        model.addAttribute("isOwnProfile", profileDto.isOwnProfile());
        return "html/profile";
    }
}

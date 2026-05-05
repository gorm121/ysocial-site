package com.ysocial.org.ysocialsite.controller;

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.ysocial.org.ysocialsite.service.ProfileService;
import com.ysocial.org.ysocialsite.dto.FriendDto;
import com.ysocial.org.ysocialsite.security.CustomUserDetails;
import com.ysocial.org.ysocialsite.service.FriendsService;


import java.util.List;

@Controller
@RequestMapping("/friends")
@Slf4j
public class FriendsController {
    private final FriendsService friendsService;
    private final ProfileService profileService;


    public FriendsController(ProfileService profileService, FriendsService friendsService) {
        this.friendsService = friendsService;
        this.profileService = profileService;
    }


    @GetMapping
    public String getMyFriendsAndRequests(@AuthenticationPrincipal CustomUserDetails userDetails,
                                          Model model)
    {
        List<FriendDto> friends = friendsService.getFriends(userDetails);
        List<FriendDto> requests = friendsService.getRequests(userDetails);
        model.addAttribute("requests", requests);
        model.addAttribute("friends", friends);
        model.addAttribute("profile", profileService.getMyProfile(userDetails));
                                        
        return "html/friends";
    }



}
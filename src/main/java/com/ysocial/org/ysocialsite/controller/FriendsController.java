package com.ysocial.org.ysocialsite.controller;

import com.ysocial.org.ysocialsite.enums.FriendshipStatus;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
                                        
        return "friends";
    }

    @PostMapping("/request/{userId}/send")
    public String sendFriendRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId,
            Model model
    ) {
        friendsService.sendRequest(userDetails, userId);
        model.addAttribute("status", FriendshipStatus.PENDING);
        model.addAttribute("userId", userId);
        model.addAttribute("isOwnProfile", false);
        return "profile :: friend-button";
    }

    @PostMapping("/request/{userId}/accept")
    public String acceptFriendRequest(@AuthenticationPrincipal CustomUserDetails userDetails,
                                      @PathVariable Long userId,
                                      Model model){
        friendsService.acceptRequest(userDetails, userId);
        model.addAttribute("status", FriendshipStatus.ACCEPTED);
        model.addAttribute("userId", userId);
        model.addAttribute("isOwnProfile", false);
        return "profile :: friend-button";
    }

    @DeleteMapping("/{userId}/remove")
    public String deleteFriend( @AuthenticationPrincipal CustomUserDetails userDetails,
                                @PathVariable Long userId,
                                Model model)
    {
        friendsService.deleteFriend(userDetails, userId);
        model.addAttribute("status", null);
        model.addAttribute("userId", userId);
        model.addAttribute("isOwnProfile", false);
        return "profile :: friend-button";
    }

    @DeleteMapping("/request/{userId}/reject")
    public String rejectFriend(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @PathVariable Long userId,
                               Model model)
    {
        friendsService.rejectFriend(userDetails, userId);
        model.addAttribute("status", null);
        model.addAttribute("userId", userId);
        model.addAttribute("isOwnProfile", false);

        return "profile :: friend-button";
    }
}
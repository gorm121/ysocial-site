package com.ysocial.org.ysocialsite.controller;

import com.ysocial.org.ysocialsite.dto.response.PostResponse;
import com.ysocial.org.ysocialsite.enums.ReactionType;
import com.ysocial.org.ysocialsite.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/posts")
@Slf4j
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/feed")
    public String getFeed(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(name = "page", defaultValue = "0") int page, 
            @RequestParam(name = "size", defaultValue = "10") int size, 
            @RequestHeader(value = "HX-Request", required = false, defaultValue = "false") boolean isHtmxRequest,
            Model model
    ) {
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        String avatarUrl = "/images/default-avatar.png";
        model.addAttribute("avatarUrl", avatarUrl);

        Page<PostResponse> postPage = postService.getFeed(userDetails, page, size);
        model.addAttribute("posts", postPage);

        if (isHtmxRequest) {
            return "html/feed :: post-chunk";
        }
        return "html/feed";
    }

    @GetMapping
    public String getFeedUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("userId") Long userId,
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            Model model
    ) {
        model.addAttribute("page", page);
        model.addAttribute("size", size);

        Page<PostResponse> postPage = postService.getUserFeed(userDetails, userId, page, size);
//        ProfileDto profileDto = profileService.getProfileById(userDetails, userId);
        model.addAttribute("posts", postPage);
        model.addAttribute("profileUserId", userId);
        model.addAttribute("isOwnProfile", true); // true временно

        return "html/profile :: post-chunk";
    }
    
    @PostMapping("/{postId}/reaction")
    public String reactToPost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long postId,
            @RequestParam ReactionType type,
            Model model
    ) {
        PostResponse result = postService.processReaction(userDetails, postId, type);
        model.addAttribute("post", result);
        return "html/post_components :: vote-fragment";
    }

}

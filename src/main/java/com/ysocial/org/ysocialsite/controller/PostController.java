package com.ysocial.org.ysocialsite.controller;

import com.ysocial.org.ysocialsite.dto.ProfileDto;
import com.ysocial.org.ysocialsite.dto.request.CreatePostRequest;
import com.ysocial.org.ysocialsite.dto.response.PostResponse;
import com.ysocial.org.ysocialsite.enums.ReactionType;
import com.ysocial.org.ysocialsite.security.CustomUserDetails;
import com.ysocial.org.ysocialsite.service.PostService;
import com.ysocial.org.ysocialsite.service.ProfileService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@Controller
@RequestMapping("/posts")
@Slf4j
public class PostController {

    private final PostService postService;
    private final ProfileService profileService;

    public PostController(PostService postService, ProfileService profileService) {
        this.postService = postService;
        this.profileService = profileService;
    }

    @GetMapping("/feed")
    public String getFeed(
            @AuthenticationPrincipal CustomUserDetails userDetails,
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
            @AuthenticationPrincipal CustomUserDetails userDetails,
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
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @RequestParam ReactionType type,
            Model model
    ) {
        PostResponse result = postService.processReaction(userDetails, postId, type);
        model.addAttribute("post", result);
        return "html/post_components :: vote-fragment";
    }


    @PostMapping
    public String createPost(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @Valid @ModelAttribute CreatePostRequest request,
                             BindingResult bindingResult,
                             MultipartFile image,
                             Model model) {
        // если вылетело исключение то покажем ошибку в углу
        if (bindingResult.hasErrors()) {
            model.addAttribute("errors", bindingResult.getFieldErrors());
            return "html/profile :: create-post-modal-fragment";
        }
        boolean isCreated = postService.createPost(userDetails, request, image);
        // пост создался то релоад страницы
        if (isCreated) {
            return "redirect:/profiles";
        }
        // если вдруг не создался, в теории наверное такое может произойти
        model.addAttribute("errors", "Не удалось создать пост");
        return "html/profile :: create-post-modal-fragment";
    }

}

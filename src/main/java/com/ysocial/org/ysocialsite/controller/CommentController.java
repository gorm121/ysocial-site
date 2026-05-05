package com.ysocial.org.ysocialsite.controller;

import com.ysocial.org.ysocialsite.dto.response.CommentResponse;
import com.ysocial.org.ysocialsite.security.CustomUserDetails;
import com.ysocial.org.ysocialsite.service.CommentService;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;



@Controller
@RequestMapping("/post/{postId}/comments")
@Slf4j
@Validated
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public String createComment(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @PathVariable("postId") Long postId,
                                @RequestParam("text") @Size(min = 1, max = 50) String text,
                                Model model
    ) {
        CommentResponse comment = commentService.createComment(userDetails, postId, text);
        model.addAttribute("comment", comment);
        return "html/post_components :: comment-item";
    }
}

package com.ysocial.org.ysocialsite.controller;

import com.ysocial.org.ysocialsite.dto.response.CommentResponse;
import com.ysocial.org.ysocialsite.security.CustomUserDetails;
import com.ysocial.org.ysocialsite.service.CommentService;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/comments")
@Slf4j
@Validated
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/{postId}")
    public String createComment(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @PathVariable("postId") Long postId,
                                @RequestParam("text") @Size(min = 1, max = 50) String text,
                                Model model
    ) {
        CommentResponse comment = commentService.createComment(userDetails, postId, text);
        model.addAttribute("comment", comment);
        return "post_components :: comment-item";
    }

    @DeleteMapping("/{commentId}")
    public String deleteComment(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @PathVariable("commentId") Long commentId
    ) {
        commentService.deleteComment(userDetails, commentId);
        return "fragments :: empty-fragment";
    }
}

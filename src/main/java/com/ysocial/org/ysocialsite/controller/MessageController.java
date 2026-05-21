package com.ysocial.org.ysocialsite.controller;

import com.ysocial.org.ysocialsite.dto.ChatDto;
import com.ysocial.org.ysocialsite.dto.MessageInChatDto;
import com.ysocial.org.ysocialsite.dto.ProfileDto;
import com.ysocial.org.ysocialsite.security.CustomUserDetails;
import com.ysocial.org.ysocialsite.service.MessageService;
import com.ysocial.org.ysocialsite.service.ProfileService;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/messages")
@Validated
public class MessageController {

    private final MessageService messageService;
    private final ProfileService profileService;

    public MessageController(MessageService messageService, ProfileService profileService) {
        this.messageService = messageService;
        this.profileService = profileService;
    }

    @GetMapping
    public String getChats(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        ProfileDto profile = profileService.getMyProfile(userDetails);
        model.addAttribute("profile", profile);
        model.addAttribute("currentUserId", profile.id());
        model.addAttribute("chats", messageService.getChats(userDetails));
        model.addAttribute("chat", null);
        model.addAttribute("selectedChatId", 0L);
        return "html/messages";
    }
    @GetMapping("/{id}")
    public String getChatOrCreate(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @PathVariable Long id,
                                  Model model)
    {
        ChatDto chat = messageService.getChatOrCreate(userDetails, id);
        model.addAttribute("chat", chat);
        model.addAttribute("profile", profileService.getMyProfile(userDetails));
        model.addAttribute("currentUserId", profileService.getMyProfile(userDetails).id());
        model.addAttribute("selectedChatId", chat.chatId());
        model.addAttribute("chats", messageService.getChats(userDetails));
        return "html/messages";
    }

    @PostMapping("/chat/{chatId}/send")
    public String sendMessage(@AuthenticationPrincipal CustomUserDetails userDetails,
                              @PathVariable Long chatId,
                              @RequestParam("message") @Size(min = 1, max = 255) String message,
                              Model model
    ) {
        MessageInChatDto m = messageService.sendMessage(userDetails, chatId, message); 
        model.addAttribute("profile", profileService.getMyProfile(userDetails));
        model.addAttribute("currentUserId", profileService.getMyProfile(userDetails).id());
        model.addAttribute("selectedChatId", chatId);
        model.addAttribute("chats", messageService.getChats(userDetails));
        model.addAttribute("message", m);


        return "html/messages :: message-fragment";
    }
}
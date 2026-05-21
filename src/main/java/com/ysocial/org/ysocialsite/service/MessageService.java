package com.ysocial.org.ysocialsite.service;

import com.ysocial.org.ysocialsite.dto.ChatDto;
import com.ysocial.org.ysocialsite.dto.ChatInListDto;
import com.ysocial.org.ysocialsite.dto.MessageInChatDto;
import com.ysocial.org.ysocialsite.entities.Chat;
import com.ysocial.org.ysocialsite.entities.Message;
import com.ysocial.org.ysocialsite.entities.Profile;
import com.ysocial.org.ysocialsite.exceptions.EntityNotFoundException;
import com.ysocial.org.ysocialsite.repository.ChatRepository;
import com.ysocial.org.ysocialsite.repository.MessageRepository;
import com.ysocial.org.ysocialsite.repository.ProfileRepository;
import com.ysocial.org.ysocialsite.security.CustomUserDetails;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final ProfileRepository profileRepository;

    public MessageService(MessageRepository messageRepository, ChatRepository chatRepository, ProfileRepository profileRepository) {
        this.messageRepository = messageRepository;
        this.chatRepository = chatRepository;
        this.profileRepository = profileRepository;
    }
   

    public List<ChatInListDto> getChats(CustomUserDetails userDetails) {
        Long currentUserId = userDetails.getId();

        List<Chat> chats = chatRepository.findAllByUser(currentUserId);
        if (chats.isEmpty()) return Collections.emptyList();

        // собираем id всех с кем есть чат
        Set<Long> partnerIds = chats.stream()
                .map(c -> c.getUser1().equals(currentUserId) ? c.getUser2() : c.getUser1())
                .collect(Collectors.toSet());

        // здесь собираем их профили
        Map<Long, Profile> userMap = profileRepository.findAllByUsersId(partnerIds.stream().toList()).stream()
                                    .collect(Collectors.toMap(Profile::getUserId, p -> p));


        return chats.stream()
                .map(chat -> {
                    Long partnerId = chat.getUser1().equals(currentUserId) ? chat.getUser2() : chat.getUser1();
                    Profile partner = userMap.get(partnerId);
                    return new ChatInListDto(
                        chat.getId(),
                        partnerId,
                        partner != null ? partner.getFirstName() + " " + partner.getLastName() : "Unknown",
                         "/images/default-avatar.png",
                        chat.getLastMessageText()
                    );
                })
            .sorted(Comparator.comparing(ChatInListDto::name).reversed())
            .toList();
    }

    @Transactional
    public ChatDto getChatOrCreate(CustomUserDetails userDetails, Long userId) {
        Long currentUserId = userDetails.getId();


        if (userId.equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нельзя создать чат с самим собой");
        }

        Optional<Chat> chatOptional = chatRepository.findChat(userId, currentUserId);
        
        Profile currentProfile = profileRepository.findByUserId(currentUserId)
            .orElseThrow(() -> new EntityNotFoundException("Профиль не найден"));;
        
        Profile profile = profileRepository.findByUserId(userId)
            .orElseThrow(() -> new EntityNotFoundException("Профиль не найден"));
        String name = profile.getFirstName() + " " + profile.getLastName();

        Chat chat;
        List<MessageInChatDto> messages = Collections.emptyList();

        // если чат есть, то заполняем все что нужно
        if (chatOptional.isPresent()) {
            chat = chatOptional.get();
            messages = messageRepository.findByChatId(chat.getId())
                .stream()
                .map(m -> new MessageInChatDto(
                    m.getSenderId(),    
                    m.getRecipientId(),   
                    m.getContent(),
                    "/images/default-avatar.png",
                    "/images/default-avatar.png",
                    m.getSentAt().format(DateTimeFormatter.ofPattern("d MMMM, HH:mm"))))
                .toList();
        } else {
            // если нет, то создаем новый чат
            chat = new Chat();
            chat.setUser1(userId);
            chat.setUser2(currentUserId);
            chat = chatRepository.save(chat); 
        }

        String avatarUrl = "/images/default-avatar.png";
        return new ChatDto(chat.getId(), userId, name, avatarUrl, messages);
    }

    @Transactional
    public MessageInChatDto sendMessage(CustomUserDetails userDetails, Long chatId, String message){
        Long currentUserId = userDetails.getId();
        
        Chat chat = chatRepository.findById(chatId)
            .orElseThrow(() -> new EntityNotFoundException("Чат не найден"));

        Long userId = chat.getUser1().equals(currentUserId) ? chat.getUser2() : chat.getUser1();

        Profile profile = profileRepository.findByUserId(userId)
            .orElseThrow(() -> new EntityNotFoundException("Профиль не найден"));
        
        Profile currentProfile = profileRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("Профиль не найден"));

        Message mes = new Message();
        mes.setChatId(chatId);
        mes.setContent(message);
        mes.setRead(false);
        mes.setSenderId(currentUserId);
        mes.setRecipientId(userId);
        mes.setSentAt(LocalDateTime.now());

        chat.setLastMessageText(mes.getContent());
        chatRepository.save(chat);
        Message savedMessage = messageRepository.save(mes);

        return new MessageInChatDto(
            savedMessage.getSenderId(),
            savedMessage.getRecipientId(),
            savedMessage.getContent(),
            "/images/default-avatar.png",
            "/images/default-avatar.png",
            savedMessage.getSentAt().format(DateTimeFormatter.ofPattern("d MMMM, HH:mm"))
        );
    }
}
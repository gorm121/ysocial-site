package com.ysocial.org.ysocialsite.service;


import com.ysocial.org.ysocialsite.dto.ProfileDto;
import com.ysocial.org.ysocialsite.dto.ProfileShortDto;
import com.ysocial.org.ysocialsite.entites.Friendship;
import com.ysocial.org.ysocialsite.entites.Profile;
import com.ysocial.org.ysocialsite.entites.User;
import com.ysocial.org.ysocialsite.enums.AccountStatus;
import com.ysocial.org.ysocialsite.enums.FriendshipStatus;
import com.ysocial.org.ysocialsite.enums.UserRole;
import com.ysocial.org.ysocialsite.repository.FriendshipRepository;
import com.ysocial.org.ysocialsite.repository.ProfileRepository;
import com.ysocial.org.ysocialsite.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;




import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;


@Service
@Slf4j
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    private final UserService userService;

    public ProfileService(ProfileRepository profileRepository, UserRepository userRepository, FriendshipRepository friendshipRepository, UserService userService) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.userService = userService;
    }

    public ProfileDto getMyProfile(UserDetails userDetails) {
        User user = userService.getUserByUserDetails(userDetails);
        Long friendsCount = friendshipRepository.countFriendshipByUsersAndStatus(user.getId(), user.getId(), FriendshipStatus.ACCEPTED);
        return mapToDto(user.getProfile(), friendsCount, true, null, user.getRole());
    }

     public ProfileDto getProfileById(UserDetails userDetails, UUID id) {
        User viewer = userService.getUserByUserDetails(userDetails);

        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Профиль не найден"));

        if (user.getStatus().equals(AccountStatus.BANNED)) {
            throw new RuntimeException("Пользователь заблокирован");
        }

        Profile profile = user.getProfile();
        Long friendsCount = friendshipRepository.countFriendshipByUsersAndStatus(user.getId(), user.getId(), FriendshipStatus.ACCEPTED);
        boolean isOwnProfile = viewer.getId().equals(user.getId());

        FriendshipStatus frontendStatus = null;
        Optional<Friendship> friendshipOpt = friendshipRepository.findFriendshipBetween(viewer.getId(), user.getId());
        // при заходе в чужой профиль нужно показать кнопку для взаимодейтвия (добавить в друзья, удалить из друзей, принять заявку)
        if (friendshipOpt.isPresent()) {
            Friendship f = friendshipOpt.get();
        
            if (f.getStatus() == FriendshipStatus.ACCEPTED) {
                frontendStatus = FriendshipStatus.ACCEPTED;
            } else if (f.getStatus() == FriendshipStatus.PENDING) {
                if (f.getRequesterId().equals(viewer.getId())) {
                    frontendStatus = FriendshipStatus.PENDING;
                } else {
                    frontendStatus = FriendshipStatus.RECEIVED;
                }
            }
        }
        // если профиль приватный и мы не владелец и это не наш друг то отдаем меньше инфы
        if (profile.isPrivate() && frontendStatus != FriendshipStatus.ACCEPTED && !isOwnProfile && viewer.getRole() == UserRole.USER) {
            return mapToPrivateProfileDto(profile, friendsCount, isOwnProfile, frontendStatus, user.getRole()); 
        }

        return mapToDto(profile, friendsCount, isOwnProfile, frontendStatus, user.getRole());
    }


    public ProfileDto mapToDto(
        Profile profile, 
        Long friendsCount, 
        boolean isOwnProfile, 
        FriendshipStatus friendStatus, 
        UserRole userRole
    ) {
        return ProfileDto.builder()
            .id(profile != null ? profile.getUserId() : null)
            .firstName(profile != null ? profile.getFirstName() : "")
            .lastName(profile != null ? profile.getLastName() : "")
            .bio(profile != null ? profile.getBio() : "")
            .birthDate(profile != null && profile.getBirthDate() != null 
                    ? profile.getBirthDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "не указано")
            .city(profile != null ? profile.getCity() : "не указан")
            .avatarUrl("/images/default-avatar.png")
            .countFriends(friendsCount)
            .isOwnProfile(isOwnProfile)
            .friendStatus(friendStatus)
            .userRole(userRole)
            .privateProfile(profile != null && profile.isPrivate())
            .build();
    }

    public ProfileDto mapToPrivateProfileDto(
        Profile profile, 
        Long friendsCount, 
        boolean isOwnProfile, 
        FriendshipStatus friendStatus, 
        UserRole userRole
    ) {
        return ProfileDto.builder()
            .id(profile != null ? profile.getUserId() : null)
            .firstName(profile != null ? profile.getFirstName() : "")
            .lastName(profile != null ? profile.getLastName() : "")
            .bio(profile != null ? profile.getBio() : "")
            .birthDate("Скрыто")
            .city("Скрыто")
            .avatarUrl("/images/default-avatar.png")
            .countFriends(friendsCount)
            .isOwnProfile(isOwnProfile)
            .friendStatus(friendStatus)
            .userRole(userRole)
            .privateProfile(true)
            .build();
    }

    public ProfileShortDto toProfileInPostDto(Profile profile) {
        String name = profile.getFirstName() + " " + profile.getLastName();
        String avatarUrl = "/images/default-avatar.png";
        return new ProfileShortDto(profile.getUserId(), name, avatarUrl);
    }
}
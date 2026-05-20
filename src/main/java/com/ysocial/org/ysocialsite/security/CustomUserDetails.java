package com.ysocial.org.ysocialsite.security;

import lombok.Getter;

import com.ysocial.org.ysocialsite.entities.User;
import com.ysocial.org.ysocialsite.enums.AccountStatus;
import com.ysocial.org.ysocialsite.enums.UserRole;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {
    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() { return user.getPasswordHash(); }

    @Override
    public String getUsername() { return user.getUsername(); }

    public Long getId() {
        return user.getId();
    }

    public UserRole getRole() {
        return user.getRole();
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() {
        return user.getStatus() != AccountStatus.BANNED;
    }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return user.getStatus() == AccountStatus.ACTIVE; }
}
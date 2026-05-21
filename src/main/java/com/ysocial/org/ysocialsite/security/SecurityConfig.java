package com.ysocial.org.ysocialsite.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, StatusCheckFilter statusCheckFilter) throws Exception {
        http
                .addFilterAfter(statusCheckFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/posts/feed", true)
                        .failureHandler((request, response, exception) -> {
                            // Если аккаунт заблокирован (isAccountNonLocked вернул false)
                            if (exception instanceof org.springframework.security.authentication.LockedException) {
                                response.sendRedirect("/login?banned=true");
                            } else {
                                response.sendRedirect("/login?error=true");
                            }
                        })
                        .permitAll()
                )

                .logout(logout -> logout
                    .logoutSuccessUrl("/login")
                    .deleteCookies("JSESSIONID")
                    .permitAll()
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login",           
                                "/register",
                                "/activate/**",
                                "/style/**",        
                                "/images/**",
                                "/favicon.ico",
                                "/error"
                        ).permitAll()

                        .anyRequest().authenticated()
                );
        return http.build();
    }
}

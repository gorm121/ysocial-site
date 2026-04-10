package com.ysocial.org.ysocialsite.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class RegisterRequest {
    @NotBlank(message = "Email не должен быть пустым")
    @NotNull
    @Email(message = "Некорректный формат Email")
    private String email;

    @NotBlank(message = "Никнейм не должен быть пустым")
    @NotNull
    private String username;

    @NotBlank(message = "Пароль не должен быть пустым")
    @NotNull
    private String password;

    @NotBlank(message = "Поле не должен быть пустым")
    @NotNull
    private String confirmPassword;
}

package com.ysocial.org.ysocialsite.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
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
    @Size(min = 8, max = 32, message = "Длина пароля должна быть от 8 до 32 символов")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$",
            message = "Пароль должен содержать минимум одну цифру, строчную, заглавную букву и спецсимвол"
    )
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @NotBlank(message = "Поле не должен быть пустым")
    @NotNull
    private String confirmPassword;
}

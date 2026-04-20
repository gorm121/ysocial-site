package com.ysocial.org.ysocialsite.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;


@Getter
@Setter
public class UpdateProfileRequest {
    
    @Size(min = 2, max = 10, message = "Имя должно быть от 2 до 10 символов")
    private String firstName;
    
    @Size(min = 2, max = 10, message = "Фамилия должна быть от 2 до 10 символов")
    private String lastName;
    
    @Size(min = 0, max = 100, message = "Описание не более 100 символом")
    private String bio;
    
    @Size(min = 0, max = 30, message = "Город должен быть не более 30 символом")
    private String city;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate birthDate;

    @NotNull
    boolean privateProfile;
}

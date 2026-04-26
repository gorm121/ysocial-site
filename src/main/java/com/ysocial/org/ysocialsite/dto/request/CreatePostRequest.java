package com.ysocial.org.ysocialsite.dto.request;


import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePostRequest {
    @Size(max = 255, message = "Слишком длинное содержание поста. Максимум 255 символов")
    private String content;
}

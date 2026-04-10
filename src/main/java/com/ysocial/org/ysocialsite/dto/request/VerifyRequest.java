package com.ysocial.org.ysocialsite.dto.request;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyRequest {
    private String code;
    private String email;
}

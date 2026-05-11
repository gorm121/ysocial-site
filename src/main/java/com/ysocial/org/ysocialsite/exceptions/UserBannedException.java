package com.ysocial.org.ysocialsite.exceptions;

public class UserBannedException extends RuntimeException {
    public UserBannedException(String message) {
        super(message);
    }

    public UserBannedException(Exception ex, String message){
        super(message, ex);
    }
}

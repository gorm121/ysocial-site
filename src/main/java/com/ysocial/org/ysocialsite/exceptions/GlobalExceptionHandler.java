package com.ysocial.org.ysocialsite.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountActivateException.class)
    public String handleAccountActivateException(AccountActivateException ex,
                                   Model model)
    {
        model.addAttribute("errorMessage", ex.getMessage());

        return "html/activation-error";
    }

    @ExceptionHandler(UserBannedException.class)
    public String handleUserBanned(UserBannedException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "html/banned_profile";
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public String handleConstraintViolation(ConstraintViolationException ex,
                                            Model model,
                                            HttpServletResponse response) {
        response.setHeader("HX-Retarget", "body");
        response.setHeader("HX-Reswap", "beforeend");

        String cleanMsg = ex.getConstraintViolations().iterator().next().getMessage();
        model.addAttribute("message", cleanMsg);

        return "html/fragments :: error-toast";
    }



    @ExceptionHandler(BadRequestException.class)
    public String handleBadRequest(BadRequestException ex,
                                            Model model,
                                            HttpServletResponse response) {
        response.setHeader("HX-Retarget", "body");
        response.setHeader("HX-Reswap", "beforeend");

        model.addAttribute("message", ex.getMessage());

        return "html/fragments :: error-toast";
    }


    @ExceptionHandler(EntityNotFoundException.class)
    public String handleEntityNotFoundException(EntityNotFoundException ex,
                                                Model model,
                                                HttpServletRequest request,
                                                HttpServletResponse response) {
        // при верификации кода, у нас если сущность юзера не найдена, то кидается это исключение
        // оно идет с URL: http://localhost:8081/verify?email=example@gmail.com
        // эта проверка нужна чисто для того чтобы пользователю вывести ошибку, ну вдруг он
        if (request.getParameter("email") != null){
            model.addAttribute("message", ex.getMessage());

            response.setHeader("HX-Retarget", "body");
            response.setHeader("HX-Reswap", "beforeend");

            return "html/fragments :: error-toast";
        }


        String hxRequest = request.getHeader("HX-Request");

        // условно если мы лайкаем пост который только что удалили
        // то через эту проверку страничка просто перезагрузится и этого поста не будет
        if ("true".equals(hxRequest)) {
            response.setHeader("HX-Refresh", "true");
            return null;
        }

        // иначе отдаем страницу not_found, ну если например чел попытался
        // в адресной строке вписать "../profiles/10" - а такого профиля нет
        model.addAttribute("message", ex.getMessage());
        return "html/not_found";
    }

    @ExceptionHandler(BindException.class)
    public String handleBindException(BindException ex,
                                      Model model,
                                      HttpServletResponse response) {
        response.setHeader("HX-Retarget", "body");
        response.setHeader("HX-Reswap", "beforeend");

        String cleanMsg = ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        model.addAttribute("message", cleanMsg);

        return "html/fragments :: error-toast";
    }

    public record ErrorResponse(HttpStatus status, String message, LocalDateTime timestamp) {}

}
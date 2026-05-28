package com.ysocial.org.ysocialsite.controller;

import java.io.InputStream;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import com.ysocial.org.ysocialsite.service.StorageService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/files")
@Slf4j
public class FileController {

    private final StorageService storageService;

    public FileController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/**")
    public ResponseEntity<byte[]> getFile(HttpServletRequest request) {
        String path = request.getRequestURI().substring("/files/".length());
        log.info("Получен запрос на файл: {}", path);
        try (InputStream stream = storageService.getFile(path)) {
            byte[] content = stream.readAllBytes();
    
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentLength(content.length);
    
            return new ResponseEntity<>(content, headers, HttpStatus.OK);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Файл не найден: " + path);
        }
    }}
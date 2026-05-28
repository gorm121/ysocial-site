package com.ysocial.org.ysocialsite.service;

import java.io.InputStream;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;

@Service
public class StorageService {

    private final MinioClient s3Client;
    private final String BUCKET_NAME = "photos";

    public StorageService(MinioClient s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadAvatar(Long userId, MultipartFile file) {
        String extension = getFileExtension(file.getOriginalFilename());
        String objectName = "avatars/user_" + userId + extension;
        uploadFile(objectName, file);
        return objectName;
    }

    public String uploadPostPhoto(MultipartFile file) {
        String extension = getFileExtension(file.getOriginalFilename());
        String objectName = "posts/" + UUID.randomUUID() + extension;
        uploadFile(objectName, file);
        return objectName;
    }

    public InputStream getFile(String objectName) {
        try {
            return s3Client.getObject(
                GetObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(objectName)
                    .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Файл не найден: " + objectName);
        }
    }

    public String getAvatarUrl(String objectName) {
        if (objectName == null || objectName.isEmpty()) {
            return "/images/default-avatar.png";
        }
        return "/files/" + objectName;
    }

    public String getPostPhotoUrl(String objectName) {
        if (objectName == null || objectName.isEmpty()) {
            return null;
        }
        return "/files/" + objectName;
    }

    private void uploadFile(String objectName, MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            s3Client.putObject(
                PutObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(objectName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Ошибка загрузки файла: " + objectName, e);
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return ".jpg";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
}
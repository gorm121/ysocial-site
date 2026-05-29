package com.ysocial.org.ysocialsite.config;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;


@Configuration
public class S3Config {

    @Value("${minio.url}")
    private String url;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    private Process seaweedProcess;

    @Bean
    public MinioClient s3Client() {
        // Сервер уже гарантированно запущен в @PostConstruct, 
        // поэтому здесь просто отдаем клиент.
        return MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();
    }

    @PostConstruct
    public void initServerAndBucket() {
        // 1. САМЫМ ПЕРВЫМ ДЕЛОМ запускаем сервер
        startSeaweedFSServer();

        // 2. Даем серверу 2 секунды на полную инициализацию S3 API 
        // (даже если порт открыт, API может быть еще не готово принимать запросы)
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 3. Теперь безопасно проверяем и создаем бакет
        try {
            MinioClient initClient = MinioClient.builder()
                    .endpoint(url)
                    .credentials(accessKey, secretKey)
                    .build();

            String bucketName = "photos";
            boolean exists = initClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            
            if (!exists) {
                initClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                System.out.println("Бакет '" + bucketName + "' создан автоматически в SeaweedFS");
            } else {
                System.out.println("Бакет '" + bucketName + "' уже существует.");
            }
        } catch (Exception e) {
            // Если что-то пошло не так, лучше выбросить RuntimeException, 
            // чтобы увидеть в логах четкую причину (например, неверные ключи в s3.json)
            throw new RuntimeException("Критическая ошибка инициализации бакета в SeaweedFS: " + e.getMessage(), e);
        }
    }

    private void startSeaweedFSServer() {
        int s3Port = extractPort(url);

        if (isPortInUse(s3Port)) {
            System.out.println("SeaweedFS уже работает на порту " + s3Port);
            return;
        }

        System.out.println("Автозапуск SeaweedFS (weed.exe)...");

        File dataDir = new File("social-network-photos");
        if (!dataDir.exists()) {
            dataDir.mkdir();
        }

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "weed.exe", 
                    "server",
                    "-ip=127.0.0.1",
                    "-dir=./social-network-photos",
                    "-s3",
                    "-s3.port=" + s3Port,
                    "-filer",
                    "-filer.port=8888",
                    "-s3.config=s3.json"
            );

            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
            
            seaweedProcess = pb.start();

            int attempts = 0;
            while (!isPortInUse(s3Port) && attempts < 10) {
                Thread.sleep(1000);
                attempts++;
            }

            System.out.println("Процесс SeaweedFS успешно стартовал!");

        } catch (IOException | InterruptedException e) {
            System.err.println("Ошибка при автоматическом запуске SeaweedFS: " + e.getMessage());
        }
    }

    @PreDestroy
    public void stopSeaweedFSServer() {
        if (seaweedProcess != null && seaweedProcess.isAlive()) {
            System.out.println("Остановка сервера SeaweedFS...");
            seaweedProcess.destroy(); 
            
            try {

                if (!seaweedProcess.waitFor(1, java.util.concurrent.TimeUnit.SECONDS)) {

                    seaweedProcess.destroyForcibly();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("Сервер SeaweedFS остановлен.");
        }
    }

    private boolean isPortInUse(int port) {
        try (Socket socket = new Socket("127.0.0.1", port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private int extractPort(String url) {
        try {
            String[] parts = url.split(":");
            return Integer.parseInt(parts[parts.length - 1].replace("/", ""));
        } catch (Exception e) {
            return 8333;
        }
    }
}
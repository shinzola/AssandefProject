package br.org.assandef.assandefsystem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class FileStorageConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads/boletos}")
    private String uploadDir;

    /**
     * Este método é executado AUTOMATICAMENTE quando a aplicação inicia
     * Ele cria a pasta uploads/boletos/ se não existir
     */
    @PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("✅ Pasta de uploads criada: " + uploadPath.toAbsolutePath());
            } else {
                System.out.println("✅ Pasta de uploads já existe: " + uploadPath.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao criar diretório de uploads: " + e.getMessage(), e);
        }
    }

    /**
     * Configura o Spring para servir arquivos da pasta uploads/
     * Exemplo: http://localhost:8080/uploads/boletos/doador_1/2025-01/boleto_123.pdf
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = Paths.get(uploadDir).toAbsolutePath().toString();

        registry.addResourceHandler("/uploads/boletos/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }

    public String getUploadDir() {
        return uploadDir;
    }
}
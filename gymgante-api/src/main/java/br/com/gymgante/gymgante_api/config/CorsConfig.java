package br.com.gymgante.gymgante_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração de CORS (Cross-Origin Resource Sharing).
 * 
 * Permite que o front-end (rodando em localhost:5500) faça requisições
 * para o back-end (rodando em localhost:8080).
 * 
 * SEM ESSA CONFIGURAÇÃO, o navegador bloqueia as requisições por segurança.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Permite CORS em todos os endpoints /api/*
                .allowedOrigins("http://localhost:5500", "http://127.0.0.1:5500") // Origem do Live Server
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Métodos permitidos
                .allowedHeaders("*") // Permite todos os headers
                .allowCredentials(true); // Permite envio de cookies/credenciais
    }
}   
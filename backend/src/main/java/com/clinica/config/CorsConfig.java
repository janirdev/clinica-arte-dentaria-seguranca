package com.clinica.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * VULNERAVEL (didatico): CORS totalmente aberto.
 * Permite que QUALQUER site no browser da vitima chame esta API.
 * Combinado com a ausencia de autenticacao, qualquer pagina maliciosa
 * consegue ler/alterar dados em nome do utilizador.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("*")
                .allowedHeaders("*");
    }
}

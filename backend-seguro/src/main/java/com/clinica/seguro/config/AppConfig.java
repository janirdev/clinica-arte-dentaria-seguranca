package com.clinica.seguro.config;

import com.clinica.seguro.security.AuthFilter;
import com.clinica.seguro.security.RateLimitFilter;
import com.clinica.seguro.security.TokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppConfig implements WebMvcConfigurer {

    private final String origem;

    public AppConfig(@Value("${app.cors.origem}") String origem) {
        this.origem = origem;
    }

    // CORRIGIDO: BCrypt para guardar/validar senhas.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // CORRIGIDO: CORS restrito apenas a origem conhecida do frontend.
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(origem)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type");
    }

    // Rate limiting primeiro (corre antes da autenticacao), para todos os /api/*.
    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter() {
        FilterRegistrationBean<RateLimitFilter> reg = new FilterRegistrationBean<>(new RateLimitFilter());
        reg.addUrlPatterns("/api/*");
        reg.setOrder(1);
        return reg;
    }

    // Autenticacao a seguir, para todos os /api/*.
    @Bean
    public FilterRegistrationBean<AuthFilter> authFilter(TokenService tokenService) {
        FilterRegistrationBean<AuthFilter> reg = new FilterRegistrationBean<>(new AuthFilter(tokenService));
        reg.addUrlPatterns("/api/*");
        reg.setOrder(2);
        return reg;
    }
}

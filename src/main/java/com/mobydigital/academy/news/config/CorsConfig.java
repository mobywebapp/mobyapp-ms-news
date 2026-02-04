package com.mobydigital.academy.news.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:9000",
                        "http://localhost:4200",
                        "http://localhost:3000",
                        "https://frontendsite-ten.vercel.app",
                        "https://frontendsite-roan.vercel.app",
                        "https://mobydigital.com",
                        "https://www.mobydigital.com"
                )
                .allowedMethods("GET")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
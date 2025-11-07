package com.fashion.leon.fashionshopbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import org.springframework.format.FormatterRegistry;
import lombok.RequiredArgsConstructor;

import com.fashion.leon.fashionshopbackend.config.StringToLongListConverter;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final StringToLongListConverter stringToLongListConverter;
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve files under /images/** from the images folder in project root
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:images/");
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(stringToLongListConverter);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}


package com.fashion.leon.fashionshopbackend.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class StringToLongListConverter implements Converter<String, Long> {
    @Override
    public Long convert(String source) {
        try {
            return Long.valueOf(source);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

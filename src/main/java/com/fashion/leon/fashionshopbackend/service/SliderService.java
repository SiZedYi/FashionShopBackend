package com.fashion.leon.fashionshopbackend.service;

import com.fashion.leon.fashionshopbackend.dto.SliderRequest;
import com.fashion.leon.fashionshopbackend.dto.SliderResponse;
import com.fashion.leon.fashionshopbackend.entity.Slider;
import com.fashion.leon.fashionshopbackend.repository.SliderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SliderService {

    private final SliderRepository sliderRepository;

    @Transactional(readOnly = true)
    public List<SliderResponse> getAllSliders() {
        log.info("Fetching all sliders");
        return sliderRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SliderResponse> getActiveSliders() {
        log.info("Fetching active sliders");
        return sliderRepository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SliderResponse getSliderById(Long id) {
        log.info("Fetching slider with id: {}", id);
        Slider slider = sliderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Slider not found with id: " + id));
        return mapToResponse(slider);
    }

    @Transactional
    public SliderResponse createSlider(SliderRequest request) {
        log.info("Creating new slider with title: {}", request.getTitle());

        Slider slider = Slider.builder()
                .imageUrl(request.getImageUrl())
                .subtitle(request.getSubtitle())
                .title(request.getTitle())
                .buttonText(request.getButtonText())
                .buttonLink(request.getButtonLink())
                .textAlign(request.getTextAlign())
                .isActive(request.getIsActive())
                .displayOrder(request.getDisplayOrder())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Slider savedSlider = sliderRepository.save(slider);
        log.info("Slider created successfully with id: {}", savedSlider.getId());

        return mapToResponse(savedSlider);
    }

    @Transactional
    public SliderResponse updateSlider(Long id, SliderRequest request) {
        log.info("Updating slider with id: {}", id);

        Slider slider = sliderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Slider not found with id: " + id));

        slider.setImageUrl(request.getImageUrl());
        slider.setSubtitle(request.getSubtitle());
        slider.setTitle(request.getTitle());
        slider.setButtonText(request.getButtonText());
        slider.setButtonLink(request.getButtonLink());
        slider.setTextAlign(request.getTextAlign());
        slider.setIsActive(request.getIsActive());
        slider.setDisplayOrder(request.getDisplayOrder());
        slider.setUpdatedAt(LocalDateTime.now());

        Slider updatedSlider = sliderRepository.save(slider);
        log.info("Slider updated successfully with id: {}", updatedSlider.getId());

        return mapToResponse(updatedSlider);
    }

    @Transactional
    public void deleteSlider(Long id) {
        log.info("Deleting slider with id: {}", id);

        if (!sliderRepository.existsById(id)) {
            throw new RuntimeException("Slider not found with id: " + id);
        }

        sliderRepository.deleteById(id);
        log.info("Slider deleted successfully with id: {}", id);
    }

    private SliderResponse mapToResponse(Slider slider) {
        return SliderResponse.builder()
                .id(slider.getId())
                .imageUrl(slider.getImageUrl())
                .subtitle(slider.getSubtitle())
                .title(slider.getTitle())
                .buttonText(slider.getButtonText())
                .buttonLink(slider.getButtonLink())
                .textAlign(slider.getTextAlign())
                .isActive(slider.getIsActive())
                .displayOrder(slider.getDisplayOrder())
                .createdAt(slider.getCreatedAt())
                .updatedAt(slider.getUpdatedAt())
                .build();
    }
}


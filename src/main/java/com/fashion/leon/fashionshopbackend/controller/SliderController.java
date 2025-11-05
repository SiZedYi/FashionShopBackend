package com.fashion.leon.fashionshopbackend.controller;

import com.fashion.leon.fashionshopbackend.dto.SliderRequest;
import com.fashion.leon.fashionshopbackend.dto.SliderResponse;
import com.fashion.leon.fashionshopbackend.service.SliderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sliders")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SliderController {

    private final SliderService sliderService;

    @GetMapping
    public ResponseEntity<List<SliderResponse>> getAllSliders() {
        log.info("GET /api/sliders - Fetching all sliders");
        List<SliderResponse> sliders = sliderService.getAllSliders();
        return ResponseEntity.ok(sliders);
    }

    @GetMapping("/active")
    public ResponseEntity<List<SliderResponse>> getActiveSliders() {
        log.info("GET /api/sliders/active - Fetching active sliders");
        List<SliderResponse> sliders = sliderService.getActiveSliders();
        return ResponseEntity.ok(sliders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SliderResponse> getSliderById(@PathVariable Long id) {
        log.info("GET /api/sliders/{} - Fetching slider by id", id);
        SliderResponse slider = sliderService.getSliderById(id);
        return ResponseEntity.ok(slider);
    }

    @PostMapping
    public ResponseEntity<SliderResponse> createSlider(@Valid @RequestBody SliderRequest request) {
        log.info("POST /api/sliders - Creating new slider");
        SliderResponse slider = sliderService.createSlider(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(slider);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SliderResponse> updateSlider(
            @PathVariable Long id,
            @Valid @RequestBody SliderRequest request) {
        log.info("PUT /api/sliders/{} - Updating slider", id);
        SliderResponse slider = sliderService.updateSlider(id, request);
        return ResponseEntity.ok(slider);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSlider(@PathVariable Long id) {
        log.info("DELETE /api/sliders/{} - Deleting slider", id);
        sliderService.deleteSlider(id);
        return ResponseEntity.noContent().build();
    }
}


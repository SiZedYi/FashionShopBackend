package com.fashion.leon.fashionshopbackend.repository;

import com.fashion.leon.fashionshopbackend.entity.Slider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SliderRepository extends JpaRepository<Slider, Long> {

    List<Slider> findByIsActiveTrueOrderByDisplayOrderAsc();

    List<Slider> findAllByOrderByDisplayOrderAsc();
}


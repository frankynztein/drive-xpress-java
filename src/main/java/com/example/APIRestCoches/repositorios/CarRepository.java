package com.example.APIRestCoches.repositorios;

import com.example.APIRestCoches.modelos.Car;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, Long> {
    boolean existsByModel(String model);
    Optional<Car> findByModel(String model);
}


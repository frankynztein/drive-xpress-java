package com.example.APIRestCoches.servicios;

import com.example.APIRestCoches.modelos.Car;
import com.example.APIRestCoches.modelos.Feature;
import com.example.APIRestCoches.repositorios.FeatureRepository;
import com.example.APIRestCoches.repositorios.CarRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class FeatureService {
    private final FeatureRepository featureRepository;
    private final CarRepository carRepository;

    public FeatureService(FeatureRepository featureRepository, CarRepository carRepository) {
        this.featureRepository = featureRepository;
        this.carRepository = carRepository;
    }

    @Transactional(readOnly = true)
    public List<Feature> getAllFeatures() {
        return featureRepository.findAll();
    }

    @Transactional(readOnly = true)
    public long countCarsUsingFeature(Long featureId) {
        return carRepository.countByFeaturesId(featureId);
    }

    @Transactional
    public Feature createFeature(Feature feature) {
        if (feature.getName() == null || feature.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Feature name cannot be empty");
        }
        return featureRepository.save(feature);
    }

    @Transactional
    public Feature updateFeature(Long id, Feature updatedFeature) {
        Feature feature = featureRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Feature with id " + id + " not found"));

        if (updatedFeature.getName() == null || updatedFeature.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Feature name cannot be empty");
        }

        feature.setName(updatedFeature.getName());
        feature.setIcon(updatedFeature.getIcon());
        return featureRepository.save(feature);
    }

    @Transactional
    public void deleteFeature(Long id) {
        Feature feature = featureRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Feature with id " + id + " not found"));

        List<Car> carsWithFeature = carRepository.findByFeaturesId(id);
        carsWithFeature.forEach(car -> car.getFeatures().remove(feature));
        carRepository.saveAll(carsWithFeature);

        featureRepository.delete(feature);
    }
}
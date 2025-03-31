package com.example.APIRestCoches.modelos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String model;
    private String transmission;
    private BigDecimal dailyRentalCost;

    @Column(length = 1000)
    private String description;

    private String mainPhotoUrl;

    /*
    @ElementCollection
    private List<String> photoGallery;

     */

    private boolean isAvailable = true;

    @ManyToMany
    @JoinTable(
            name = "car_features",
            joinColumns = @JoinColumn(name = "car_id"),
            inverseJoinColumns = @JoinColumn(name = "feature_id")
    )
    private Set<Feature> features = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "car_categories",
            joinColumns = @JoinColumn(name = "car_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    @OneToMany(mappedBy = "car")
    @JsonIgnoreProperties("car")
    private Set<Rental> rentals = new HashSet<>();

    // En Car.java
    @ElementCollection
    @CollectionTable(
            name = "car_photo_gallery",
            joinColumns = @JoinColumn(name = "car_id")
    )
    @Column(name = "photo_url")
    private List<String> photoGallery = new ArrayList<>();

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getTransmission() {
        return transmission;
    }

    public void setTransmission(String transmission) {
        this.transmission = transmission;
    }

    public BigDecimal getDailyRentalCost() {
        return dailyRentalCost;
    }

    public void setDailyRentalCost(BigDecimal dailyRentalCost) {
        this.dailyRentalCost = dailyRentalCost;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMainPhotoUrl() {
        return mainPhotoUrl;
    }

    public void setMainPhotoUrl(String mainPhotoUrl) {
        this.mainPhotoUrl = mainPhotoUrl;
    }

    public List<String> getPhotoGallery() {
        return photoGallery;
    }

    public void setPhotoGallery(List<String> photoGallery) {
        this.photoGallery = photoGallery;
    }

    public void setFeatures(Set<Feature> features) {
        this.features = features;
    }

    public Set<Feature> getFeatures() {
        return features;
    }

    public void addFeature(Feature feature) {
        this.features.add(feature);
    }

    public void removeFeature(Feature feature) {
        this.features.remove(feature);
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    public void addCategory(Category category) {
        this.categories.add(category);
        category.getCars().add(this);
    }

    public void removeCategory(Category category) {
        this.categories.remove(category);
        category.getCars().remove(this);
    }

    public Set<Rental> getRentals() {
        return rentals;
    }

    public void setRentals(Set<Rental> rentals) {
        this.rentals = rentals;
    }
    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }
}
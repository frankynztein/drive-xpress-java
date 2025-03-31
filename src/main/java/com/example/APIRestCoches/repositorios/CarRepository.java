package com.example.APIRestCoches.repositorios;

import com.example.APIRestCoches.modelos.Car;
import com.example.APIRestCoches.modelos.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, Long> {
    boolean existsByModel(String model);
    Optional<Car> findByModel(String model);
    List<Car> findByFeaturesId(Long featureId);
    List<Car> findByCategoriesTitleContainingIgnoreCase(String title);

    @Query("SELECT COUNT(c) FROM Car c JOIN c.features f WHERE f.id = :featureId")
    long countByFeaturesId(@Param("featureId") Long featureId);

    // búsqueda por categoría
    @Query("SELECT c FROM Car c JOIN c.categories cat WHERE cat.title = :category")
    List<Car> findByCategoriesName(@Param("category") String category);

    // búsqueda por disponibilidad
    @Query("SELECT c FROM Car c WHERE c.id NOT IN " +
            "(SELECT r.car.id FROM Rental r WHERE " +
            "r.startDate < :endDate AND r.endDate > :startDate AND r.status <> 'CANCELLED')")
    List<Car> findAvailableBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // búsqueda combinada
    @Query("SELECT c FROM Car c JOIN c.categories cat WHERE " +
            "cat.id = :categoryId AND c.id NOT IN " +
            "(SELECT r.car.id FROM Rental r WHERE " +
            "r.startDate < :endDate AND r.endDate > :startDate AND r.status <> 'CANCELLED')")
    List<Car> findByCategoryIdAndAvailableBetweenDates(
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT DISTINCT c.model FROM Car c")
    List<String> findAllModels();

    // búsqueda insensible a mayúsculas/minúsculas
    @Query("SELECT c FROM Category c WHERE LOWER(c.title) LIKE LOWER(concat('%', :searchTerm, '%'))")
    List<Category> findByTitleContainingIgnoreCase(@Param("searchTerm") String searchTerm);

    // Búsqueda por disponibilidad general
    @Query("SELECT c FROM Car c WHERE c.isAvailable = true")
    List<Car> findAvailableCars();

    // Búsqueda por categoría y disponibilidad
    @Query("SELECT c FROM Car c JOIN c.categories cat WHERE c.isAvailable = true AND cat.id = :categoryId")
    List<Car> findAvailableCarsByCategory(@Param("categoryId") Long categoryId);

    // Búsqueda por disponibilidad en rango de fechas
    @Query("SELECT c FROM Car c WHERE c.isAvailable = true AND " +
            "NOT EXISTS (SELECT r FROM Rental r WHERE r.car = c AND " +
            "r.status IN ('ACTIVE', 'CONFIRMED') AND " +
            "((r.startDate <= :endDate AND r.endDate >= :startDate)))")
    List<Car> findAvailableCarsByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Búsqueda por categoría y disponibilidad en rango de fechas
    @Query("SELECT c FROM Car c JOIN c.categories cat WHERE " +
            "c.isAvailable = true AND cat.id = :categoryId AND " +
            "NOT EXISTS (SELECT r FROM Rental r WHERE r.car = c AND " +
            "r.status IN ('ACTIVE', 'CONFIRMED') AND " +
            "((r.startDate <= :endDate AND r.endDate >= :startDate)))")
    List<Car> findAvailableCarsByCategoryAndDateRange(
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}


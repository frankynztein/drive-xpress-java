package com.example.APIRestCoches.repositorios;

import com.example.APIRestCoches.modelos.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    List<Rental> findByCarIdAndEndDateAfterAndStartDateBefore(
            Long carId,
            LocalDateTime startDate,
            LocalDateTime endDate);

    List<Rental> findByUserId(Long userId);

    List<Rental> findByCarIdAndEndDateAfter(
            Long carId,
            LocalDateTime currentDate);

    @Query("SELECT r FROM Rental r WHERE r.car.id = :carId AND r.status IN :statuses")
    List<Rental> findByCarIdAndStatusIn(
            @Param("carId") Long carId,
            @Param("statuses") List<String> statuses);

}
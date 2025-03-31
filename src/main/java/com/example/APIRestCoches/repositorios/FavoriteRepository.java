package com.example.APIRestCoches.repositorios;

import com.example.APIRestCoches.modelos.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    boolean existsByUserIdAndCarId(Long userId, Long carId);

    @Transactional
    @Modifying
    @Query("DELETE FROM Favorite f WHERE f.user.id = ?1 AND f.car.id = ?2")
    void deleteByUserIdAndCarId(Long userId, Long carId);

    List<Favorite> findByUserId(Long userId);
}
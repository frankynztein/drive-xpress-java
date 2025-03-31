package com.example.APIRestCoches.servicios;

import com.example.APIRestCoches.modelos.Favorite;
import com.example.APIRestCoches.repositorios.FavoriteRepository;
import com.example.APIRestCoches.repositorios.UserRepository;
import com.example.APIRestCoches.repositorios.CarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FavoriteService {
    @Autowired
    private final FavoriteRepository favoriteRepository;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final CarRepository carRepository;

    @Autowired
    public FavoriteService(FavoriteRepository favoriteRepository,
                           UserRepository userRepository,
                           CarRepository carRepository) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.carRepository = carRepository;
    }

    @Transactional
    public boolean toggleFavorite(Long userId, Long carId) {
        userRepository.findById(userId).orElseThrow(() ->
                new RuntimeException("Usuario no encontrado"));
        carRepository.findById(carId).orElseThrow(() ->
                new RuntimeException("Coche no encontrado"));

        boolean isFavorite = favoriteRepository.existsByUserIdAndCarId(userId, carId);

        if (isFavorite) {
            favoriteRepository.deleteByUserIdAndCarId(userId, carId);
            return false;
        } else {
            Favorite favorite = new Favorite();
            favorite.setUser(userRepository.getReferenceById(userId));
            favorite.setCar(carRepository.getReferenceById(carId));
            favoriteRepository.save(favorite);
            return true;
        }
    }

    public List<Favorite> getUserFavorites(Long userId) {
        return favoriteRepository.findByUserId(userId);
    }

    public boolean isFavorite(Long userId, Long carId) {
        return favoriteRepository.existsByUserIdAndCarId(userId, carId);
    }
}

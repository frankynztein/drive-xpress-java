package com.example.APIRestCoches.repositorios;

import com.example.APIRestCoches.modelos.Car;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class CarRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CarRepository carRepository;

    private Car testCar;

    @BeforeEach
    void setUp() {
        testCar = new Car();
        testCar.setModel("Test Model");
        testCar.setTransmission("Manual");
        testCar.setDailyRentalCost(BigDecimal.valueOf(100.00));
        testCar.setDescription("Test Description");
        testCar.setMainPhotoUrl("test.jpg");
        testCar.setAvailable(true);

        entityManager.persist(testCar);
        entityManager.flush();
    }

    @Test
    void whenFindById_thenReturnCar() {
        Optional<Car> found = carRepository.findById(testCar.getId());

        assertTrue(found.isPresent());
        assertEquals(testCar.getModel(), found.get().getModel());
    }

    @Test
    void whenFindByModel_thenReturnCar() {
        Optional<Car> found = carRepository.findByModel("Test Model");

        assertTrue(found.isPresent());
        assertEquals(testCar.getId(), found.get().getId());
    }

    @Test
    void whenExistsByModel_thenReturnTrue() {
        boolean exists = carRepository.existsByModel("Test Model");
        assertTrue(exists);
    }

    @Test
    void whenFindAvailableCars_thenReturnAvailableCars() {
        List<Car> availableCars = carRepository.findAvailableCars();
        assertFalse(availableCars.isEmpty());
        assertTrue(availableCars.stream().allMatch(Car::isAvailable));
    }

    @Test
    void whenFindAllModels_thenReturnAllModels() {
        List<String> models = carRepository.findAllModels();
        assertFalse(models.isEmpty());
        assertTrue(models.contains("Test Model"));
    }
}

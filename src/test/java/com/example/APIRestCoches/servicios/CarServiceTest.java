package com.example.APIRestCoches.servicios;

import com.example.APIRestCoches.modelos.Car;
import com.example.APIRestCoches.repositorios.CarRepository;
import com.example.APIRestCoches.utils.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CarServiceTest {

    @Mock
    private CarRepository carRepository;

    @InjectMocks
    private CarService carService;

    private Car testCar;

    @BeforeEach
    void setUp() {
        testCar = new Car();
        testCar.setId(1L);
        testCar.setModel("Test Model");
        testCar.setTransmission("Manual");
        testCar.setDailyRentalCost(BigDecimal.valueOf(100.00));
        testCar.setDescription("Test Description");
        testCar.setMainPhotoUrl("test.jpg");
        testCar.setAvailable(true);
    }

    @Test
    void whenGetAllCars_thenReturnAllCars() {
        when(carRepository.findAll()).thenReturn(List.of(testCar));

        List<Car> cars = carService.getAllCars();

        assertFalse(cars.isEmpty());
        assertEquals(1, cars.size());
        verify(carRepository, times(1)).findAll();
    }

    @Test
    void whenGetCarById_thenReturnCar() {
        when(carRepository.findById(1L)).thenReturn(Optional.of(testCar));

        Car found = carService.getCarById(1L);

        assertNotNull(found);
        assertEquals("Test Model", found.getModel());
    }

    @Test
    void whenGetCarByIdWithInvalidId_thenThrowException() {
        when(carRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            carService.getCarById(99L);
        });
    }

    @Test
    void whenGetCarByModel_thenReturnCar() {
        when(carRepository.findByModel("Test Model")).thenReturn(Optional.of(testCar));

        Car found = carService.getCarByModel("Test Model");

        assertNotNull(found);
        assertEquals(1L, found.getId());
    }

    @Test
    void whenFindAvailableCars_thenReturnAvailableCars() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);

        when(carRepository.findAvailableCarsByDateRange(start, end))
                .thenReturn(List.of(testCar));

        List<Car> availableCars = carService.findAvailableCars(null, start, end);

        assertFalse(availableCars.isEmpty());
        assertEquals(1, availableCars.size());
    }

    @Test
    void whenUpdateCarAvailability_thenCarAvailabilityIsUpdated() {
        when(carRepository.findById(1L)).thenReturn(Optional.of(testCar));

        carService.updateCarAvailability(1L, false);

        assertFalse(testCar.isAvailable());
        verify(carRepository, times(1)).save(testCar);
    }
}

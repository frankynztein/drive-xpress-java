package com.example.APIRestCoches.servicios;

import com.example.APIRestCoches.modelos.Car;
import com.example.APIRestCoches.modelos.Category;
import com.example.APIRestCoches.modelos.Rental;
import com.example.APIRestCoches.repositorios.CarRepository;
import com.example.APIRestCoches.repositorios.CategoryRepository;
import com.example.APIRestCoches.repositorios.RentalRepository;
import com.example.APIRestCoches.utils.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarService {

    private final CarRepository carRepository;
    private final CategoryRepository categoryRepository;
    private final RentalRepository rentalRepository;

    @Autowired
    public CarService(CarRepository carRepository, CategoryRepository categoryRepository, RentalRepository rentalRepository) {
        this.carRepository = carRepository;
        this.categoryRepository = categoryRepository;
        this.rentalRepository = rentalRepository;
    }

    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    public Car getCarById(Long id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with id: " + id));
    }

    public Car getCarByModel(String model) {
        return carRepository.findByModel(model)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with model: " + model));
    }

    public List<Car> findAvailableCars(String categoryName, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Las fechas proporcionadas no son válidas");
        }

        if (categoryName == null || categoryName.isEmpty()) {
            return carRepository.findAvailableCarsByDateRange(startDate, endDate);
        } else {
            Category category = categoryRepository.findByTitle(categoryName)
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada: " + categoryName));

            return carRepository.findAvailableCarsByCategoryAndDateRange(
                    category.getId(), startDate, endDate);
        }
    }

    public boolean isCarAvailable(Car car, LocalDateTime startDate, LocalDateTime endDate) {
        if (!car.isAvailable()) {
            return false;
        }

        return car.getRentals().stream()
                .noneMatch(rental -> {
                    LocalDateTime rentalStart = rental.getStartDate();
                    LocalDateTime rentalEnd = rental.getEndDate();

                    return ("ACTIVE".equals(rental.getStatus()) || "CONFIRMED".equals(rental.getStatus())) &&
                            !(rentalEnd.isBefore(startDate) || rentalStart.isAfter(endDate));
                });
    }

    public void updateCarAvailability(Long carId, boolean isAvailable) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with id: " + carId));
        car.setAvailable(isAvailable);
        carRepository.save(car);
    }

    public List<LocalDateTime> getAvailableDates(Long carId, LocalDate startDate, LocalDate endDate) {
        if (carId == null) {
            throw new IllegalArgumentException("Car ID cannot be null");
        }

        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with id: " + carId));

        LocalDate defaultStart = LocalDate.now();
        LocalDate defaultEnd = defaultStart.plusMonths(1);

        LocalDate queryStart = startDate != null ? startDate : defaultStart;
        LocalDate queryEnd = endDate != null ? endDate : defaultEnd;

        List<Rental> conflictingRentals = rentalRepository.findByCarIdAndEndDateAfterAndStartDateBefore(
                carId,
                queryStart.atStartOfDay(),
                queryEnd.atTime(23, 59, 59));

        List<LocalDate> allDatesInRange = queryStart.datesUntil(queryEnd.plusDays(1))
                .collect(Collectors.toList());

        return allDatesInRange.stream()
                .filter(date -> isDateAvailable(date, conflictingRentals))
                .map(date -> date.atTime(12, 0))
                .collect(Collectors.toList());
    }

    private boolean isDateAvailable(LocalDate date, List<Rental> conflictingRentals) {
        LocalDateTime dateTime = date.atTime(12, 0);

        return conflictingRentals.stream()
                .noneMatch(rental ->
                        !dateTime.isBefore(rental.getStartDate()) &&
                                !dateTime.isAfter(rental.getEndDate()) &&
                                !rental.getStatus().equals("CANCELLED"));
    }

    public List<LocalDateTime> getNotAvailableDates(Long carId, LocalDate startDate, LocalDate endDate) {

        if (carId == null) {
            throw new IllegalArgumentException("Car ID cannot be null");
        }

        LocalDate defaultStart = LocalDate.now();
        LocalDate defaultEnd = defaultStart.plusMonths(1);

        LocalDate queryStart = startDate != null ? startDate : defaultStart;
        LocalDate queryEnd = endDate != null ? endDate : defaultEnd;

        List<Rental> conflictingRentals = rentalRepository.findByCarIdAndEndDateAfterAndStartDateBefore(
                carId,
                queryStart.atStartOfDay(),
                queryEnd.atTime(23, 59, 59));

        conflictingRentals = conflictingRentals.stream()
                .filter(rental -> !rental.getStatus().equals("CANCELLED"))
                .collect(Collectors.toList());

        return conflictingRentals.stream()
                .flatMap(rental ->
                        rental.getStartDate().toLocalDate()
                                .datesUntil(rental.getEndDate().toLocalDate().plusDays(1))
                                .map(date -> date.atTime(12, 0))) // Hora arbitraria para consistencia
                .distinct()
                .collect(Collectors.toList());
    }
}
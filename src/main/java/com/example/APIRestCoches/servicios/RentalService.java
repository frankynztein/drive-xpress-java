package com.example.APIRestCoches.servicios;

import com.example.APIRestCoches.dto.RentalDTO;
import com.example.APIRestCoches.modelos.*;
import com.example.APIRestCoches.repositorios.*;
import com.example.APIRestCoches.excepciones.InvalidRentalException;
import com.example.APIRestCoches.excepciones.CarNotAvailableException;
import com.example.APIRestCoches.utils.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class RentalService {

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private final CarService carService;

    @Autowired
    public RentalService(RentalRepository rentalRepository,
                         CarRepository carRepository,
                         UserRepository userRepository,
                         CarService carService) {
        this.rentalRepository = rentalRepository;
        this.carRepository = carRepository;
        this.userRepository = userRepository;
        this.carService = carService;
    }

    public Rental getRentalById(Long id) {
        return rentalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rental no encontrado con id: " + id));
    }

    @Transactional
    public Rental createRental(RentalDTO request) {
        validateRentalRequest(request);

        Car car = carRepository.findById(request.getCarId())
                .orElseThrow(() -> new ResourceNotFoundException("Coche no encontrado con id: " + request.getCarId()));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + request.getUserId()));

        if (!carService.isCarAvailable(car, request.getStartDate(), request.getEndDate())) {
            throw new CarNotAvailableException("El coche no está disponible en las fechas solicitadas");
        }

        BigDecimal totalPrice = calculateTotalPrice(car, request.getStartDate(), request.getEndDate());

        Rental rental = buildRental(request, car, user, totalPrice);

        updateCarAvailability(car.getId(), false);

        return rentalRepository.save(rental);
    }

    @Transactional
    public Rental cancelRental(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException("Rental no encontrado con id: " + rentalId));

        if (List.of("CANCELLED", "COMPLETED").contains(rental.getStatus())) {
            throw new InvalidRentalException("No se puede cancelar un rental con estado: " + rental.getStatus());
        }

        rental.setStatus("CANCELLED");

        updateCarAvailability(rental.getCar().getId(), true);

        return rentalRepository.save(rental);
    }

    @Transactional
    public Rental completeRental(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException("Rental no encontrado con id: " + rentalId));

        if (!"ACTIVE".equals(rental.getStatus())) {
            throw new InvalidRentalException("Solo se pueden completar rentals ACTIVOS");
        }

        rental.setStatus("COMPLETED");

        updateCarAvailability(rental.getCar().getId(), true);

        return rentalRepository.save(rental);
    }

    public List<Rental> getUserRentals(Long userId) {
        return rentalRepository.findByUserId(userId);
    }

    public List<Rental> getActiveRentalsForCar(Long carId) {
        return rentalRepository.findByCarIdAndStatusIn(carId, List.of("PENDING", "CONFIRMED", "ACTIVE"));
    }

    private void validateRentalRequest(RentalDTO request) {
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new InvalidRentalException("Las fechas de inicio y fin son requeridas");
        }

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new InvalidRentalException("La fecha de fin debe ser posterior a la de inicio");
        }

        if (request.getStartDate().isBefore(LocalDateTime.now())) {
            throw new InvalidRentalException("No se pueden hacer reservas en fechas pasadas");
        }
    }

    private BigDecimal calculateTotalPrice(Car car, LocalDateTime startDate, LocalDateTime endDate) {
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        return car.getDailyRentalCost().multiply(BigDecimal.valueOf(days));
    }

    private Rental buildRental(RentalDTO request, Car car, User user, BigDecimal totalPrice) {
        Rental rental = new Rental();
        rental.setCar(car);
        rental.setUser(user);
        rental.setStartDate(request.getStartDate());
        rental.setEndDate(request.getEndDate());
        rental.setTotalPrice(totalPrice);
        rental.setStatus("PENDING"); // Estado inicial

        return rental;
    }

    private void updateCarAvailability(Long carId, boolean isAvailable) {
        carService.updateCarAvailability(carId, isAvailable);
    }

    public boolean isCarAvailable(Car car, LocalDateTime startDate, LocalDateTime endDate) {
        return rentalRepository.findByCarIdAndEndDateAfterAndStartDateBefore(
                car.getId(), startDate, endDate).isEmpty();
    }
}
package com.example.APIRestCoches.controladores;

import com.example.APIRestCoches.dto.RentalDTO;
import com.example.APIRestCoches.modelos.Rental;
import com.example.APIRestCoches.modelos.User;
import com.example.APIRestCoches.servicios.RentalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/rentals")
public class RentalController {

    @Autowired
    private RentalService rentalService;

    @PostMapping
    public ResponseEntity<Rental> createRental(
            @Valid @RequestBody RentalDTO request,
            @AuthenticationPrincipal User user) {

        request.setUserId(user.getId());

        Rental createdRental = rentalService.createRental(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdRental.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdRental);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rental> getRentalById(@PathVariable Long id) {
        Rental rental = rentalService.getRentalById(id);
        return ResponseEntity.ok(rental);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Rental>> getUserRentals(@PathVariable Long userId) {
        List<Rental> rentals = rentalService.getUserRentals(userId);
        return ResponseEntity.ok(rentals);
    }
}
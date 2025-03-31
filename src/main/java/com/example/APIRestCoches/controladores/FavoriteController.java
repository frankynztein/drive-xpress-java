package com.example.APIRestCoches.controladores;

import com.example.APIRestCoches.modelos.User;
import com.example.APIRestCoches.servicios.FavoriteService;
import com.example.APIRestCoches.repositorios.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {
    @Autowired
    private final FavoriteService favoriteService;
    private final UserRepository userRepository;

    @Autowired
    public FavoriteController(FavoriteService favoriteService, UserRepository userRepository) {
        this.favoriteService = favoriteService;
        this.userRepository = userRepository;
    }

    @PostMapping("/toggle/{carId}")
    public ResponseEntity<?> toggleFavorite(@PathVariable Long carId, HttpServletRequest request) {
        try {
            // Obtener autenticación del contexto de seguridad
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "No autenticado", "message", "Debes iniciar sesión"));
            }

            // Obtener el email del usuario autenticado
            String email;
            if (auth.getPrincipal() instanceof UserDetails) {
                email = ((UserDetails) auth.getPrincipal()).getUsername();
            } else if (auth.getPrincipal() instanceof String) {
                email = (String) auth.getPrincipal();
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Tipo de autenticación no soportado"));
            }

            // Buscar el usuario
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado para el email: " + email));

            // Llamar al servicio
            boolean isFavorite = favoriteService.toggleFavorite(user.getId(), carId);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "isFavorite", isFavorite
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error en el servidor", "message", e.getMessage()));
        }
    }


    @GetMapping("/check/{carId}")
    public ResponseEntity<?> checkFavorite(@PathVariable Long carId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return ResponseEntity.ok(Map.of("isFavorite", false));
            }

            String username = null;
            if (auth.getPrincipal() instanceof UserDetails) {
                username = ((UserDetails) auth.getPrincipal()).getUsername();
            } else if (auth.getPrincipal() instanceof String) {
                username = (String) auth.getPrincipal();
            }

            if (username == null) {
                return ResponseEntity.ok(Map.of("isFavorite", false));
            }

            User user = userRepository.findByEmail(username)
                    .orElse(null);

            if (user == null) {
                return ResponseEntity.ok(Map.of("isFavorite", false));
            }

            boolean isFavorite = favoriteService.isFavorite(user.getId(), carId);
            return ResponseEntity.ok(Map.of("isFavorite", isFavorite));

        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "isFavorite", false,
                    "error", "Error al verificar favorito"
            ));
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserFavorites() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();

            return ResponseEntity.ok(favoriteService.getUserFavorites(user.getId()));
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al obtener favoritos: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
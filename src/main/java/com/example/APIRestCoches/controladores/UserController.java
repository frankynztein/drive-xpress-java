package com.example.APIRestCoches.controladores;

import com.example.APIRestCoches.dto.LoginDTO;
import com.example.APIRestCoches.dto.UserDTO;
import com.example.APIRestCoches.modelos.User;
import com.example.APIRestCoches.repositorios.UserRepository;
import com.example.APIRestCoches.servicios.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.GrantedAuthority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body("El email ya está registrado");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok("Usuario registrado exitosamente");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO request, HttpSession session) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            session.setAttribute("USER_ID", user.getId());

            return ResponseEntity.ok(Map.of(
                    "email", user.getEmail(),
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName(),
                    "isAdmin", user.isAdmin(),
                    "initials", user.getFirstName().charAt(0) + "" + user.getLastName().charAt(0),
                    "mensaje", "Login exitoso",
                    "roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales inválidas"));
        }
    }


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("User {} attempting to access /api/users", auth.getName());
        log.info("User authorities: {}", auth.getAuthorities());
        log.info("Is authenticated: {}", auth.isAuthenticated());
        log.info("Principal: {}", auth.getPrincipal());


        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateAdminStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> payload) {
        boolean isAdmin = payload.get("isAdmin");
        System.out.println("Payload recibido: " + payload);

        boolean updated = userService.setAdmin(id, isAdmin);
        if (updated) {
            return ResponseEntity.ok().body(Map.of("message", "Estado de administrador actualizado"));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
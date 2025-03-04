package com.example.APIRestCoches.controladores;

import com.example.APIRestCoches.modelos.Car;
import com.example.APIRestCoches.repositorios.CarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.http.HttpStatus;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/cars")
public class CarController {
    @Autowired
    private CarRepository carRepository;

    @Value("${upload.dir}")
    private String uploadDir;

    private static String UPLOAD_DIR;

    @PostConstruct
    public void init() {
        UPLOAD_DIR = new File(uploadDir).getAbsolutePath() + File.separator;
        System.out.println("Directorio de uploads: " + UPLOAD_DIR);
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                System.out.println("Directorio de uploads creado");
            } else {
                System.err.println("No se pudo crear el directorio de uploads");
            }
        }
        if (!dir.canWrite()) {
            System.err.println("No se puede escribir en el directorio de uploads");
        }
    }

    @PostMapping
    public ResponseEntity<?> createCar(
            @NotBlank(message = "El modelo es requerido") @RequestParam("model") String model,
            @NotBlank(message = "La categoría es requerida") @RequestParam("category") String category,
            @NotBlank(message = "La transmisión es requerida") @RequestParam("transmission") String transmission,
            @NotNull(message = "El costo de alquiler es requerido") @Positive(message = "El costo de alquiler debe ser positivo") @RequestParam("dailyRentalCost") BigDecimal dailyRentalCost,
            @NotBlank(message = "La descripción es requerida") @RequestParam("description") String description,
            @RequestParam(value = "mainPhoto", required = false) MultipartFile mainPhoto,
            @RequestParam(value = "gallery", required = false) MultipartFile[] gallery
    ) throws IOException {
        System.out.println("Método createCar llamado");
        if (carRepository.existsByModel(model)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El modelo de carro ya existe");
        }

        Car car = new Car();
        car.setModel(model);
        car.setCategory(category);
        car.setTransmission(transmission);
        car.setDailyRentalCost(dailyRentalCost);
        car.setDescription(description);

        if (mainPhoto != null && !mainPhoto.isEmpty()) {
            System.out.println("Recibida imagen principal: " + mainPhoto.getOriginalFilename());
            System.out.println("Tamaño de la imagen principal: " + mainPhoto.getSize() + " bytes");
            String mainPhotoPath = saveFile(mainPhoto);
            car.setMainPhotoUrl(mainPhotoPath);
        } else {
            System.out.println("No se recibió imagen principal");
        }

        List<String> galleryUrls = new ArrayList<>();
        if (gallery != null) {
            for (MultipartFile photo : gallery) {
                if (!photo.isEmpty()) {
                    System.out.println("Recibida imagen de galería: " + photo.getOriginalFilename());
                    System.out.println("Tamaño de la imagen de galería: " + photo.getSize() + " bytes");
                    String photoPath = saveFile(photo);
                    galleryUrls.add(photoPath);
                }
            }
        }
        car.setPhotoGallery(galleryUrls);

        Car savedCar = carRepository.save(car);
        return ResponseEntity.ok(savedCar);
    }

    private String saveFile(MultipartFile file) throws IOException {
        try {
            String fileName = file.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + fileName);
            System.out.println("Intentando guardar archivo en: " + path.toAbsolutePath());
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());
            System.out.println("Archivo guardado exitosamente: " + fileName);
            return fileName;
        } catch (IOException e) {
            System.err.println("Error al guardar el archivo: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping
    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Car> getCarById(@PathVariable Long id) {
        return carRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCar(@PathVariable Long id) {
        return carRepository.findById(id)
                .map(car -> {
                    carRepository.delete(car);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<?> handleValidationExceptions(jakarta.validation.ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach((violation) -> {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }

    @GetMapping("/model/{model}")
    public ResponseEntity<Car> getCarByModel(@PathVariable String model) {
        Optional<Car> car = carRepository.findByModel(model);
        return car.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

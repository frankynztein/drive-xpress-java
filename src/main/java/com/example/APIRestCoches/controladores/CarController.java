package com.example.APIRestCoches.controladores;

import com.example.APIRestCoches.modelos.Car;
import com.example.APIRestCoches.modelos.Category;
import com.example.APIRestCoches.modelos.Feature;
import com.example.APIRestCoches.repositorios.CategoryRepository;
import com.example.APIRestCoches.repositorios.FeatureRepository;
import com.example.APIRestCoches.repositorios.CarRepository;
import com.example.APIRestCoches.servicios.CarService;
import com.example.APIRestCoches.utils.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.http.HttpStatus;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/cars")
public class CarController {

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private CarRepository carRepository;
    @Autowired
    private final FeatureRepository featureRepository;
    private final CategoryRepository categoryRepository;
    private final CarService carService;

    public CarController(CarRepository carRepository, FeatureRepository featureRepository, CategoryRepository categoryRepository, CarService carService) {
        this.carRepository = carRepository;
        this.featureRepository = featureRepository;
        this.categoryRepository = categoryRepository;
        this.carService = carService;
    }

    @Value("${upload.dir}")
    String uploadDir;

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

    @Transactional
    @PostMapping
    public ResponseEntity<?> createCar(
            @NotBlank(message = "El modelo es requerido") @RequestParam("model") String model,
            @NotBlank(message = "La transmisión es requerida") @RequestParam("transmission") String transmission,
            @NotNull(message = "El costo de alquiler es requerido") @Positive(message = "El costo de alquiler debe ser positivo") @RequestParam("dailyRentalCost") BigDecimal dailyRentalCost,
            @NotBlank(message = "La descripción es requerida") @RequestParam("description") String description,
            @RequestParam(value = "mainPhoto", required = false) MultipartFile mainPhoto,
            @RequestParam(value = "gallery", required = false) MultipartFile[] gallery,
            @RequestParam(value = "features", required = false) List<Long> featureIds,
            @RequestParam(value = "categories", required = false) List<Long> categoryIds
    ) throws IOException {
        if (carRepository.existsByModel(model)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El modelo de carro ya existe");
        }

        Car car = new Car();
        car.setModel(model);
        car.setTransmission(transmission);
        car.setDailyRentalCost(dailyRentalCost);
        car.setDescription(description);

        if (featureIds != null && !featureIds.isEmpty()) {
            List<Feature> features = featureRepository.findAllById(featureIds);
            car.setFeatures(new HashSet<>(features));
        }

        if (categoryIds != null && !categoryIds.isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(categoryIds);
            if (categories.size() != categoryIds.size()) {
                return ResponseEntity.badRequest().body("Una o más categorías no existen.");
            }
            car.setCategories(new HashSet<>(categories));
        }

        if (mainPhoto != null && !mainPhoto.isEmpty()) {
            String mainPhotoPath = saveFile(mainPhoto);
            car.setMainPhotoUrl(mainPhotoPath);
        }

        Car savedCar = carRepository.save(car);

        if (gallery != null) {
            for (MultipartFile photo : gallery) {
                if (!photo.isEmpty()) {
                    String photoPath = saveFile(photo);
                    entityManager.createNativeQuery(
                                    "INSERT INTO car_photo_gallery (car_id, photo_url, display_order) VALUES (?, ?, ?)")
                            .setParameter(1, savedCar.getId()) // Ahora savedCar existe
                            .setParameter(2, photoPath)
                            .setParameter(3, 0)
                            .executeUpdate();
                }
            }
        }

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
                .map(car -> {
                    List<String> galleryPhotos = entityManager.createNativeQuery(
                                    "SELECT photo_url FROM car_photo_gallery WHERE car_id = :carId ORDER BY id")
                            .setParameter("carId", id)
                            .getResultList();

                    car.setPhotoGallery(galleryPhotos);
                    return ResponseEntity.ok(car);
                })
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
        if (car.isPresent()) {
            List<String> galleryPhotos = entityManager.createNativeQuery(
                            "SELECT photo_url FROM car_photo_gallery WHERE car_id = :carId ORDER BY id")
                    .setParameter("carId", car.get().getId())
                    .getResultList();

            car.get().setPhotoGallery(galleryPhotos);
            return ResponseEntity.ok(car.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCar(
            @PathVariable Long id,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String transmission,
            @RequestParam(required = false) BigDecimal dailyRentalCost,
            @RequestParam(required = false) String description,
            @RequestParam(value = "mainPhoto", required = false) MultipartFile mainPhoto,
            @RequestParam(value = "gallery", required = false) MultipartFile[] gallery,
            @RequestParam(value = "features", required = false) List<Long> featureIds,
            @RequestParam(value = "categories", required = false) List<Long> categoryIds

    )
            throws IOException {

        Car existingCar = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coche no encontrado con id: " + id));

        if (model != null) existingCar.setModel(model);
        if (transmission != null) existingCar.setTransmission(transmission);
        if (dailyRentalCost != null) existingCar.setDailyRentalCost(dailyRentalCost);
        if (description != null) existingCar.setDescription(description);

        if (featureIds != null) {
            List<Feature> features = featureRepository.findAllById(featureIds);
            if (features.size() != featureIds.size()) {
                List<Long> foundIds = features.stream().map(Feature::getId).toList();
                List<Long> missingIds = featureIds.stream()
                        .filter(featureId -> !foundIds.contains(featureId))
                        .toList();
                throw new IllegalArgumentException("Características no encontradas: " + missingIds);
            }
            existingCar.setFeatures(new HashSet<>(features));
        } else {
            existingCar.getFeatures().clear();
        }

        if (categoryIds != null) {
            List<Category> categories = categoryRepository.findAllById(categoryIds);
            if (categories.size() != categoryIds.size()) {
                return ResponseEntity.badRequest().body("Una o más categorías no existen.");
            }
            existingCar.setCategories(new HashSet<>(categories));
        } else {
            existingCar.getCategories().clear();
        }

        if (mainPhoto != null && !mainPhoto.isEmpty()) {
            String mainPhotoUrl = saveFile(mainPhoto);
            existingCar.setMainPhotoUrl(mainPhotoUrl);
        }

        if (gallery != null && gallery.length > 0) {
            List<String> galleryUrls = new ArrayList<>();
            for (MultipartFile photo : gallery) {
                if (!photo.isEmpty()) {
                    galleryUrls.add(saveFile(photo));
                }
            }
            existingCar.setPhotoGallery(galleryUrls);
        }

        Car updatedCar = carRepository.save(existingCar);
        return ResponseEntity.ok(updatedCar);
    }

    @GetMapping("/by-category/{categoryId}")
    public ResponseEntity<List<Car>> getCarsByCategory(@PathVariable Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElse(null);

        if (category == null) {
            return ResponseEntity.notFound().build();
        }

        List<Car> cars = new ArrayList<>(category.getCars());  // Convertir Set a List

        return ResponseEntity.ok(cars);
    }

    @GetMapping("/models")
    public ResponseEntity<List<String>> getAllCarModels() {
        return ResponseEntity.ok(carRepository.findAllModels());

    }

    @GetMapping("/available")
    public ResponseEntity<List<Car>> getAvailableCars(
            @RequestParam(required = false) String category,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        List<Car> availableCars = carService.findAvailableCars(category, startDate, endDate);
        return ResponseEntity.ok(availableCars);
    }


    @GetMapping("/{id}/available-dates")
    public ResponseEntity<List<LocalDateTime>> getAvailableDates(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<LocalDateTime> availableDates = carService.getAvailableDates(id, startDate, endDate);
        return ResponseEntity.ok(availableDates);
    }

    @GetMapping("/{id}/not-available-dates")
    public ResponseEntity<List<LocalDateTime>> getNotAvailableDates(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<LocalDateTime> notAvailableDates = carService.getNotAvailableDates(id, startDate, endDate);
        return ResponseEntity.ok(notAvailableDates);
    }
}

package com.example.APIRestCoches.controladores;

import com.example.APIRestCoches.modelos.Category;
import com.example.APIRestCoches.repositorios.CarRepository;
import com.example.APIRestCoches.repositorios.CategoryRepository;
import com.example.APIRestCoches.utils.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final CarRepository carRepository;

    @Value("${upload.dir}")
    private String uploadDir;
    private static String UPLOAD_DIR;

    public CategoryController(CategoryRepository categoryRepository, CarRepository carRepository) {
        this.categoryRepository = categoryRepository;
        this.carRepository = carRepository;
    }

    @PostConstruct
    public void init() {
        UPLOAD_DIR = new File(uploadDir).getAbsolutePath() + File.separator;
        System.out.println("Directorio de uploads para categorías: " + UPLOAD_DIR);
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryRepository.findAll());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> createCategory(
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {

        try {
            Category category = new Category();
            category.setTitle(title);
            category.setDescription(description);

            if (image != null && !image.isEmpty()) {
                String imagePath = saveFile(image);
                category.setImageUrl(imagePath);
            }

            Category savedCategory = categoryRepository.save(category);
            return ResponseEntity.ok(savedCategory);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error al crear la categoría: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Long id) {
        return categoryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateCategory(
            @PathVariable Long id,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {

        try {
            Category existingCategory = categoryRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

            if (title != null) existingCategory.setTitle(title);
            if (description != null) existingCategory.setDescription(description);

            if (image != null && !image.isEmpty()) {
                String imagePath = saveFile(image);
                existingCategory.setImageUrl(imagePath);
            }

            Category updatedCategory = categoryRepository.save(existingCategory);
            return ResponseEntity.ok(updatedCategory);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error al actualizar la categoría: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        try {
            Category category = categoryRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

            // Desasociar todos los coches
            if (!category.getCars().isEmpty()) {
                category.getCars().forEach(car -> {
                    car.getCategories().remove(category);
                    carRepository.save(car);
                });
            }

            categoryRepository.delete(category);
            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error al eliminar la categoría: " + e.getMessage());
        }
    }

    private String saveFile(MultipartFile file) throws IOException {
        try {
            String fileName = "category_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());
            return fileName;
        } catch (IOException e) {
            System.err.println("Error al guardar la imagen de categoría: " + e.getMessage());
            throw e;
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Category>> searchCategories(@RequestParam String term) {
        try {
             List<Category> results = categoryRepository.findByTitleContainingIgnoreCase(term);

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(null);
        }
    }
}
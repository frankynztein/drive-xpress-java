package com.example.APIRestCoches.controladores;

import com.example.APIRestCoches.modelos.Car;
import com.example.APIRestCoches.repositorios.CarRepository;
import com.example.APIRestCoches.repositorios.CategoryRepository;
import com.example.APIRestCoches.repositorios.FeatureRepository;
import com.example.APIRestCoches.servicios.CarService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class CarControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CarRepository carRepository;

    @Mock
    private FeatureRepository featureRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CarService carService;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private CarController carController;

    private Car testCar;

    @BeforeEach
    void setUp() {
        // Configurar el directorio de uploads para tests
        carController.uploadDir = "target/test-uploads";
        carController.init();

        mockMvc = MockMvcBuilders.standaloneSetup(carController).build();

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
    void whenGetAllCars_thenReturnCarList() throws Exception {
        when(carRepository.findAll()).thenReturn(List.of(testCar));

        mockMvc.perform(get("/api/cars")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].model").value("Test Model"));
    }

    @Test
    void whenGetCarById_thenReturnCar() throws Exception {
        when(carRepository.findById(1L)).thenReturn(Optional.of(testCar));
        when(entityManager.createNativeQuery(anyString())).thenReturn(mock(jakarta.persistence.Query.class));
        when(entityManager.createNativeQuery(anyString()).setParameter(anyInt(), any())).thenReturn(mock(jakarta.persistence.Query.class));
        when(entityManager.createNativeQuery(anyString()).getResultList()).thenReturn(List.of("photo1.jpg", "photo2.jpg"));

        mockMvc.perform(get("/api/cars/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("Test Model"))
                .andExpect(jsonPath("$.photoGallery").isArray());
    }

    @Test
    void whenGetCarByModel_thenReturnCar() throws Exception {
        when(carRepository.findByModel("Test Model")).thenReturn(Optional.of(testCar));
        when(entityManager.createNativeQuery(anyString())).thenReturn(mock(jakarta.persistence.Query.class));
        when(entityManager.createNativeQuery(anyString()).setParameter(anyString(), any())).thenReturn(mock(jakarta.persistence.Query.class));
        when(entityManager.createNativeQuery(anyString()).getResultList()).thenReturn(List.of("photo1.jpg", "photo2.jpg"));

        mockMvc.perform(get("/api/cars/model/Test Model")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("Test Model"))
                .andExpect(jsonPath("$.photoGallery").isArray());
    }

    @Test
    void whenGetAvailableCars_thenReturnAvailableCars() throws Exception {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);

        when(carService.findAvailableCars(any(), any(), any())).thenReturn(List.of(testCar));

        mockMvc.perform(get("/api/cars/available")
                        .param("startDate", start.toString())
                        .param("endDate", end.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].model").value("Test Model"));
    }

    @Test
    void whenCreateCar_thenReturnCreatedCar() throws Exception {
        MockMultipartFile mainPhoto = new MockMultipartFile(
                "mainPhoto",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes());

        when(carRepository.existsByModel(anyString())).thenReturn(false);
        when(carRepository.save(any(Car.class))).thenReturn(testCar);
        when(entityManager.createNativeQuery(anyString())).thenReturn(mock(jakarta.persistence.Query.class));

        mockMvc.perform(multipart("/api/cars")
                        .file(mainPhoto)
                        .param("model", "Test Model")
                        .param("transmission", "Manual")
                        .param("dailyRentalCost", "100.00")
                        .param("description", "Test Description")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("Test Model"));
    }

    @Test
    void whenCreateCarWithExistingModel_thenReturnConflict() throws Exception {
        MockMultipartFile mainPhoto = new MockMultipartFile(
                "mainPhoto",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes());

        when(carRepository.existsByModel("Existing Model")).thenReturn(true);

        mockMvc.perform(multipart("/api/cars")
                        .file(mainPhoto)
                        .param("model", "Existing Model")
                        .param("transmission", "Manual")
                        .param("dailyRentalCost", "100.00")
                        .param("description", "Test Description")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isConflict());
    }

    @Test
    void whenDeleteCar_thenReturnNoContent() throws Exception {
        when(carRepository.existsById(1L)).thenReturn(true);
        doNothing().when(carRepository).deleteById(1L);

        mockMvc.perform(delete("/api/cars/1"))
                .andExpect(status().isOk());
    }

    @Test
    void whenDeleteNonExistingCar_thenReturnNotFound() throws Exception {
        when(carRepository.existsById(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/cars/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenGetAvailableDates_thenReturnDates() throws Exception {
        when(carService.getAvailableDates(anyLong(), any(), any()))
                .thenReturn(List.of(LocalDateTime.now()));

        mockMvc.perform(get("/api/cars/1/available-dates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
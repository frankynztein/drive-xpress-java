package com.example.APIRestCoches.repositorios;

import com.example.APIRestCoches.modelos.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByTitle(String title);

    @Query("SELECT c FROM Category c WHERE LOWER(c.title) LIKE LOWER(concat('%', :searchTerm, '%'))")
    List<Category> searchByTitle(@Param("searchTerm") String searchTerm);

    List<Category> findByTitleContainingIgnoreCase(String title);
}


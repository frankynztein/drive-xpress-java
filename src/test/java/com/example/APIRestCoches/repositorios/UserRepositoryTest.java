package com.example.APIRestCoches.repositorios;

import com.example.APIRestCoches.modelos.User;
import com.example.APIRestCoches.repositorios.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    void whenFindByEmail_thenReturnUser() {
        // Given
        User user = new User();
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("test@example.com");
        user.setPassword("password");
        userRepository.save(user);

        // When
        User found = userRepository.findByEmail(user.getEmail()).orElse(null);

        // Then
        assertNotNull(found);
        assertEquals(user.getEmail(), found.getEmail());
    }
}

package com.example.APIRestCoches.servicios;

import com.example.APIRestCoches.dto.UserDTO;
import com.example.APIRestCoches.modelos.User;
import com.example.APIRestCoches.repositorios.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void getAllUsers_ShouldReturnListOfUserDTOs() {
        // Arrange
        User user1 = new User();
        user1.setId(1L);
        user1.setFirstName("John");
        user1.setLastName("Doe");
        user1.setEmail("john@example.com");
        user1.setAdmin(false);

        User user2 = new User();
        user2.setId(2L);
        user2.setFirstName("Jane");
        user2.setLastName("Smith");
        user2.setEmail("jane@example.com");
        user2.setAdmin(true);

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        // Act
        List<UserDTO> result = userService.getAllUsers();

        // Assert
        assertEquals(2, result.size());
        assertEquals("John", result.get(0).getFirstName());
        assertEquals("Doe", result.get(0).getLastName());
        assertEquals("john@example.com", result.get(0).getEmail());
        assertFalse(result.get(0).isAdmin());

        assertEquals("Jane", result.get(1).getFirstName());
        assertEquals("Smith", result.get(1).getLastName());
        assertEquals("jane@example.com", result.get(1).getEmail());
        assertTrue(result.get(1).isAdmin());

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void setAdmin_WhenUserExists_ShouldUpdateAndReturnTrue() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setAdmin(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        boolean result = userService.setAdmin(1L, true);

        // Assert
        assertTrue(result);
        assertTrue(user.isAdmin());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void setAdmin_WhenUserNotExists_ShouldReturnFalse() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        boolean result = userService.setAdmin(1L, true);

        // Assert
        assertFalse(result);
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).save(any());
    }
}

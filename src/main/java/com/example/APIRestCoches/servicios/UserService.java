package com.example.APIRestCoches.servicios;

import com.example.APIRestCoches.dto.UserDTO;
import com.example.APIRestCoches.modelos.User;
import com.example.APIRestCoches.repositorios.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> new UserDTO(user.getId() ,user.getFirstName(), user.getLastName(), user.getEmail(), user.isAdmin()))
                .collect(Collectors.toList());
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public boolean setAdmin(Long id, boolean isAdmin) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setAdmin(isAdmin);
            userRepository.save(user);
            return true;
        }
        return false;
    }
}

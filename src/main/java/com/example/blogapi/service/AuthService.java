
package com.example.blogapi.service;

import com.example.blogapi.dto.LoginRequest;
import com.example.blogapi.dto.RegisterRequest;
import com.example.blogapi.dto.UserResponse;
import com.example.blogapi.entity.User;
import com.example.blogapi.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        System.out.println("Registering email: " + email);

        if (userRepository.existsByEmailIgnoreCase(email)) {
            System.out.println("Email already exists: " + email);

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Email is already registered"
            );
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(email);

        // Store a BCrypt hash, never the raw password.
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);

        return new UserResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public User authenticate(LoginRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"
                ));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password"
            );
        }

        return user;
    }
    

@Transactional(readOnly = true)
public UserResponse getCurrentUser(String email) {

    User user = userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User not found"
            ));

    return new UserResponse(user);
}

}

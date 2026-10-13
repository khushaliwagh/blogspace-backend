
package com.example.blogapi.controller;

import com.example.blogapi.dto.LoginRequest;
import com.example.blogapi.dto.RegisterRequest;
import com.example.blogapi.dto.UserResponse;
import com.example.blogapi.entity.User;
import com.example.blogapi.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;




import java.util.Map;


import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(
            AuthService authService,
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(
            @Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public UserResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        Authentication authentication =
                authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                        request.getEmail().trim().toLowerCase(),
                        request.getPassword()
                    )
                );

        SecurityContext context =
                SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        // Prevent session fixation after successful authentication.
        HttpSession session = httpRequest.getSession(true);
        httpRequest.changeSessionId();

        securityContextRepository.saveContext(
                context, httpRequest, httpResponse);

        User user = authService.authenticate(request);

        return new UserResponse(user);
    }
    

@GetMapping("/me")
public UserResponse getCurrentUser(
        org.springframework.security.core.Authentication authentication) {

    if (authentication == null
            || !authentication.isAuthenticated()) {
        throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Please log in"
        );
    }

    return authService.getCurrentUser(authentication.getName());
}


@PostMapping("/logout")
public Map<String, String> logout(
        HttpServletRequest request) {

    HttpSession session = request.getSession(false);

    if (session != null) {
        session.invalidate();
    }

    SecurityContextHolder.clearContext();

    return Map.of("message", "Logged out successfully");
}


}

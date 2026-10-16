package com.example.blogapi.config;

import com.example.blogapi.service.CustomUserDetailsService;

import java.util.List;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
            List.of("https://blogspace-frontend-3q10.onrender.com")
        );

        configuration.setAllowedMethods(
            List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")
        );

        configuration.setAllowedHeaders(
            List.of(
                "Content-Type",
                "Accept",
                "X-XSRF-TOKEN",
                "X-CSRF-TOKEN"
            )
        );

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            DaoAuthenticationProvider provider) {
        return new ProviderManager(provider);
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider provider,
            SecurityContextRepository contextRepository)
            throws Exception {

        http
        .cors(cors ->
            cors.configurationSource(corsConfigurationSource())
        )
        .csrf(csrf -> csrf
                .csrfTokenRepository(
                    CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(
                    new CsrfTokenRequestAttributeHandler())
            )
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
                )
            )
            .securityContext(context -> context
                .securityContextRepository(contextRepository))
            .authenticationProvider(provider)
            .authorizeHttpRequests(auth -> auth

                // Public registration and login
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/auth/register",
                    "/api/auth/login"
                ).permitAll()

                // My Stories must require login.
                // Keep this BEFORE the public GET rules.
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/posts/my-stories"
                ).authenticated()

                // Public story and comment-reading endpoints
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/posts",
                    "/api/posts/search",
                    "/api/posts/*",
                    "/api/posts/*/comments"
                ).permitAll()

                // Like endpoints
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/posts/*/likes"
                ).permitAll()

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/posts/*/likes/me"
                ).authenticated()

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/posts/*/like"
                ).authenticated()

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/posts/*/like"
                ).authenticated()

                // Authentication endpoints
                .requestMatchers("/api/auth/me").authenticated()
                .requestMatchers("/api/auth/logout").authenticated()

                // Adding comments requires login
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/posts/*/comments"
                ).authenticated()

                // Creating, updating and deleting stories requires login
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/posts/**"
                ).authenticated()

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/posts/**"
                ).authenticated()

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/posts/**"
                ).authenticated()

                .anyRequest().permitAll()
            );

        return http.build();
    }
}
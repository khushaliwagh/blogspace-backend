
package com.example.blogapi.config;

import com.example.blogapi.service.CustomUserDetailsService;

import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.http.HttpStatus;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
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

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/auth/register",
                    "/api/auth/login"
                ).permitAll()

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/posts",
                    "/api/posts/search",
                    "/api/posts/*",
                    "/api/posts/*/comments"
                ).permitAll()

                // Like/Unlike endpoints
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

                .requestMatchers("/api/auth/me").authenticated()
                .requestMatchers("/api/auth/logout").authenticated()

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/posts/*/comments"
                ).authenticated()

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

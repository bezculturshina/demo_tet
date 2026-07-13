package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;


@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Отключаем CSRF для REST-запросов
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/rest-api/auth/register", "/rest-api/auth/login").permitAll()
                        .requestMatchers("/rest-api/customer/books").permitAll()

                        .requestMatchers("/rest-api/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/rest-api/employee/**").hasAuthority("ROLE_EMPLOYEE")
                        .requestMatchers("/rest-api/customer/**").hasAuthority("CUSTOMER")

                        .requestMatchers("/rest-api/**").authenticated()
                        .anyRequest().permitAll()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .formLogin(form -> form.disable()) // Отключаем стандартную форму "Please sign in"
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
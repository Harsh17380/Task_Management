package com.TaskManager.Taskmanager.security;

import com.TaskManager.Taskmanager.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            objectMapper.writeValue(
                                    response.getWriter(),
                                    new ApiResponse<>(false, "Authentication required. Please login again.")
                            );
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            objectMapper.writeValue(
                                    response.getWriter(),
                                    new ApiResponse<>(false, "You do not have permission to perform this action.")
                            );
                        })
                )

                .authorizeHttpRequests(auth -> auth

                        // VERY IMPORTANT
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Public endpoints
                        .requestMatchers("/users/login").permitAll()

                        // Swagger
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // Protected endpoints
                        .requestMatchers(HttpMethod.POST, "/users").hasAnyRole("SUPER_ADMIN", "COMPANY_ADMIN")
                        .requestMatchers("/users/role/**").authenticated()

                        .requestMatchers(HttpMethod.POST, "/tasks")
                        .hasRole("SUPERVISOR")

                        .requestMatchers("/tasks/supervisor/**").hasRole("SUPERVISOR")
                        .requestMatchers("/tasks/tl/**").hasRole("TL")
                        .requestMatchers("/tasks/**")
                        .authenticated()

                        .requestMatchers("/subtasks").hasRole("TL")
                        .requestMatchers("/subtasks/dev/**").hasRole("DEVELOPER")
                        .requestMatchers("/subtasks/*/status").hasRole("DEVELOPER")
                        .requestMatchers("/users/change-password").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/users/**")
                        .hasAnyRole("SUPER_ADMIN", "COMPANY_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/users")
                        .hasAnyRole("SUPER_ADMIN", "COMPANY_ADMIN")
                        .anyRequest().authenticated()
                )

                // IMPORTANT
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:4200",
                "https://corequeue.netlify.app"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization", "Content-Type", "X-Requested-With"
        ));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CommandLineRunner testPassword(PasswordEncoder encoder) {
        return args -> {
            System.out.println(encoder.encode("Admin@123"));
        };
    }
}

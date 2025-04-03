package ut.edu.project.configurations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ut.edu.project.jwt.JwtAuthenticationFilter;
import ut.edu.project.jwt.JwtUtil;
import ut.edu.project.services.UserService;

import static ut.edu.project.models.User.ROLE_ADMIN;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Công khai
                        .requestMatchers("/auths/**").permitAll()
                        .requestMatchers("/homestays/{id}").permitAll()
                        .requestMatchers("/homestays").permitAll()
                        // User endpoints - move this higher in the chain
                        .requestMatchers("/user/me").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/user/me/update").authenticated()
                        // HomestayController
                        .requestMatchers("/homestays/my-homestays").authenticated()
                        .requestMatchers("/homestays/create").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/homestays/{id}").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/homestays/{id}").hasAuthority("ADMIN")
                        // UserController
                        .requestMatchers(HttpMethod.GET, "/user/{id}").hasAuthority("ADMIN")
                        .requestMatchers("/user/all").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/user/{id}").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/user/{id}").hasAuthority("ADMIN")
                        // Admin routes
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        // Các request khác
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
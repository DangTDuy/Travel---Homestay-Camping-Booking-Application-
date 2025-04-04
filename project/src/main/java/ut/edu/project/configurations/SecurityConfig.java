package ut.edu.project.configurations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import ut.edu.project.jwt.JwtAuthenticationFilter;
import ut.edu.project.jwt.JwtUtil;
import ut.edu.project.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder);
        authProvider.setHideUserNotFoundExceptions(false);
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Cấu hình CSRF với CookieCsrfTokenRepository
        CookieCsrfTokenRepository tokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName("_csrf");

        return http
                // Bật CSRF với cấu hình cookie
                .csrf(csrf -> csrf
                        .csrfTokenRepository(tokenRepository)
                        .csrfTokenRequestHandler(requestHandler)
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().migrateSession()
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/payments",
                                "/",
                                "/home",
                                "/api/auth/**",
                                "/auth/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/assets/**",
                                "/homestay/**",
                                "/login",
                                "/login-processing"
                        ).permitAll()
                        .requestMatchers(
                                "/user/profile",
                                "/user/profile/update"
                        ).hasAnyAuthority("USER", "ADMIN")
                        .requestMatchers("/user/all").hasAuthority("ADMIN")
                        .requestMatchers("/admin/**", "/api/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/user/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/user/**").hasAuthority("ADMIN")
                        .requestMatchers("/dashboard").hasAnyAuthority("USER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/auth/login-user")
                        .loginProcessingUrl("/login-processing")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/auth/login-user?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            logger.info("Đăng xuất thành công. Chuyển hướng đến trang chủ.");
                            response.sendRedirect("/home?logout=true");
                        })
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID", "jwtToken", "XSRF-TOKEN")
                        .permitAll()
                )
                // Thêm filter để log request
                .addFilterBefore((request, response, chain) -> {
                    if (request instanceof HttpServletRequest) {
                        HttpServletRequest httpRequest = (HttpServletRequest) request;
                        logger.info("Request to: {}", httpRequest.getRequestURI());
                        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                        if (authentication != null && authentication.isAuthenticated()) {
                            logger.info("User authenticated: {}", authentication.getName());
                        } else {
                            logger.info("User not authenticated.");
                        }
                    }
                    chain.doFilter(request, response);
                }, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
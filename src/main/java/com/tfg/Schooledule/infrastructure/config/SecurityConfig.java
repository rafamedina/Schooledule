package com.tfg.Schooledule.infrastructure.config;

import com.tfg.Schooledule.infrastructure.security.CustomLoginSuccessHandler;
import com.tfg.Schooledule.infrastructure.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private CustomLoginSuccessHandler successHandler;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // @Bean
    // public SecurityFilterChain securityFilterChain(HttpSecurity http) throws
    // Exception {
    // http
    // .authorizeHttpRequests(auth -> auth
    // .requestMatchers("/admin/**").hasRole("ADMIN")
    // .requestMatchers("/profesor/**").hasRole("PROFESOR")
    // .requestMatchers("/alumno/**").hasRole("ALUMNO")
    // .requestMatchers("/", "/login", "/css/**", "/js/**",
    // "/images/**").permitAll()
    // .anyRequest().authenticated())
    // .formLogin(form -> form
    // .loginPage("/login")
    // .permitAll())
    // .logout(logout -> logout
    // .permitAll());
    //
    // return http.build();
    // }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. DESACTIVAR CSRF (Vital para desarrollo y Postman)
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // 2. Roles específicos
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/profesor/**").hasRole("PROFESOR")
                        .requestMatchers("/alumno/**").hasRole("ALUMNO")

                        // 3. Rutas públicas (¡OJO! /loginSession debe estar aquí para tu Postman)
                        .requestMatchers("/", "/login", "/loginSession", "/register", "/css/**", "/js/**", "/images/**",
                                "/error")
                        .permitAll()

                        // 4. El resto requiere estar logueado
                        .anyRequest().authenticated())

                .formLogin(form -> form
                        .loginPage("/login")
                        // AQUI ESTA LA MAGIA:
                        .loginProcessingUrl("/login")
                        .successHandler(successHandler)
                        .permitAll())

                .logout(logout -> logout
                        // Configuración para cerrar sesión
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login?logout")
                        .permitAll());

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

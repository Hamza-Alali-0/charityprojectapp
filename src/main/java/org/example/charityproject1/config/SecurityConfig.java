package org.example.charityproject1.config;

import org.example.charityproject1.filter.JwtAuthenticationFilter;
import org.example.charityproject1.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(CustomUserDetailsService userDetailsService, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Public routes
                        .requestMatchers("/","/auth/**", "/accueil", "/about", "/organizations","/organization/**", "/contact", "/campaigns","/campaigns/**").permitAll()
                        .requestMatchers("/styles/**", "/js/**", "/images/**","/accueil").permitAll()

                        // Role-specific dashboard access
                        .requestMatchers("/superadmin/dashboard").hasAuthority("ROLE_SUPER_ADMIN")
                        .requestMatchers("/organisation/dashboard").hasAuthority("ROLE_ORGANISATION")
                        .requestMatchers("/user/dashboard").hasAuthority("ROLE_USER")

                        // API access restrictions
                        .requestMatchers("/api/superadmin/**").hasAuthority("ROLE_SUPER_ADMIN")

                        // All other requests need authentication
                        .anyRequest().authenticated()
                )
 .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessHandler((request, response, authentication) -> {
                    // Default redirect path
                    String redirectUrl = "/accueil";
                    
                    // Check for role-based redirection
                    if (authentication != null && authentication.getAuthorities() != null) {
                        for (GrantedAuthority authority : authentication.getAuthorities()) {
                            if ("ROLE_SUPER_ADMIN".equals(authority.getAuthority())) {
                                redirectUrl = "/auth/login/superadmin";
                                break;
                            } else if ("ROLE_ORGANISATION".equals(authority.getAuthority())) {
                                redirectUrl = "/auth/login/organisation";
                                break;
                            }
                        }
                    }
                    
                    // Redirect to the appropriate login page
                    response.sendRedirect(redirectUrl);
                })
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
            )

                // Use ALWAYS to maintain session for web views
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // Handle unauthorized access better
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            // Redirect based on requested URL
                            if (request.getRequestURI().contains("/superadmin")) {
                                response.sendRedirect("/auth/login/superadmin");
                            } else if (request.getRequestURI().contains("/organisation")) {
                                response.sendRedirect("/auth/login/organisation");
                            } else {
                                response.sendRedirect("/auth/login/user");
                            }
                        })
                        .authenticationEntryPoint((request, response, authException) -> {
                            // Similar path-based redirect logic
                            if (request.getRequestURI().contains("/superadmin")) {
                                response.sendRedirect("/auth/login/superadmin");
                            } else if (request.getRequestURI().contains("/organisation")) {
                                response.sendRedirect("/auth/login/organisation");
                            } else {
                                response.sendRedirect("/auth/login/user");
                            }
                        })
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
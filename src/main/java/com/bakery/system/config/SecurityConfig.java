package com.bakery.system.config;

import com.bakery.system.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomLoginSuccessHandler successHandler;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomLoginSuccessHandler successHandler,
                          CustomUserDetailsService userDetailsService) {
        this.successHandler = successHandler;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/demo-payment/**", "/chatbot/**", "/api/**")
                )

                .authorizeHttpRequests(auth -> auth
                        // ===== 1. Public pages – no login needed =====
                        .requestMatchers(
                                "/login", "/register", "/forgot-password", "/reset-password",
                                "/css/**", "/js/**", "/images/**", "/webjars/**",
                                "/uploads/**",   // product images visible to guests
                                "/chatbot/**", "/chatbot/ask",
                                "/api/user/role", "/api/orders/pending-count",
                                "/api/catering/active-count"
                        ).permitAll()

                        // Product browsing (GET) – anyone can view the catalogue
                        .requestMatchers(HttpMethod.GET, "/", "/home", "/product/**").permitAll()

                        // ===== 2. Everything else requires authentication =====
                        // Review submission (POST)
                        .requestMatchers(HttpMethod.POST, "/product/*/review").authenticated()

                        // Cart, checkout, orders, profile, catering – must be logged in
                        .requestMatchers(
                                "/cart/**", "/checkout", "/my-orders", "/order/**",
                                "/profile/**", "/catering/**"
                        ).authenticated()

                        // ===== 3. Staff / Admin / Super Admin only =====
                        .requestMatchers("/admin/**").hasAuthority("ROLE_SUPER_ADMIN")

                        .requestMatchers(
                                "/staff/orders/**",
                                "/categories/**",
                                "/promotions/**",
                                "/staff/setmenus/**",
                                "/staff/delivery/**",
                                "/staff/delivery-settings/**",
                                "/staff/reviews/**"
                        ).hasAnyAuthority("ROLE_STAFF", "ROLE_ADMIN", "ROLE_SUPER_ADMIN")

                        .requestMatchers(
                                "/dashboard", "/add-product", "/edit-product/**",
                                "/delete-product/**", "/inventory", "/update-stock/**",
                                "/staff/catering/**", "/reports/**",
                                "/products"   // ← Product Backlog page
                        ).hasAnyAuthority("ROLE_STAFF", "ROLE_ADMIN", "ROLE_SUPER_ADMIN")

                        // ===== 4. Driver only =====
                        .requestMatchers("/driver/**").hasAuthority("ROLE_DRIVER")

                        // Catch-all
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(successHandler)   // handles redirects per role
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/home?logout")   // redirect to guest dashboard with logout toast
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }
}
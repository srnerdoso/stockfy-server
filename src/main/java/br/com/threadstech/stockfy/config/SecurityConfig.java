package br.com.threadstech.stockfy.config;

import br.com.threadstech.stockfy.modules.users.infrastructure.security.JwtAuthenticationFilter;
import br.com.threadstech.stockfy.modules.users.infrastructure.security.RateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final RateLimitFilter rateLimitFilter;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            exception ->
                exception
                    .authenticationEntryPoint(
                        (request, response, authException) ->
                            response.sendError(
                                org.springframework.http.HttpStatus.UNAUTHORIZED.value()))
                    .accessDeniedHandler(
                        (request, response, accessDeniedException) ->
                            response.sendError(
                                org.springframework.http.HttpStatus.FORBIDDEN.value())))
        .addFilterBefore(
            rateLimitFilter,
            org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
                .class)
        .addFilterAfter(jwtAuthenticationFilter, RateLimitFilter.class)
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        org.springframework.http.HttpMethod.POST, "/api/v1/auth/sessions/**")
                    .permitAll()
                    .requestMatchers(
                        org.springframework.http.HttpMethod.PATCH, "/api/v1/users/password")
                    .permitAll()
                    .requestMatchers("/api/v1/**")
                    .authenticated()
                    .anyRequest()
                    .permitAll());

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}

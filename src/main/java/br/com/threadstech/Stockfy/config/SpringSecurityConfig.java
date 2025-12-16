package br.com.threadstech.stockfy.config;

import br.com.threadstech.stockfy.api.ApiPaths;
import br.com.threadstech.stockfy.enums.Role;
import br.com.threadstech.stockfy.security.jwt.JwtAccessDeniedHandler;
import br.com.threadstech.stockfy.security.jwt.JwtAuthenticationEntryPoint;
import br.com.threadstech.stockfy.security.jwt.JwtAuthorizationFilter;
import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

// TODO: Implementar cors para permitir apenas aplicativos mobile permitidos
@EnableWebMvc
@Configuration
@EnableMethodSecurity
public class SpringSecurityConfig {

  private static final String[] DOCUMENTATION_OPENAPI = {
    "/docs/index.html",
    "/docs-stockfy.html",
    "/docs-stockfy/**",
    "/v3/api-docs/**",
    "/swagger-ui-custom.html",
    "/swagger-ui.html",
    "/swagger-ui/**",
    "/**.html",
    "/webjars/**",
    "/configuration/**",
    "/swagger-resources/**"
  };

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) {
    return http.csrf(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            config ->
                config
                    .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                    .accessDeniedHandler(new JwtAccessDeniedHandler()))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(DOCUMENTATION_OPENAPI)
                    .permitAll()
                    .requestMatchers(toPatternPath(ApiPaths.AUTH))
                    .permitAll()
                    .requestMatchers(new String[] {ApiPaths.CUSTOMER, ApiPaths.EMPLOYEE})
                    .hasAnyRole(Role.ADMIN.name())
                    .requestMatchers(toPatternPaths(ApiPaths.CUSTOMER, ApiPaths.EMPLOYEE))
                    .hasAnyRole(Role.ADMIN.name())
                    .requestMatchers(toPatternPath(ApiPaths.PAYMENT))
                    .hasAnyRole(Role.ADMIN.name(), Role.SALES_ATTENDANT.name())
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  @Bean
  public JwtAuthorizationFilter jwtFilter() {
    return new JwtAuthorizationFilter();
  }

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  private String toPatternPath(String basePath) {
    return basePath + "/**";
  }

  private String[] toPatternPaths(String... basePaths) {
    return Arrays.stream(basePaths).map(this::toPatternPath).toArray(String[]::new);
  }
}

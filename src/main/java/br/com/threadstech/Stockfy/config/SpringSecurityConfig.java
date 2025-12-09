package br.com.threadstech.stockfy.config;

import br.com.threadstech.stockfy.api.ApiPaths;
import br.com.threadstech.stockfy.security.jwt.JwtAccessDeniedHandler;
import br.com.threadstech.stockfy.security.jwt.JwtAuthenticationEntryPoint;
import br.com.threadstech.stockfy.security.jwt.JwtAuthorizationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
                    .requestMatchers(ApiPaths.AUTH + "/*")
                    .permitAll()
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
}

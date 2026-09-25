package com.frequency.ticketing.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.frequency.ticketing.web.dto.ApiError;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * Genuinely default-deny (spec 005 FR-006, amended): {@code permitAll} ONLY on login and the
 * operational endpoints outside this feature's scope; {@code anyRequest().authenticated()} for
 * everything else, including {@code /api/v1/chatbot/**} before its controller even exists
 * (research.md "Endpoint access rules"). CSRF is disabled — this is a JSON-only API with no
 * server-rendered forms (research.md "CORS and CSRF").
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private final ObjectMapper objectMapper;

  public SecurityConfig(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
      throws Exception {
    return configuration.getAuthenticationManager();
  }

  @Bean
  public SecurityContextRepository securityContextRepository() {
    return new HttpSessionSecurityContextRepository();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(
            authorize ->
                authorize
                    // CORS preflight requests carry no credentials and must reach WebMvc's CORS
                    // handling (CorsConfig) rather than be rejected here first.
                    .requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    .requestMatchers("/api/v1/auth/login")
                    .permitAll()
                    .requestMatchers("/actuator/health", "/actuator/info")
                    .permitAll()
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            exceptionHandling ->
                exceptionHandling
                    .authenticationEntryPoint(
                        (request, response, authException) ->
                            writeError(
                                response,
                                HttpServletResponse.SC_UNAUTHORIZED,
                                ApiError.Code.UNAUTHENTICATED,
                                "Authentication is required",
                                request.getRequestURI()))
                    .accessDeniedHandler(
                        (request, response, accessDeniedException) ->
                            writeError(
                                response,
                                HttpServletResponse.SC_FORBIDDEN,
                                ApiError.Code.FORBIDDEN,
                                "This action is not permitted for your role",
                                request.getRequestURI())));
    return http.build();
  }

  private void writeError(
      HttpServletResponse response, int status, ApiError.Code code, String message, String path)
      throws java.io.IOException {
    response.setStatus(status);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(response.getWriter(), ApiError.of(code, message, path));
  }
}

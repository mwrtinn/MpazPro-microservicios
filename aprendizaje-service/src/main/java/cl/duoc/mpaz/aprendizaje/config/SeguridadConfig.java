package cl.duoc.mpaz.aprendizaje.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.List;

// Configuración de seguridad del microservicio de aprendizaje.
@Configuration
public class SeguridadConfig {

    // Valida los tokens emitidos por usuarios-service.
    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${mpaz.jwt.secret}") String secret) {

        byte[] clave = Base64.getDecoder().decode(secret);

        SecretKeySpec key =
                new SecretKeySpec(clave, "HmacSHA256");

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        JwtClaimValidator<List<String>> audiencia =
                new JwtClaimValidator<>("aud",
                        aud -> aud != null && aud.contains("mpaz-api"));

        JwtClaimValidator<Object> vencimiento =
                new JwtClaimValidator<>("exp", exp -> exp != null);

        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(
                        JwtValidators.createDefaultWithIssuer(
                                "mpaz-usuarios-service"),
                        audiencia,
                        vencimiento
                )
        );

        return decoder;
    }

    // Recupera el rol del usuario desde el token JWT.
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter roles =
                new JwtGrantedAuthoritiesConverter();

        roles.setAuthoritiesClaimName("rol");
        roles.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(roles);

        return converter;
    }

    // Define los permisos de acceso según el perfil del usuario.
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter converter) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // Consulta de material educativo.
                        .requestMatchers(HttpMethod.GET,
                                "/api/objetivos/**",
                                "/api/contenidos/**",
                                "/api/preguntas/**")
                        .hasAnyRole("ESTUDIANTE", "DOCENTE", "UTP")

                        // Administración de objetivos y contenidos.
                        .requestMatchers(HttpMethod.POST,
                                "/api/objetivos/**",
                                "/api/contenidos/**",
                                "/api/preguntas/**")
                        .hasRole("DOCENTE")

                        .requestMatchers(HttpMethod.PUT,
                                "/api/objetivos/**",
                                "/api/contenidos/**",
                                "/api/preguntas/**")
                        .hasRole("DOCENTE")

                        .requestMatchers(HttpMethod.DELETE,
                                "/api/objetivos/**",
                                "/api/contenidos/**",
                                "/api/preguntas/**")
                        .hasRole("DOCENTE")

                        // Revisión de contenidos por UTP.
                        .requestMatchers(HttpMethod.PATCH,
                                "/api/contenidos/*/estado")
                        .hasRole("UTP")

                        // Registro de respuestas del estudiante.
                        .requestMatchers(HttpMethod.POST,
                                "/api/respuestas")
                        .hasRole("ESTUDIANTE")

                        // Consulta de resultados e intentos.
                        .requestMatchers(HttpMethod.GET,
                                "/api/respuestas/**",
                                "/api/intentos/**")
                        .hasAnyRole("ESTUDIANTE", "DOCENTE")

                        // Informes reservados para el docente.
                        .requestMatchers("/api/informes/**")
                        .hasRole("DOCENTE")

                        .anyRequest().denyAll()
                )

                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(converter)));

        return http.build();
    }
}
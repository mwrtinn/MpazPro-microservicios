package cl.duoc.mpaz.usuarios.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.List;

// Configuración de seguridad y permisos del microservicio.
@Configuration
public class SeguridadConfig {

    // Protege las contraseñas mediante BCrypt.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Valida la firma, el emisor y el vencimiento del token JWT.
    // Valida la firma, el emisor y el vencimiento del token JWT.
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

    // Obtiene los permisos desde el rol incluido en el token.
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

    // Define los endpoints públicos y los permisos por perfil.
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

                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/login").permitAll()

                        .requestMatchers(HttpMethod.GET,
                                "/api/usuarios",
                                "/api/usuarios/**")
                        .hasAnyRole("DOCENTE", "UTP")

                        .requestMatchers(HttpMethod.POST,
                                "/api/usuarios").hasRole("UTP")

                        .requestMatchers(HttpMethod.PUT,
                                "/api/usuarios/**").hasRole("UTP")

                        .requestMatchers(HttpMethod.DELETE,
                                "/api/usuarios/**").hasRole("UTP")

                        .anyRequest().denyAll()
                )

                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(converter)));

        return http.build();
    }
}
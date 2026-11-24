package cl.duoc.mpaz.usuarios.security;

import cl.duoc.mpaz.usuarios.dto.UsuarioResponse;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

@Service
public class JwtService {

    private final JwtEncoder encoder;

    public JwtService(
            @Value("${mpaz.jwt.secret}") String secret) {

        byte[] clave = Base64.getDecoder().decode(secret);

        if (clave.length < 32) {
            throw new IllegalArgumentException(
                    "La clave JWT debe tener al menos 32 bytes");
        }

        SecretKeySpec key =
                new SecretKeySpec(clave, "HmacSHA256");

        this.encoder =
                new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    public String generarToken(UsuarioResponse usuario) {

        Instant ahora = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("mpaz-usuarios-service")
                .subject(usuario.rut())
                .audience(List.of("mpaz-api"))
                .issuedAt(ahora)
                .expiresAt(ahora.plusSeconds(3600))
                .claim("rol", usuario.rol())
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        return encoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }
}

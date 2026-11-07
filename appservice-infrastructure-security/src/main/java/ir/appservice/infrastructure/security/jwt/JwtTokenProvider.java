package ir.appservice.infrastructure.security.jwt;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * JWT Token Provider & Decoder Factory.
 * Supports generating and validating signed JWTs with RS256 (RSA) and ES256 (Elliptic Curve) signatures.
 */
public class JwtTokenProvider {

    private final RSAKey rsaKey;
    private final ECKey ecKey;
    private final String issuer;

    public JwtTokenProvider() {
        this("https://auth.appservice.ir");
    }

    public JwtTokenProvider(String issuer) {
        this.issuer = issuer;
        this.rsaKey = generateRsaKey();
        this.ecKey = generateEcKey();
    }

    public static RSAKey generateRsaKey() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();
            return new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                    .privateKey((RSAPrivateKey) keyPair.getPrivate())
                    .keyID(UUID.randomUUID().toString())
                    .build();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("RSA algorithm not available", e);
        }
    }

    public static ECKey generateEcKey() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
            generator.initialize(256);
            KeyPair keyPair = generator.generateKeyPair();
            return new ECKey.Builder(Curve.P_256, (ECPublicKey) keyPair.getPublic())
                    .privateKey((ECPrivateKey) keyPair.getPrivate())
                    .keyID(UUID.randomUUID().toString())
                    .build();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("EC algorithm not available", e);
        }
    }

    public RSAKey getRsaKey() {
        return rsaKey;
    }

    public ECKey getEcKey() {
        return ecKey;
    }

    public JwtDecoder createRsaJwtDecoder() {
        try {
            return NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize RSA JwtDecoder", e);
        }
    }

    public JwtDecoder createEcJwtDecoder() {
        try {
            JWKSet jwkSet = new JWKSet(ecKey.toPublicJWK());
            JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(jwkSet);
            JWSKeySelector<SecurityContext> jwsKeySelector = new JWSVerificationKeySelector<>(JWSAlgorithm.ES256, jwkSource);
            DefaultJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
            jwtProcessor.setJWSKeySelector(jwsKeySelector);
            return new NimbusJwtDecoder(jwtProcessor);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize EC JwtDecoder", e);
        }
    }

    public String createSignedToken(String subject, String tenantId, List<String> roles, List<String> permissions, long durationSeconds) {
        return createSignedToken(subject, tenantId, roles, permissions, durationSeconds, JWSAlgorithm.RS256);
    }

    public String createSignedToken(String subject, String tenantId, List<String> roles, List<String> permissions, long durationSeconds, JWSAlgorithm algorithm) {
        try {
            Instant now = Instant.now();
            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                    .issuer(this.issuer)
                    .subject(subject)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plus(durationSeconds, ChronoUnit.SECONDS)))
                    .jwtID(UUID.randomUUID().toString())
                    .claim("tenant_id", tenantId);

            if (roles != null && !roles.isEmpty()) {
                claimsBuilder.claim("roles", roles);
            }
            if (permissions != null && !permissions.isEmpty()) {
                claimsBuilder.claim("permissions", permissions);
            }

            JWTClaimsSet claimsSet = claimsBuilder.build();

            if (JWSAlgorithm.ES256.equals(algorithm)) {
                JWSSigner signer = new ECDSASigner(ecKey.toECPrivateKey());
                JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(ecKey.getKeyID()).build();
                SignedJWT signedJWT = new SignedJWT(header, claimsSet);
                signedJWT.sign(signer);
                return signedJWT.serialize();
            } else {
                JWSSigner signer = new RSASSASigner(rsaKey.toRSAPrivateKey());
                JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build();
                SignedJWT signedJWT = new SignedJWT(header, claimsSet);
                signedJWT.sign(signer);
                return signedJWT.serialize();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error signing JWT token", e);
        }
    }

    public String createCustomToken(JWTClaimsSet claimsSet, JWSAlgorithm algorithm) {
        try {
            if (JWSAlgorithm.ES256.equals(algorithm)) {
                JWSSigner signer = new ECDSASigner(ecKey.toECPrivateKey());
                JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(ecKey.getKeyID()).build();
                SignedJWT signedJWT = new SignedJWT(header, claimsSet);
                signedJWT.sign(signer);
                return signedJWT.serialize();
            } else {
                JWSSigner signer = new RSASSASigner(rsaKey.toRSAPrivateKey());
                JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build();
                SignedJWT signedJWT = new SignedJWT(header, claimsSet);
                signedJWT.sign(signer);
                return signedJWT.serialize();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error signing custom JWT token", e);
        }
    }
}

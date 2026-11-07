package ir.appservice.infrastructure.security.pkce;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * Proof Key for Code Exchange (PKCE) Challenge Verifier compliant with RFC 7636 and OAuth 2.1.
 * Enforces SHA-256 (S256) challenge transformation and timing-safe constant-time string comparison.
 */
public class PkceChallengeVerifier {

    public static final String METHOD_S256 = "S256";
    public static final String METHOD_PLAIN = "plain";

    // RFC 7636 Section 4.1: [A-Z] / [a-z] / [0-9] / "-" / "." / "_" / "~", length 43-128
    private static final Pattern VERIFIER_PATTERN = Pattern.compile("^[a-zA-Z0-9\\-._~]{43,128}$");

    /**
     * Verifies that code_verifier transforms to code_challenge using specified method.
     *
     * @param codeVerifier   Plaintext verifier provided by client during token exchange
     * @param codeChallenge  Challenge string supplied during initial authorization request
     * @param method         Transformation algorithm ('S256' or 'plain')
     * @return true if verifier matches challenge; false otherwise
     */
    public boolean verify(String codeVerifier, String codeChallenge, String method) {
        if (codeVerifier == null || codeChallenge == null || method == null) {
            return false;
        }

        if (!isValidCodeVerifier(codeVerifier)) {
            return false;
        }

        String normalizedMethod = method.trim();
        if (METHOD_S256.equalsIgnoreCase(normalizedMethod)) {
            String computedChallenge = computeS256Challenge(codeVerifier);
            return timingSafeEquals(computedChallenge, codeChallenge.trim());
        } else if (METHOD_PLAIN.equalsIgnoreCase(normalizedMethod)) {
            // OAuth 2.1 strictly deprecates 'plain', but supported if explicit
            return timingSafeEquals(codeVerifier.trim(), codeChallenge.trim());
        }

        return false;
    }

    /**
     * Checks if code_verifier conforms to RFC 7636 length and character set bounds.
     */
    public boolean isValidCodeVerifier(String codeVerifier) {
        if (codeVerifier == null) {
            return false;
        }
        return VERIFIER_PATTERN.matcher(codeVerifier.trim()).matches();
    }

    /**
     * Computes BASE64URL-ENCODE(SHA256(ASCII(code_verifier))) without padding characters.
     */
    public String computeS256Challenge(String codeVerifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.trim().getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available in current JVM", e);
        }
    }

    /**
     * Performs constant-time byte comparison to protect against side-channel timing attacks.
     */
    public boolean timingSafeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(aBytes, bBytes);
    }
}

package ir.appservice.infrastructure.security;

import ir.appservice.infrastructure.security.pkce.PkceChallengeVerifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PKCE Challenge Verifier Tests (RFC 7636 / OAuth 2.1)")
class PkceChallengeVerifierTest {

    private final PkceChallengeVerifier verifier = new PkceChallengeVerifier();

    @Test
    @DisplayName("RFC 7636 Appendix B: verify official test vector for S256")
    void testOfficialRfcTestVector() {
        // RFC 7636 Appendix B test vector
        String codeVerifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";
        String expectedChallenge = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM";

        String computedChallenge = verifier.computeS256Challenge(codeVerifier);
        assertThat(computedChallenge).isEqualTo(expectedChallenge);

        boolean result = verifier.verify(codeVerifier, expectedChallenge, "S256");
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Verify rejection of altered or mismatched code verifier")
    void testMismatchedVerifier() {
        String codeVerifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";
        String alteredVerifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXm";
        String expectedChallenge = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM";

        boolean result = verifier.verify(alteredVerifier, expectedChallenge, "S256");
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Verify code verifier length validation boundaries (43 to 128 characters)")
    void testLengthBoundaries() {
        // 42 characters (too short)
        String tooShort = "a".repeat(42);
        assertThat(verifier.isValidCodeVerifier(tooShort)).isFalse();

        // 43 characters (minimum allowed)
        String minValid = "a".repeat(43);
        assertThat(verifier.isValidCodeVerifier(minValid)).isTrue();

        // 128 characters (maximum allowed)
        String maxValid = "a".repeat(128);
        assertThat(verifier.isValidCodeVerifier(maxValid)).isTrue();

        // 129 characters (too long)
        String tooLong = "a".repeat(129);
        assertThat(verifier.isValidCodeVerifier(tooLong)).isFalse();
    }

    @Test
    @DisplayName("Verify code verifier character set restrictions (unreserved chars only)")
    void testCharacterSet() {
        String validSpecialChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-._~";
        assertThat(validSpecialChars.length()).isGreaterThanOrEqualTo(43);
        assertThat(verifier.isValidCodeVerifier(validSpecialChars)).isTrue();

        // Contains invalid symbols like + / = %
        String invalidChars = "a".repeat(42) + "+";
        assertThat(verifier.isValidCodeVerifier(invalidChars)).isFalse();
    }

    @Test
    @DisplayName("Verify 'plain' method and unknown method handling")
    void testPlainAndUnknownMethods() {
        String verifierStr = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";

        assertThat(verifier.verify(verifierStr, verifierStr, "plain")).isTrue();
        assertThat(verifier.verify(verifierStr, "different", "plain")).isFalse();
        assertThat(verifier.verify(verifierStr, verifierStr, "UNKNOWN_METHOD")).isFalse();
        assertThat(verifier.verify(null, verifierStr, "S256")).isFalse();
        assertThat(verifier.verify(verifierStr, null, "S256")).isFalse();
    }
}

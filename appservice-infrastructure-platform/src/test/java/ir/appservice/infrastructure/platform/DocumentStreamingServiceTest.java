package ir.appservice.infrastructure.platform;

import ir.appservice.infrastructure.platform.document.DocumentStreamingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Document Streaming & SHA-256 Deduplication Tests")
class DocumentStreamingServiceTest {

    private final DocumentStreamingService streamingService = new DocumentStreamingService();

    @Test
    @DisplayName("Non-blocking chunked streaming calculates accurate SHA-256 and detects duplicate uploads")
    void testStreamingAndDeduplication() throws IOException, NoSuchAlgorithmException {
        // Prepare 64KB test payload
        byte[] payload = "AppService Enterprise Platform Document Streaming Binary Data Payload Chunk test".repeat(800)
                .getBytes(StandardCharsets.UTF_8);

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String expectedSha256 = HexFormat.of().formatHex(digest.digest(payload));

        ByteArrayInputStream sourceStream1 = new ByteArrayInputStream(payload);
        ByteArrayOutputStream targetStream1 = new ByteArrayOutputStream();

        // Transfer 1: First upload (not duplicate)
        DocumentStreamingService.StreamTransferResult result1 = streamingService.streamWithChecksum(sourceStream1, targetStream1, 4096);

        assertThat(result1.totalBytesTransferred()).isEqualTo(payload.length);
        assertThat(result1.sha256Hex()).isEqualTo(expectedSha256);
        assertThat(result1.isDuplicate()).isFalse();
        assertThat(targetStream1.toByteArray()).isEqualTo(payload);

        // Transfer 2: Uploading exact same document again -> isDuplicate must be true
        ByteArrayInputStream sourceStream2 = new ByteArrayInputStream(payload);
        ByteArrayOutputStream targetStream2 = new ByteArrayOutputStream();

        DocumentStreamingService.StreamTransferResult result2 = streamingService.streamWithChecksum(sourceStream2, targetStream2, 4096);
        assertThat(result2.totalBytesTransferred()).isEqualTo(payload.length);
        assertThat(result2.sha256Hex()).isEqualTo(expectedSha256);
        assertThat(result2.isDuplicate()).isTrue();
    }
}

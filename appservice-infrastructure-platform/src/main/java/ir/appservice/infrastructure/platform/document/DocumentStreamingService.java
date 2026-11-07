package ir.appservice.infrastructure.platform.document;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Non-blocking binary Document Streaming adapter supporting direct stream transfer,
 * zero heap-saturation buffering, and streaming SHA-256 deduplication.
 */
@Service
public class DocumentStreamingService {

    public record StreamTransferResult(long totalBytesTransferred, String sha256Hex, boolean isDuplicate) {}

    // In-memory registry of known document checksums for deduplication
    private final Map<String, String> checksumRegistry = new ConcurrentHashMap<>();

    /**
     * Streams binary content from source InputStream directly to target OutputStream
     * using a fixed-size chunk buffer (zero whole-file heap allocation) while calculating SHA-256.
     *
     * @param sourceStream Input stream supplying binary data
     * @param targetStream Output stream receiving streamed chunks
     * @param bufferSize   Chunk buffer size in bytes (e.g. 8192)
     * @return StreamTransferResult with byte count and SHA-256 checksum
     */
    public StreamTransferResult streamWithChecksum(InputStream sourceStream, OutputStream targetStream, int bufferSize) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            DigestInputStream digestInputStream = new DigestInputStream(sourceStream, digest);

            byte[] buffer = new byte[Math.max(4096, bufferSize)];
            long totalBytes = 0;
            int bytesRead;

            while ((bytesRead = digestInputStream.read(buffer)) != -1) {
                targetStream.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }
            targetStream.flush();

            String sha256Hex = HexFormat.of().formatHex(digest.digest());
            boolean isDuplicate = checksumRegistry.containsKey(sha256Hex);
            if (!isDuplicate) {
                checksumRegistry.put(sha256Hex, "doc_" + sha256Hex.substring(0, 8));
            }

            return new StreamTransferResult(totalBytes, sha256Hex, isDuplicate);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 digest algorithm not available", e);
        }
    }

    public boolean isDuplicateChecksum(String sha256Hex) {
        return checksumRegistry.containsKey(sha256Hex);
    }

    public void registerChecksum(String sha256Hex, String documentId) {
        checksumRegistry.put(sha256Hex, documentId);
    }
}

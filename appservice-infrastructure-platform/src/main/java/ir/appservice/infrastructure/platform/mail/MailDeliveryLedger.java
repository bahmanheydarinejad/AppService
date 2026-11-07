package ir.appservice.infrastructure.platform.mail;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory thread-safe ledger recording email dispatch outcomes, bounces, and delivery metrics.
 */
public class MailDeliveryLedger {

    public enum DeliveryStatus {
        SENT,
        BOUNCED,
        FAILED
    }

    public record DeliveryRecord(
            String id,
            String recipient,
            String subject,
            DeliveryStatus status,
            Instant timestamp,
            String diagnosticMessage
    ) {}

    private final List<DeliveryRecord> records = new CopyOnWriteArrayList<>();

    public DeliveryRecord recordSent(String recipient, String subject) {
        DeliveryRecord record = new DeliveryRecord(
                UUID.randomUUID().toString(),
                recipient,
                subject,
                DeliveryStatus.SENT,
                Instant.now(),
                "Dispatched to SMTP transport successfully"
        );
        records.add(record);
        return record;
    }

    public DeliveryRecord recordBounce(String recipient, String subject, String bounceReason) {
        DeliveryRecord record = new DeliveryRecord(
                UUID.randomUUID().toString(),
                recipient,
                subject,
                DeliveryStatus.BOUNCED,
                Instant.now(),
                bounceReason
        );
        records.add(record);
        return record;
    }

    public DeliveryRecord recordFailure(String recipient, String subject, String error) {
        DeliveryRecord record = new DeliveryRecord(
                UUID.randomUUID().toString(),
                recipient,
                subject,
                DeliveryStatus.FAILED,
                Instant.now(),
                error
        );
        records.add(record);
        return record;
    }

    public List<DeliveryRecord> getRecords() {
        return Collections.unmodifiableList(records);
    }

    public long countSent() {
        return records.stream().filter(r -> r.status() == DeliveryStatus.SENT).count();
    }

    public long countBounces() {
        return records.stream().filter(r -> r.status() == DeliveryStatus.BOUNCED).count();
    }

    public void clear() {
        records.clear();
    }
}

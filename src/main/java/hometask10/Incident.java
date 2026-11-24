package hometask10;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public record Incident(
        String id,
        String service,
        Instant startedAt,
        Instant resolvedAt) {

    public Incident {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(service, "service must not be null");
        Objects.requireNonNull(startedAt, "startedAt must not be null");
        Objects.requireNonNull(resolvedAt, "resolvedAt must not be null");

        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (service.isBlank()) {
            throw new IllegalArgumentException("service must not be blank");
        }
        if (resolvedAt.isBefore(startedAt)) {
            throw new IllegalArgumentException("resolvedAt cannot be before startedAt");
        }
    }

    public Duration duration() {
        return Duration.between(startedAt, resolvedAt);
    }
}

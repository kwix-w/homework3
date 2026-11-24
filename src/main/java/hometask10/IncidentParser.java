package hometask10;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Objects;

public final class IncidentParser {

    private IncidentParser() {
    }

    public static Incident parse(String line) {
        Objects.requireNonNull(line, "line must not be null");

        String[] parts = line.split(";", -1);
        if (parts.length != 4) {
            throw new IllegalArgumentException("Expected 4 fields separated by ';', got: " + parts.length);
        }

        String id = parts[0].trim();
        String service = parts[1].trim();
        Instant startedAt;
        Instant resolvedAt;

        try {
            startedAt = Instant.parse(parts[2].trim());
            resolvedAt = Instant.parse(parts[3].trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid Instant format in line: " + line, e);
        }

        return new Incident(id, service, startedAt, resolvedAt);
    }
}
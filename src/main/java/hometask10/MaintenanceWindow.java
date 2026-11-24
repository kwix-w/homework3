package hometask10;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public record MaintenanceWindow(Instant start, Instant end) {

    public MaintenanceWindow {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("end cannot be before start");
        }
    }

    public Duration duration() {
        return Duration.between(start, end);
    }
}

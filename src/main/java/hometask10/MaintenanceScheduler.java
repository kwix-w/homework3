package hometask10;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.zone.ZoneOffsetTransition;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class MaintenanceScheduler {

    private final Clock clock;

    public MaintenanceScheduler(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    public MaintenanceWindow nextWindow(
            LocalDate date,
            LocalTime localStart,
            Duration duration,
            ZoneId zone,
            OverlapPolicy policy) {

        Objects.requireNonNull(date, "date must not be null");
        Objects.requireNonNull(localStart, "localStart must not be null");
        Objects.requireNonNull(duration, "duration must not be null");
        Objects.requireNonNull(zone, "zone must not be null");
        Objects.requireNonNull(policy, "policy must not be null");

        if (duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException("duration must be positive");
        }

        LocalDateTime localDateTime = LocalDateTime.of(date, localStart);
        ZoneOffsetTransition transition = zone.getRules().getTransition(localDateTime);
        if (transition != null && transition.isGap()) {
            throw new DateTimeException("Local time " + localDateTime + " falls into DST gap in zone " + zone);
        }

        ZonedDateTime zonedStart = ZonedDateTime.of(localDateTime, zone);
        if (policy == OverlapPolicy.EARLIER) {
            zonedStart = zonedStart.withEarlierOffsetAtOverlap();
        } else {
            zonedStart = zonedStart.withLaterOffsetAtOverlap();
        }

        Instant start = zonedStart.toInstant();
        Instant end = start.plus(duration);
        return new MaintenanceWindow(start, end);
    }

    public List<MaintenanceWindow> upcomingWindows(
            int days,
            LocalTime localStart,
            Duration duration,
            ZoneId zone,
            OverlapPolicy policy) {

        Objects.requireNonNull(localStart, "localStart must not be null");
        Objects.requireNonNull(duration, "duration must not be null");
        Objects.requireNonNull(zone, "zone must not be null");
        Objects.requireNonNull(policy, "policy must not be null");

        if (days < 0) {
            throw new IllegalArgumentException("days cannot be negative: " + days);
        }
        if (duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException("duration must be positive");
        }
        if (days == 0) {
            return List.of();
        }

        LocalDate startDate = LocalDate.now(clock.withZone(zone));
        List<MaintenanceWindow> result = new ArrayList<>(days);
        for (int i = 0; i < days; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            result.add(nextWindow(currentDate, localStart, duration, zone, policy));
        }
        return result;
    }
}
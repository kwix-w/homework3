package hometask10;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;

public final class LegacyDateAdapter {

    private LegacyDateAdapter() {
    }

    public static Instant toInstant(Date date) {
        Objects.requireNonNull(date, "date must not be null");
        return date.toInstant();
    }

    public static Date toDate(Instant instant) {
        Objects.requireNonNull(instant, "instant must not be null");
        return Date.from(instant);
    }

    public static ZonedDateTime toZonedDateTime(GregorianCalendar calendar) {
        Objects.requireNonNull(calendar, "calendar must not be null");
        return calendar.toZonedDateTime();
    }

    public static GregorianCalendar toGregorianCalendar(ZonedDateTime zonedDateTime) {
        Objects.requireNonNull(zonedDateTime, "zonedDateTime must not be null");
        return GregorianCalendar.from(zonedDateTime);
    }
}
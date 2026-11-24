package hometask10;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TimeServiceTest {

    private static final ZoneId BERLIN = ZoneId.of("Europe/Berlin");

    @Test
    void parseValidIncidentAndRejectInvalid() {
        Incident inc = IncidentParser.parse("INC-1;billing;2024-03-30T22:30:00Z;2024-03-31T01:00:00Z");
        assertEquals("INC-1", inc.id());
        assertEquals("billing", inc.service());
        assertEquals(Duration.ofMinutes(150), inc.duration());

        assertThrows(IllegalArgumentException.class, () ->
                IncidentParser.parse("INC-1;billing;not-a-timestamp;2024-03-31T01:00:00Z"));
        assertThrows(IllegalArgumentException.class, () ->
                IncidentParser.parse(";billing;2024-03-30T22:30:00Z;2024-03-31T01:00:00Z"));
        assertThrows(IllegalArgumentException.class, () ->
                IncidentParser.parse("INC-1;billing;2024-03-31T01:00:00Z;2024-03-30T22:30:00Z"));
    }

    @Test
    void analyticsTotalAverageLongestAndEmpty() {
        assertEquals(Duration.ZERO, IncidentAnalytics.totalDowntime(List.of()));
        assertEquals(Duration.ZERO, IncidentAnalytics.averageDowntime(List.of()));
        assertEquals(Optional.empty(), IncidentAnalytics.longestIncident(List.of()));

        Incident i1 = new Incident("INC-2", "auth",
                Instant.parse("2024-03-30T10:00:00Z"),
                Instant.parse("2024-03-30T10:00:00.000000010Z"));
        Incident i2 = new Incident("INC-1", "auth",
                Instant.parse("2024-03-30T11:00:00Z"),
                Instant.parse("2024-03-30T11:00:00.000000011Z"));

        List<Incident> list = List.of(i1, i2);
        assertEquals(Duration.ofNanos(21), IncidentAnalytics.totalDowntime(list));
        assertEquals(Duration.ofNanos(10), IncidentAnalytics.averageDowntime(list));
        assertEquals(Optional.of(i2), IncidentAnalytics.longestIncident(list));
    }

    @Test
    void groupByStartDateInBerlinZone() {
        Incident inc1 = IncidentParser.parse("INC-1;billing;2024-03-30T22:30:00Z;2024-03-31T01:00:00Z");
        Incident inc2 = IncidentParser.parse("INC-2;gateway;2024-03-30T23:30:00Z;2024-03-31T00:15:00Z");

        Map<LocalDate, List<Incident>> grouped = IncidentAnalytics.groupByStartDate(List.of(inc2, inc1), BERLIN);

        assertEquals(List.of(LocalDate.of(2024, 3, 30), LocalDate.of(2024, 3, 31)),
                List.copyOf(grouped.keySet()));
        assertEquals(List.of(inc1), grouped.get(LocalDate.of(2024, 3, 30)));
        assertEquals(List.of(inc2), grouped.get(LocalDate.of(2024, 3, 31)));
    }

    @Test
    void schedulerRejectsDstGap() {
        Clock clock = Clock.fixed(Instant.parse("2024-03-30T00:00:00Z"), ZoneOffset.UTC);
        MaintenanceScheduler scheduler = new MaintenanceScheduler(clock);

        assertThrows(DateTimeException.class, () ->
                scheduler.nextWindow(
                        LocalDate.of(2024, 3, 31),
                        LocalTime.of(2, 30),
                        Duration.ofMinutes(30),
                        BERLIN,
                        OverlapPolicy.EARLIER));
    }

    @Test
    void schedulerHandlesDstOverlapExplicitly() {
        Clock clock = Clock.fixed(Instant.parse("2024-10-26T00:00:00Z"), ZoneOffset.UTC);
        MaintenanceScheduler scheduler = new MaintenanceScheduler(clock);

        MaintenanceWindow earlier = scheduler.nextWindow(
                LocalDate.of(2024, 10, 27),
                LocalTime.of(2, 30),
                Duration.ofMinutes(30),
                BERLIN,
                OverlapPolicy.EARLIER);

        MaintenanceWindow later = scheduler.nextWindow(
                LocalDate.of(2024, 10, 27),
                LocalTime.of(2, 30),
                Duration.ofMinutes(30),
                BERLIN,
                OverlapPolicy.LATER);

        assertEquals(ZoneOffset.of("+02:00"), earlier.start().atZone(BERLIN).getOffset());
        assertEquals(ZoneOffset.of("+01:00"), later.start().atZone(BERLIN).getOffset());
        assertEquals(Duration.ofHours(1), Duration.between(earlier.start(), later.start()));
    }

    @Test
    void upcomingWindowsAcrossSpringForwardAndDeterministicClock() {
        Clock clock = Clock.fixed(Instant.parse("2024-03-30T00:00:00Z"), ZoneOffset.UTC);
        MaintenanceScheduler scheduler = new MaintenanceScheduler(clock);

        List<MaintenanceWindow> firstRun = scheduler.upcomingWindows(
                2, LocalTime.of(3, 30), Duration.ofMinutes(30), BERLIN, OverlapPolicy.EARLIER);
        List<MaintenanceWindow> secondRun = scheduler.upcomingWindows(
                2, LocalTime.of(3, 30), Duration.ofMinutes(30), BERLIN, OverlapPolicy.EARLIER);

        assertEquals(firstRun, secondRun);
        assertEquals(2, firstRun.size());

        ZonedDateTime zdt1 = firstRun.get(0).start().atZone(BERLIN);
        ZonedDateTime zdt2 = firstRun.get(1).start().atZone(BERLIN);
        assertEquals(LocalTime.of(3, 30), zdt1.toLocalTime());
        assertEquals(LocalTime.of(3, 30), zdt2.toLocalTime());
        assertEquals(Duration.ofHours(23), Duration.between(firstRun.get(0).start(), firstRun.get(1).start()));
    }

    @Test
    void reportFormattingAndLegacyAdapterRoundTrip() {
        MaintenanceWindow window = new MaintenanceWindow(
                Instant.parse("2024-03-31T01:00:00Z"),
                Instant.parse("2024-03-31T02:00:00Z"));

        String formatted = ReportFormatter.formatWindow(window, BERLIN, Locale.GERMANY);
        assertEquals("31.03.2024 03:00 +02:00 (Europe/Berlin) – 31.03.2024 04:00 +02:00 (Europe/Berlin)", formatted);

        Instant original = Instant.parse("2024-03-31T01:00:00Z");
        Date date = LegacyDateAdapter.toDate(original);
        assertEquals(original, LegacyDateAdapter.toInstant(date));

        ZonedDateTime zdt = original.atZone(BERLIN);
        GregorianCalendar cal = LegacyDateAdapter.toGregorianCalendar(zdt);
        ZonedDateTime restored = LegacyDateAdapter.toZonedDateTime(cal);
        assertEquals(zdt.toInstant(), restored.toInstant());
        assertEquals(zdt.getZone(), restored.getZone());
    }
}
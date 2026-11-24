package hometask10;

import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

public final class IncidentAnalytics {

    private static final BigInteger NANOS_PER_SECOND = BigInteger.valueOf(1_000_000_000L);

    private IncidentAnalytics() {
    }

    public static Duration totalDowntime(List<Incident> incidents) {
        validateList(incidents);
        Duration total = Duration.ZERO;
        for (Incident incident : incidents) {
            total = total.plus(incident.duration());
        }
        return total;
    }

    public static Duration averageDowntime(List<Incident> incidents) {
        validateList(incidents);
        if (incidents.isEmpty()) {
            return Duration.ZERO;
        }

        BigInteger totalNanos = BigInteger.ZERO;
        for (Incident incident : incidents) {
            Duration d = incident.duration();
            BigInteger nanos = BigInteger.valueOf(d.getSeconds())
                    .multiply(NANOS_PER_SECOND)
                    .add(BigInteger.valueOf(d.getNano()));
            totalNanos = totalNanos.add(nanos);
        }

        BigInteger avgNanos = totalNanos.divide(BigInteger.valueOf(incidents.size()));
        BigInteger[] secAndNano = avgNanos.divideAndRemainder(NANOS_PER_SECOND);
        return Duration.ofSeconds(secAndNano[0].longValueExact(), secAndNano[1].longValueExact());
    }

    public static Optional<Incident> longestIncident(List<Incident> incidents) {
        validateList(incidents);
        if (incidents.isEmpty()) {
            return Optional.empty();
        }

        Comparator<Incident> comparator = Comparator
                .comparing(Incident::duration)
                .thenComparing(Incident::startedAt, Comparator.reverseOrder())
                .thenComparing(Incident::id, Comparator.reverseOrder());

        Incident longest = incidents.get(0);
        for (int i = 1; i < incidents.size(); i++) {
            Incident current = incidents.get(i);
            if (comparator.compare(current, longest) > 0) {
                longest = current;
            }
        }
        return Optional.of(longest);
    }

    public static Map<LocalDate, List<Incident>> groupByStartDate(List<Incident> incidents, ZoneId zone) {
        validateList(incidents);
        Objects.requireNonNull(zone, "zone must not be null");

        Map<LocalDate, List<Incident>> grouped = new TreeMap<>();
        for (Incident incident : incidents) {
            LocalDate date = incident.startedAt().atZone(zone).toLocalDate();
            grouped.computeIfAbsent(date, d -> new ArrayList<>()).add(incident);
        }

        Comparator<Incident> groupOrder = Comparator
                .comparing(Incident::startedAt)
                .thenComparing(Incident::id);

        for (List<Incident> list : grouped.values()) {
            list.sort(groupOrder);
        }

        return grouped;
    }

    private static void validateList(List<Incident> incidents) {
        Objects.requireNonNull(incidents, "incidents must not be null");
        for (Incident incident : incidents) {
            Objects.requireNonNull(incident, "incident element must not be null");
        }
    }
}
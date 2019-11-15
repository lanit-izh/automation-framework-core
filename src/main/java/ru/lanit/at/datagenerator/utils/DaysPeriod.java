package ru.lanit.at.datagenerator.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;


/**
 * Период дней от {@link DaysPeriod#start} до {@link DaysPeriod#end}
 */
public class DaysPeriod {
    private final LocalDate start;
    private final LocalDate end;
    private final DateTimeFormatter DEFAULT_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public DaysPeriod(LocalDate start, LocalDate end) {
        if (start.isBefore(end)) {
            this.start = start;
            this.end = end;
        } else {
            this.start = end;
            this.end = start;
        }
    }

    public DaysPeriod(long startDays, long endDays) {
        this(LocalDate.now().plusDays(startDays), LocalDate.now().plusDays(endDays));
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return String.format("с %s по %s", DEFAULT_FORMAT.format(start), DEFAULT_FORMAT.format(end));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DaysPeriod period = (DaysPeriod) o;
        return Objects.equals(start, period.start) &&
                Objects.equals(end, period.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }

    public boolean contains(DaysPeriod period) {
        return (this.start.isBefore(period.getStart()) || this.start.isEqual(period.getStart())) &&
                (this.end.isAfter(period.getEnd()) || this.end.isEqual(period.getEnd()));
    }
}

package ru.lanit.at.datagenerator.helpers;


import ru.lanit.at.datagenerator.utils.DaysPeriod;
import ru.lanit.at.exceptions.FrameworkRuntimeException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateHelper {
    public String defaultFormat;


    public DateHelper(String defaultFormat) {
        this.defaultFormat = defaultFormat;
    }

    public DaysPeriod getPeriodFromNowToDay(int endDays) {
        return new DaysPeriod(0, Math.abs(endDays));
    }

    public DaysPeriod getPeriodPastToNowDay(int startDays) {
        return new DaysPeriod(LocalDate.now().minusDays(Math.abs(startDays)), LocalDate.now());
    }

    public DaysPeriod stringToDaysPeriod(String dateStart, String dateEnd) {
        return stringToDaysPeriod(dateStart, dateEnd, defaultFormat);
    }

    public DaysPeriod stringToDaysPeriod(String dateStart, String dateEnd, String format) {
        LocalDate start = stringToDate(dateStart, format);
        LocalDate end = stringToDate(dateEnd, format);
        return new DaysPeriod(start, end);
    }


    public LocalDate stringToDate(String dateString) {
        return stringToDate(dateString, defaultFormat);
    }

    public LocalDate stringToDate(String dateString, String format) {
        try {
            LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern(format));
            return date;
        } catch (DateTimeParseException e) {
            throw new FrameworkRuntimeException("Преобразуемая в дату строка не соответствует формату \"" + format + "\"");
        }
    }


    public String getStringDate(LocalDate date) {
        return getStringDate(date, DateTimeFormatter.ofPattern(defaultFormat));
    }

    public String getStringDate(LocalDate localDate, DateTimeFormatter dateTimeFormatter) {
        return dateTimeFormatter.format(localDate);
    }
}

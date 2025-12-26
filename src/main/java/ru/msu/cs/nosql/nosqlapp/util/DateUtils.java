package ru.msu.cs.nosql.nosqlapp.util;

import java.util.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.ThreadLocalRandom;

public class DateUtils {

    public static Date randomDate() {
        // Начальная дата: 1 января 2020
        LocalDate startDate = LocalDate.of(2020, 1, 1);
        long startMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endMillis = System.currentTimeMillis();

        long randomMillis = ThreadLocalRandom.current().nextLong(startMillis, endMillis);
        return Date.from(Instant.ofEpochMilli(randomMillis));
    }
}

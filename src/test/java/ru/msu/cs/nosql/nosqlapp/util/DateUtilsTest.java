package ru.msu.cs.nosql.nosqlapp.util;

import org.junit.jupiter.api.Test;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilsTest {

    @Test
    void testRandomDate() {
        Date date = DateUtils.randomDate();
        assertNotNull(date);
        assertTrue(date.getTime() > 0);
    }
}

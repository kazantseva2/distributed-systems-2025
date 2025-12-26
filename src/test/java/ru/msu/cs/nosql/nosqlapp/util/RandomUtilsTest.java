package ru.msu.cs.nosql.nosqlapp.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RandomUtilsTest {

    @Test
    void testRandomRating() {
        for (int i = 0; i < 100; i++) {
            int r = RandomUtils.randomRating();
            assertTrue(r >= 1 && r <= 5);
        }
    }

    @Test
    void testRandomText() {
        String text = RandomUtils.randomText(5);
        assertNotNull(text);
        assertTrue(text.split(" ").length == 5);
    }
}

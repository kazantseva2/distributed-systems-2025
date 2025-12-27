package ru.msu.cs.nosql.nosqlapp.util;

import java.util.Random;

public class RandomUtils {
    private static final Random random = new Random();

    private static final String[] POSITIVE = {
            "Great product, works perfectly",
            "Very satisfied with the purchase",
            "Excellent quality for the price",
            "Battery lasts long and charges fast",
            "Highly recommend this product"
    };

    private static final String[] NEGATIVE = {
            "Battery drains very quickly",
            "Poor build quality",
            "Stopped working after a week",
            "Not worth the money",
            "Very disappointed with this product"
    };

    private static final String[] NEUTRAL = {
            "Average product",
            "Works as expected",
            "Nothing special",
            "Decent for the price",
            "Acceptable quality"
    };

    public static int randomRating() {
        return random.nextInt(5) + 1; // 1-5
    }

    public static String randomText(int rating) {
        if (rating >= 4) {
            return POSITIVE[random.nextInt(POSITIVE.length)];
        } else if (rating <= 2) {
            return NEGATIVE[random.nextInt(NEGATIVE.length)];
        } else {
            return NEUTRAL[random.nextInt(NEUTRAL.length)];
        }
    }
}

package ru.msu.cs.nosql.nosqlapp.util;

import java.util.Random;

public class RandomUtils {
    private static final Random random = new Random();
    private static final String[] words = {"good", "bad", "excellent", "cheap", "durable", "battery", "fast"};

    public static int randomRating() {
        return random.nextInt(5) + 1; // 1-5
    }

    public static String randomText(int wordCount) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < wordCount; i++) {
            sb.append(words[random.nextInt(words.length)]).append(" ");
        }
        return sb.toString().trim();
    }

    public static <T> T randomElement(T[] array) {
        return array[random.nextInt(array.length)];
    }
}

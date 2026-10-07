package com.framework.utils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Thread-safe random data primitives. Used instead of timestamps so that scenarios running in
 * parallel within the same second never generate colliding identifiers.
 */
public final class RandomDataUtils {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = LOWER.toUpperCase(Locale.ROOT);
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^*";
    private static final int MIN_PASSWORD_LENGTH = 8;

    private RandomDataUtils() {
    }

    public static String numeric(int length) {
        return randomFrom(DIGITS, length);
    }

    public static String lowercaseAlphanumeric(int length) {
        return randomFrom(LOWER + DIGITS, length);
    }

    public static String letters(int length) {
        return randomFrom(LOWER, length);
    }

    /**
     * Generates a password containing upper, lower, digit and special characters.
     */
    public static String strongPassword(int length) {
        if (length < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password length must be at least " + MIN_PASSWORD_LENGTH);
        }
        List<Character> characters = new ArrayList<>();
        characters.add(randomChar(UPPER));
        characters.add(randomChar(LOWER));
        characters.add(randomChar(DIGITS));
        characters.add(randomChar(SPECIAL));
        String all = UPPER + LOWER + DIGITS + SPECIAL;
        while (characters.size() < length) {
            characters.add(randomChar(all));
        }
        Collections.shuffle(characters, RANDOM);
        StringBuilder builder = new StringBuilder(length);
        characters.forEach(builder::append);
        return builder.toString();
    }

    private static String randomFrom(String alphabet, int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(randomChar(alphabet));
        }
        return builder.toString();
    }

    private static char randomChar(String alphabet) {
        return alphabet.charAt(RANDOM.nextInt(alphabet.length()));
    }
}

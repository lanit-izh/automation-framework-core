package ru.lanit.at.datagenerator.utils;


import org.apache.commons.lang3.RandomUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InnGenerator {
    /* Generate Inn*/
    private static List<String> regionCodes = new ArrayList<>();
    private static List<String> taxCodes = new ArrayList<>();

    public static String generateInn() {
        String result = "";
        addRegion();
        addTax();

        result += regionCodes.get(RandomUtils.nextInt(0, regionCodes.size()));
        result += taxCodes.get(RandomUtils.nextInt(0, taxCodes.size()));


        result += randomNumber(7);
        int[] massInt = parseStringInArrayInt(result);

        int number = 7 * massInt[0] + 2 * massInt[1] + 4 * massInt[2] + 10 * massInt[3] + 3 * massInt[4];
        number += 5 * massInt[5] + 9 * massInt[6] + 4 * massInt[7] + 6 * massInt[8];
        if (massInt.length == 10) {
            number += 8 * massInt[9];
        }
        result += (number % 11) % 10;
        massInt = parseStringInArrayInt(result);
        number = 3 * massInt[0] + 7 * massInt[1] + 2 * massInt[2] + 4 * massInt[3] + 10 * massInt[4] + 3 * massInt[5];
        number += 5 * massInt[6] + 9 * massInt[7] + 4 * massInt[8] + 6 * massInt[9];
        if (massInt.length == 11) {
            number += 8 * massInt[10];
        }
        result += (number % 11) % 10;
        if (result.length() >= 13) {
            result = result.substring(0, 12);
        }

        return result;
    }

    private static int[] parseStringInArrayInt(String value) {
        int[] massInt = new int[value.length()];
        for (int i = 0; i < value.length(); i++) {
            massInt[i] = Integer.parseInt(String.valueOf(value.toCharArray()[i]));
        }
        return massInt;
    }

    private static void addRegion() {
        for (int i = 1; i < 100; i++) {
            regionCodes.add(String.valueOf(i));
        }
        String[] n = {"102", "113", "116", "121", "125", "138", "150", "190", "152", "154", "159", "161", "163", "164", "173", "174", "177", "199", "197", "198"};
        regionCodes.addAll(Arrays.asList(n));
    }

    private static void addTax() {
        for (int i = 1; i < 78; i++) {
            taxCodes.add(String.valueOf(i));
        }
        String[] n = {"82", "83", "86", "87", "89", "92", "95"};
        taxCodes.addAll(Arrays.asList(n));
    }

    private static String randomNumber(int count) {
        String result = "";
        for (int i = 0; i < count; i++) {
            result += RandomUtils.nextInt(0, 9);
        }
        return result;
    }


}

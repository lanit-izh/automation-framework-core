package ru.lanit.at.datagenerator.helpers;


import org.apache.commons.lang3.RandomStringUtils;

public class StringHelper {
    private final String CYR = "АБВГДЕЖЗИКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ";
    private final String LAT = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final String SPECIAL_SYM = "^_.-'#№@»`«&,().!@#;:'\"<>/^*()_";
    private final String ROMANIAN_DIGITS = "IXVCMD";
    private final String DIG_N = "123456789";


    public String getRandomCyrillicString(int count) {
        return getRandomString(count, CYR);
    }

    public String getRandomLatinString(int count) {
        return getRandomString(count, LAT);
    }

    public String getRandomRomanianDigits(int count) {
        return getRandomString(count, ROMANIAN_DIGITS);
    }

    public String getRandomSpecialSymbol(int count) {
        return getRandomString(count, SPECIAL_SYM);
    }

    public String getRandomNumericString(int count) {
        return RandomStringUtils.randomNumeric(count);
    }

    public String getRandomNumericNotZero(int count) {
        return getRandomString(count, DIG_N);
    }

    public String getRandomNumeric(int minLengthInclusive, int maxLengthExclusive) {
        return RandomStringUtils.randomNumeric(minLengthInclusive, maxLengthExclusive);
    }

    public String getRandomString(int count, String alphabet) {
        return RandomStringUtils.random(count, alphabet.toCharArray());
    }


}

package ru.lanit.at.datagenerator.helpers;


import net.datafaker.Faker;
import org.apache.commons.lang3.RandomUtils;
import ru.lanit.at.datagenerator.utils.InnGenerator;

import java.text.SimpleDateFormat;

public class PersonalData {
    private Faker faker;

    public PersonalData(Faker faker) {
        this.faker = faker;
    }

    public String getFullName() {
        return faker.name().nameWithMiddle();
    }

    public String getFirstName() {
        return faker.name().firstName();
    }

    public String getLastName() {
        return faker.name().lastName();
    }

    public String getMiddleName() {
        return faker.name().firstName() + "вич";
    }

    public String getBirthday(int minAge, int maxAge) {
        return new SimpleDateFormat("dd.MM.yyyy").format(faker.date().birthday(minAge, maxAge));
    }

    public String getSnils() {
        return buildSnils();
    }

    public String getEmail() {
        return faker.internet().emailAddress(faker.bothify("?????"));
    }

    public String getInn() {
        return InnGenerator.generateInn();
    }


    private static String buildSnils() {
        String snils = String.valueOf(RandomUtils.nextInt(100_000_000, 900_000_000));
        int length = snils.length();

        int sum = 0;
        for (int i = 0; i < length; i++) {
            char currentChar = snils.charAt(i);
            int c = Character.getNumericValue(currentChar);

            if (i < length - 2) {
                if (currentChar == snils.charAt(i + 1) && currentChar == snils.charAt(i + 2)) {
                    return buildSnils();
                }
            }
            sum += c * (length - i);
        }
        String controlNumber;
        sum %= 101;
        if (sum == 100) {
            controlNumber = "00";
        } else {
            controlNumber = sum < 10 ? "0" + sum : "" + sum;
        }

        return snils.substring(0, 3) + "-" + snils.substring(3, 6) + "-" + snils.substring(6, 9) + " " + controlNumber;
    }

}

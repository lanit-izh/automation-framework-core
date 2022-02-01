package ru.lanit.at.datagenerator;


import net.datafaker.Faker;
import ru.lanit.at.datagenerator.helpers.AddressHelper;
import ru.lanit.at.datagenerator.helpers.DateHelper;
import ru.lanit.at.datagenerator.helpers.FileHelper;
import ru.lanit.at.datagenerator.helpers.NumberHelper;
import ru.lanit.at.datagenerator.helpers.PersonalData;
import ru.lanit.at.datagenerator.helpers.StringHelper;

import java.util.Locale;

public class DataGenerator {

    public static StringHelper stringHelper = new StringHelper();
    public static NumberHelper numberHelper = new NumberHelper();
    public static FileHelper fileHelper = new FileHelper();
    public static PersonalData personalData = new PersonalData(new Faker(new Locale("ru")));
    public static AddressHelper addressHelper = new AddressHelper(new Faker(new Locale("ru")));
    public static DateHelper dateHelper = new DateHelper("dd.MM.yyyy");

}

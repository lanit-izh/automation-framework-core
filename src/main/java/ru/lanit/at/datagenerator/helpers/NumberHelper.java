package ru.lanit.at.datagenerator.helpers;


import net.datafaker.Faker;
import net.datafaker.Number;

import java.util.Locale;

public class NumberHelper {
    public Number getNumber() {
        return new Faker(new Locale("ru-RU")).number();
    }
}

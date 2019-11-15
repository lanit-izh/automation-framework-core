package ru.lanit.at.datagenerator.helpers;


import com.github.javafaker.Faker;
import com.github.javafaker.Number;

import java.util.Locale;

public class NumberHelper {
    public Number getNumber() {
        return new Faker(new Locale("ru-RU")).number();
    }
}

package ru.lanit.at.datagenerator.helpers;


import net.datafaker.Faker;


public class AddressHelper {

    private Faker faker;

    public AddressHelper(Faker faker) {
        this.faker = faker;
    }

    public String getState() {
        return faker.address().state();
    }

    public String getZipCode() {
        return faker.address().zipCode();
    }

    public String getCity() {
        return faker.address().city();
    }

    public String getBuildingNumber() {
        return faker.address().buildingNumber();
    }

    public String getStreetAddress() {
        return faker.address().streetAddress();
    }

    public String getFullAddress() {
        return faker.address().fullAddress().replace("###", "").trim();
    }

    public String getFlatNumber() {
        return faker.address().secondaryAddress().replace("0", "1");
    }
}

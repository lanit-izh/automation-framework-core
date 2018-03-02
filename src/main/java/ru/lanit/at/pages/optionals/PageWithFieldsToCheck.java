package ru.lanit.at.pages.optionals;

import java.util.List;

public interface PageWithFieldsToCheck extends OptionalPageInterface {
    boolean checkFieldsPresentAndFilled(List<String> fieldsToCheck);
}

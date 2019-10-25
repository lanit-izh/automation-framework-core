package ru.lanit.at.steps;


import cucumber.api.java.ru.И;
import cucumber.api.java.ru.Пусть;
import cucumber.api.java.ru.То;
import cucumber.api.java.ru.Тогда;
import org.testng.Assert;
import ru.lanit.at.pages.AbstractPage;
import ru.lanit.at.pages.elements.UIElement;
import ru.yandex.qatools.matchers.webdriver.EnabledMatcher;

import java.util.Arrays;

import static ru.yandex.qatools.matchers.webdriver.DisplayedMatcher.displayed;

public final class UIStepsLibrary extends AbstractFrameworkSteps {


    @Пусть("^открываем приложение по адресу '(.*)'$")
    public void createDriver(String url) {
        getDriver().get(url);
    }


    @И("^перейти к странице '(.*)'$")
    public void changePageTo(String pageName) {
        getPageByTitle(pageName);
    }

    @И("^вернуться к странице'$")
    public void returnToPage() {
        resetCurrentBlock();
    }


    @И("^на текущей странице перейти к блоку '(.*)'$")
    public void focusOnBlock(String blockNameStr) {
        String[] blocks = blockNameStr.split(">");
        AbstractPage page = getCurrentPage();
        setCurrentBlockByName(blocks[0]);
        if (blocks.length > 1) {
            focusOnBlockInBlock(blockNameStr.substring(blockNameStr.indexOf(">") + 1));
        }
    }

    @И("^в текущем блоке перейти к блоку '(.*)'$")
    public void focusOnBlockInBlock(String blockNameStr) {
        Arrays.stream(blockNameStr.split(">"))
                .forEach(blockName -> setCurrentBlockByName(blockName.trim()));
    }


    private UIElement getElement(String param) {
        String[] data = param.split(":");
        String elem = data[0].trim();
        String[] params = data[1].trim().split(",");
        return getUIElementByName(elem, params);
    }


    @И("^нажать на '(.*)'$")
    public void clickButton(String param) {
        getElement(param).click();
    }

    @И("^ввести в поле '(.*)' значение '(.*)'$")
    public void typeIntoInput(String elem, String text) {
        getElement(elem).sendKeys(text);
    }

    @Тогда("^проверить, что в поле '(.*)' значение = '(.*)'$")
    public void checkInputValueEquals(String inputName, String expectedValue) {
        String actualValue = getElement(inputName).getText();
        Assert.assertEquals("Текст элемента '" + inputName + "' = '" + actualValue
                        + "'. Не совпадает с ожидаемым значением: '" + expectedValue + '\'',
                expectedValue, actualValue);
    }

    @И("^ввести в поле '(.*)' значение '(.*)' и проверить введенное значение.$")
    public void typeIntoInputAndCheckValue(String inputName, String expectedValue) {
        typeIntoInput(inputName, expectedValue);
        checkInputValueEquals(inputName, expectedValue);
    }

    @И("^очистить поле '(.*)'$")
    public void clearInput(String inputName) {
        getElement(inputName).clear();
    }

//    @И("^активировать чекбокс '(.*)'$")
//    public void selectCheckbox(String elementName) {
//        CheckBox checkBox = (CheckBox) getElement(elementName);
//        checkBox.setChecked(true);
//    }
//
//    /**
//     * Set the element is unchecked.
//     *
//     * @param elementName name of element
//     */
//    @И("^деактивировать чекбокс '(.*)'$")
//    public void deselectCheckbox(String elementName) {
//        CheckBox checkBox = (CheckBox) getElement(elementName);
//        checkBox.setChecked(false);
//    }
//
//    /**
//     * Check that  the element is checked=true.
//     *
//     * @param elementName name of element
//     */
//    @Тогда("^проверить, что чекбокс активирован '(.*)'$")
//    public void checkCheckboxSelected(String elementName) {
//        CheckBox checkBox = (CheckBox) getElement(elementName);
//        boolean isSelected = checkBox.isSelected();
//        Assert.assertTrue(isSelected, "Ожидалось что чекбокс '" + elementName + "' активирован.");
//    }
//
//    /**
//     * Check that the element is checked=false.
//     *
//     * @param elementName name of element
//     */
//    @Тогда("^чекбокс не активирован '(.*)'$")
//    public void checkCheckboxNotSelected(String elementName) {
//        CheckBox checkBox = (CheckBox) getElement(elementName);
//        boolean isSelected = checkBox.isSelected();
//        Assert.assertFalse(isSelected, "Ожидалось что чекбокс '" + elementName + "' не активирован.");
//    }
//
//    /**
//     * Select radio button
//     *
//     * @param elementName name of element
//     */
//    @И("^выбрать радиокнопку '(.*)'$")
//    public void selectRadioButton(String elementName) {
//        RadioButton radioButton = (RadioButton) getElement(elementName);
//        radioButton.select();
//    }
//
//    /**
//     * Check that the element is selected=true.
//     *
//     * @param elementName name of element
//     */
//    @То("^радиокнопка выбрана'(.*)'$")
//    public void checkRadioButtonSelected(String elementName) {
//        RadioButton radioButton = (RadioButton) getElement(elementName);
//        boolean isSelected = radioButton.isSelected();
//        Assert.assertTrue(isSelected, "Ожидалось что радиокнопка '" + elementName + "' выбрана.");
//    }
//
//    /**
//     * Check that the element is selected=false.
//     *
//     * @param elementName name of element
//     */
//    @То("^радиокнопка не выбрана'(.*)'$")
//    public void checkRadioButtonNotSelected(String elementName) {
//        RadioButton radioButton = (RadioButton) getElement(elementName);
//        boolean isSelected = radioButton.isSelected();
//        Assert.assertFalse(isSelected, "Ожидалось что радиокнопка '" + elementName + "' не выбрана.");
//    }

    /**
     * Check the text of the element
     *
     * @param elementName  name of element
     * @param expectedText expected text
     */
    @Тогда("^текст (?:элемента|ссылки) '(.*)' = '(.*)'$")
    public void assertElementHasText(String elementName, String expectedText) {
        String actualText = getElement(elementName).getText();
        Assert.assertEquals("Текст элемента '" + elementName + "' = '" + actualText
                        + "'. Не совпадает с ожидаемым значением: '" + expectedText + '\'',
                expectedText, actualText);
    }

//
//    /**
//     * Check the link address of the element
//     *
//     * @param linkName            name of element
//     * @param expectedLinkAddress expected link address
//     */
//    @Тогда("^проверить что адрес ссылки '(.*)' = '(.*)'$")
//    public void checkLinkAddress(String linkName, String expectedLinkAddress) {
//        String actualLinkAddress = ((Link) getElement(linkName)).getLinkAddress();
//        Assert.assertEquals("Адрес ссылки '" + linkName + "' = '" + actualLinkAddress
//                        + "'. Не совпадает с ожидаемым значением: '" + expectedLinkAddress + '\'',
//                expectedLinkAddress, actualLinkAddress);
//    }
//
//    @И("^в выпадающем списке '(.*)' выбрать значение '(.*)'$")
//    public void selectInDropdown(String elementName, String value) {
//        ((DropDown) getElement(elementName)).selectByValue(value);
//    }
//
//    /**
//     * Select options in dropdown
//     *
//     * @param elementName name of element
//     * @param values      options
//     */
//    @И("^в выпадающем списке '(.*)' выбрать:$")
//    public void selectMultipleValuesInDropdown(String elementName, List<String> values) {
//        ((DropDown) getElement(elementName)).selectMultipleItemsInDropdown(values.toArray(new String[0]));
//    }
//
//    /**
//     * Check selected option in dropdown
//     *
//     * @param elementName   name of element
//     * @param expectedValue expected option
//     */
//    @Тогда("^в выпадающем списке '(.*)' выбрано '(.*)'$")
//    public void checkThatValueInDropdownChosen(String elementName, String expectedValue) {
//        String actualValue = ((DropDown) getElement(elementName)).getSelectedInDropdownValue();
//        Assert.assertEquals("Выбранное в элементе '" + elementName
//                        + "' значение '" + actualValue + "'  не соответствует ожидаемому значению '" + expectedValue + "'",
//                expectedValue, actualValue);
//    }
    @То("^элемент '(.*)' отображен")
    public void checkIsDisplayed(String elementName) {
        Assert.assertTrue(getElement(elementName).isDisplayed(), "Элемент '" + elementName + "'  не отображён");
    }

    @То("^элемент '(.*)' не отображается")
    public void checkNotDisplayed(String elementName) {
        Assert.assertFalse(getElement(elementName).isDisplayed(), "Элемент '" + elementName + "'  не должен отображаться");
    }

    /**
     * Check the element is enabled
     *
     * @param elementName name of element
     */
    @То("^элемент '(.*)' не заблокирован$")
    public void checkIsEnabled(String elementName) {
        Assert.assertTrue(getElement(elementName).isEnabled(), "Элемент '" + elementName + "' заблокирован на странице");
    }

    /**
     * Check the element is  not enabled
     *
     * @param elementName name of element
     */
    @То("^элемент '(.*)' заблокирован$")
    public void isDisabled(String elementName) {
        Assert.assertFalse(getElement(elementName).isEnabled(), "Элемент '" + elementName + "' не заблокирован на странице");
    }

    @И("^подождать, когда элемент '(.*)' станет видимым$")
    public void waitUntilVisible(String elementName) {
        getElement(elementName).waitUntil(displayed());
    }

    @И("^подождать, когда элемент '(.*)' станет доступен$")
    public void waitUntilEnabled(String elementName) {
        getElement(elementName).waitUntil(EnabledMatcher.enabled());
    }

    @И("^элемент '(.*)' присутствует на странице$")
    public void checkElementWithText(String elementName) {
        Assert.assertTrue(getElement(elementName).isDisplayed(), "Элемент с текстом '" + elementName + "'отсутствует");
    }


}


//    @И("^подождать, когда элемент '(.*)' станет видимым$")
//    public void waitUntilVisible(String elementName) {
//        commonSteps.waitUntilVisible(commonSteps.getElementByName(elementName));
//    }
//
//    /**
//     * Wait for the element to be not displayed
//     *
//     * @param elementName name of element
//     */
//    @И("^подождать, когда элемент '(.*)' исчезнет$")
//    public void waitUntilNotVisible(String elementName) {
//        commonSteps.waitUntilNotVisible(commonSteps.getElementByName(elementName));
//    }
//
//    /**
//     * Wait for the element to be enabled
//     *
//     * @param elementName name of element
//     */
//    @И("^подождать, когда элемент '(.*)' станет доступен$")
//    public void waitUntilEnabled(String elementName) {
//        commonSteps.waitUntilEnabled(commonSteps.getElementByName(elementName));
//    }
//
//    /**
//     * Wait for the element to be not enabled
//     *
//     * @param elementName name of element
//     */
//    @И("^подождать, когда элемент '(.*)' станет недоступен$")
//    public void waitUntilDisabled(String elementName) {
//        commonSteps.waitUntilDisabled(commonSteps.getElementByName(elementName));
//    }
//
//    /**
//     * Find element on page by text and execute some action.
//     * Available actions : "нажать","проверить на видимость","проверить на отсутствие",
//     * "проверить на доступность","проверить на недоступность"
//     *
//     * @param elementText text the element
//     * @param action      action
//     */
//    @И("^найти (?:элемент|кнопку) с текстом '(.*)'" +
//            " и выполнить действие " +
//            "(нажать|проверить на видимость|проверить на отсутствие|проверить на доступность|проверить на недоступность)$")
//    public void findAndExecute(String elementText, String action) {
//        String elemName = "Элемент с текстом: " + elementText;
//        WebElement element = commonSteps.getElementByText(elementText);
//        switch (action) {
//            case "нажать": {
//                clickButton(element);
//                break;
//            }
//            case "проверить на видимость": {
//                isDisplayed(element, elemName);
//                break;
//            }
//            case "проверить на отсутствие": {
//                notDisplayed(element, elemName);
//                break;
//            }
//            case "проверить на доступность": {
//                isEnabled(element, elemName);
//                break;
//            }
//            case "проверить на недоступность": {
//                isDisabled(element, elemName);
//                break;
//            }
//        }
//    }
//
//    /**
//     * Close current driver
//     */
//    @Тогда("^закрыть текущий драйвер$")
//    public void closeCurrentDriver() {
//        commonSteps.closeCurrentDriver();
//    }
//
//    /**
//     * Close current drivers
//     */
//    @Тогда("^закрыть текущие драйвера$")
//    public void closeCurrentDrivers() {
//        commonSteps.closeCurrentDrivers();
//    }
//
//    /**
//     * Switch driver focus to window/tab and change current page in PageCatalog
//     *
//     * @param pageTitle - title window/tab and page name
//     */
//    /*Tabs,Windows*/
//    @И("^переключиться на новое окно > c переходом на страницу '(.*)'$")
//    public void switchToWindow(String pageTitle) {
//        commonSteps.switchToWindow(pageTitle);
//        changePageTo(pageTitle);
//    }
//
//    /**
//     * Switch driver focus to window/tab and change current page in PageCatalog
//     *
//     * @param pageTitle - title window/tab
//     * @param pageName  -  page by pageName{@link ru.homecredit.at.pages.annotations.PageTitle}.
//     */
//    @И("^переключиться на новое окно '(.*)' > c переходом на страницу '(.*)'$")
//    public void switchToWindow(String pageTitle, String pageName) {
//        commonSteps.switchToWindow(pageTitle);
//        changePageTo(pageName);
//    }
//
//    /**
//     * Close current window/tab
//     */
//    @И("^закрыть текущее окно$")
//    public void closeCurrentWindow() {
//        commonSteps.closeCurrentWindow();
//    }
//
//
//    /**
//     * Click on element.
//     *
//     * @param button WebElement
//     */
//    private void clickButton(WebElement button) {
//        commonSteps.clickOn(button);
//    }
//
//    /**
//     * Check the element is displayed
//     *
//     * @param element  WebElement
//     * @param elemName name of element
//     */
//    private void isDisplayed(WebElement element, String elemName) {
//        Assert.assertTrue("Элемент '" + elemName + "'  не отображён на странице '"
//                        + commonSteps.getCurrentBlock().getClass().getSimpleName() + "' ",
//                commonSteps.isDisplayed(element));
//    }
//
//    /**
//     * Check the element is not displayed
//     *
//     * @param element  WebElement
//     * @param elemName name of element
//     */
//    private void notDisplayed(WebElement element, String elemName) {
//        Assert.assertFalse("Элемент: '" + elemName + "' не должен быть отображён на странице '"
//                        + commonSteps.getCurrentBlock().getClass().getSimpleName() + "' ",
//                commonSteps.isDisplayed(element));
//    }
//
//    /**
//     * Check the element is enabled
//     *
//     * @param element  WebElement
//     * @param elemName name of element
//     */
//    private void isEnabled(WebElement element, String elemName) {
//        Assert.assertTrue("Элемент: '" + elemName + "' заблокирован на странице '"
//                        + commonSteps.getCurrentBlock().getClass().getSimpleName() + "' ",
//                commonSteps.isEnabled(element));
//    }
//
//    /**
//     * Check the element is  not enabled
//     *
//     * @param element  WebElement
//     * @param elemName name of element
//     */
//    private void isDisabled(WebElement element, String elemName) {
//
//        Assert.assertFalse("Элемент: '" + elemName + "'  не заблокирован на странице '"
//                        + commonSteps.getCurrentBlock().getClass().getSimpleName() + "' ",
//                commonSteps.isEnabled(element));
//    }
//
//}

package ru.lanit.at.pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ru.lanit.at.context.Context;
import ru.lanit.at.make.Make;
import ru.lanit.at.make.Wait;
import ru.lanit.at.pages.optionals.Openable;
import ru.yandex.qatools.htmlelements.loader.decorator.HtmlElementDecorator;
import ru.yandex.qatools.htmlelements.loader.decorator.HtmlElementLocatorFactory;

import java.util.List;

public abstract class AbstractPage implements Openable {
    protected final int DEFAULT_TIMEOUT = 10; //The timeout in seconds
    protected Logger log = LogManager.getLogger(getClass());
    protected Wait wait;
    protected Make make;
    private WebDriver driver;
    private PageCatalog pageCatalog;

    public AbstractPage(WebDriver driver) {
        this.driver = driver;
        log.info("Инициализируем элементы {}", this.getClass().getSimpleName());
        PageFactory.initElements(new HtmlElementDecorator(new HtmlElementLocatorFactory(driver)), this);

        pageCatalog = (PageCatalog) Context.getInstance().getBean("pageCatalog");
        wait = (Wait) Context.getInstance().getBean("wait");
        make = (Make) Context.getInstance().getBean("make");
    }

    public WebDriver getDriver() {
        return driver;
    }

    protected <T extends AbstractPage> T getPage(Class<T> clazz) {
        return pageCatalog.getPage(clazz);
    }

    protected void waitForElementVisible(WebElement htmlElement, int timeout) {
        new WebDriverWait(driver, timeout).until(ExpectedConditions.visibilityOf(htmlElement));
    }

    protected void waitForElementVisible(WebElement htmlElement) {
        waitForElementVisible(htmlElement, DEFAULT_TIMEOUT);
    }

    protected void waitForElementInvisible(WebElement htmlElement, int timeout) {
        new WebDriverWait(driver, timeout).until(ExpectedConditions.invisibilityOf(htmlElement));
    }

    protected void waitForElementInvisible(WebElement htmlElement) {
        new WebDriverWait(driver, DEFAULT_TIMEOUT).until(ExpectedConditions.invisibilityOf(htmlElement));
    }

    protected void waitForElementClickable(int timeout, WebElement... htmlElements) {
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        for (WebElement webElement : htmlElements) {
            wait.until(ExpectedConditions.elementToBeClickable(webElement));
        }
    }

    protected void waitForElementClickable(WebElement... htmlElements) {
        waitForElementClickable(DEFAULT_TIMEOUT, htmlElements);
    }

    protected void waitForJSandJQueryToLoad() {
        WebDriverWait wait = new WebDriverWait(driver, DEFAULT_TIMEOUT);
        ExpectedCondition<Boolean> jQueryLoad = driver -> {
            try {
                return ((Long) ((JavascriptExecutor) driver).executeScript("return jQuery.active") == 0);
            } catch (Exception e) {
                return true;
            }
        };

        ExpectedCondition<Boolean> jsLoad = driver -> ((JavascriptExecutor) driver).executeScript("return document.readyState")
                .toString().equals("complete");

        wait.until(jQueryLoad);
        wait.until(jsLoad);
    }

    public int getParentStepNum(WebElement element) {
        WebElement parentStep;
        try {
            parentStep = element.findElement(By.xpath("./ancestor::fieldset[contains(@class, 'form-step')]"));
            String stepId = parentStep.getAttribute("id");
            String stepNum = stepId.substring(stepId.length() - 1);
            return Integer.parseInt(stepNum);
        } catch (WebDriverException wde) {
            log.warn("Элемент " + element + " не имеет родительского шага");
            return 0;
        }
    }

    public void clickAndWait(WebElement element) {
        waitForElementClickable(element);
        element.click();
        waitForJSandJQueryToLoad();
    }

    /**
     * Default timeout in seconds
     *
     * @return Default timeout in seconds
     */
    protected int getDefaultTimeout() {
        return DEFAULT_TIMEOUT;
    }

    /**
     * Method that tries to find element of page with given text. Searching occurs by xpath = {@code "//*[text()='" + text + "']"}
     * @param text key text that should be found on the page.
     * @return WebElement that contains given text.
     */
    protected WebElement getElementByText(String text) {
        String xPath = "//*[text()='" + text + "']";
        List<WebElement> foundElements = getDriver().findElements(By.xpath(xPath));
        if (foundElements.size() == 1) return foundElements.get(0);
        if (foundElements.size() > 1) {
            log.warn(foundElements.size() + " elements with text '" + text + "' were found on " + this.getClass().getSimpleName()
            + ". Returning first element of list.");
            return foundElements.get(0);
        }
        throw new NoSuchElementException("No elements with text '" + text + "' were found on " + this.getClass().getSimpleName());
    }

    @Deprecated
    protected <T extends AbstractPage> T initPage(Class<T> clazz) {
        return getPage(clazz);
    }
}

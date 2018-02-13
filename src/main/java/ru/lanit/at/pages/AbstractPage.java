package ru.lanit.at.pages;

import org.apache.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ru.lanit.at.context.Context;
import ru.yandex.qatools.htmlelements.element.HtmlElement;
import ru.yandex.qatools.htmlelements.loader.decorator.HtmlElementDecorator;
import ru.yandex.qatools.htmlelements.loader.decorator.HtmlElementLocatorFactory;

public abstract class AbstractPage implements Openable {
    public static ThreadLocal<AbstractPage> currentPage = new ThreadLocal<>();
    protected final int DEFAULT_TIMEOUT = 10;
    protected Logger log = Logger.getLogger(getClass());
    private WebDriver driver;
    private PageCatalog pageCatalog;

    public AbstractPage(WebDriver driver) {
        this.driver = driver;
        log.info("Инициализируем элементы " + this);
        PageFactory.initElements(new HtmlElementDecorator(new HtmlElementLocatorFactory(driver)), this);

        pageCatalog = (PageCatalog) Context.getInstance().getBean("pageCatalog");
    }

    public static void setCurrentPage(AbstractPage abstractPage) {
        if (currentPage.get() == null || currentPage.get() != abstractPage) {
            currentPage.set(abstractPage);
        }
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

    protected void waitForElementVisible(HtmlElement htmlElement) {
        waitForElementVisible(htmlElement, DEFAULT_TIMEOUT);
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
}

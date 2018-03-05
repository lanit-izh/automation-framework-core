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

import static ru.lanit.at.FrameworkConstants.DEFAULT_TIMEOUT;

public abstract class AbstractPage implements Openable {
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

    /**
     * @return Instance of {@link WebDriver} by which elements of current page object are initialized.
     */
    public WebDriver getDriver() {
        return driver;
    }

    /**
     * Returns instance of page with given class from {@link PageCatalog}. If {@link PageCatalog} doesn't contain page with such page yet - it will be initialized and saved.
     *
     * @param clazz Class of page object that should be initialized and returned.
     * @return Instance of page object from {@link PageCatalog}.
     */
    protected <T extends AbstractPage> T getPage(Class<T> clazz) {
        return pageCatalog.getPage(clazz);
    }

    /**
     * Waits for the specified time for {@link WebElement} to be visible, using {@link WebDriverWait} and {@link ExpectedConditions#visibilityOf(WebElement)}.
     *
     * @param htmlElement The element that should be visible.
     * @param timeout     Timeout in seconds.
     */
    protected void waitForElementVisible(WebElement htmlElement, int timeout) {
        new WebDriverWait(driver, timeout).until(ExpectedConditions.visibilityOf(htmlElement));
    }

    /**
     * Waits for default timeout for {@link WebElement} to be visible, using {@link WebDriverWait} and {@link ExpectedConditions#visibilityOf(WebElement)}.
     *
     * @param htmlElement The element that should be visible.
     */
    protected void waitForElementVisible(WebElement htmlElement) {
        waitForElementVisible(htmlElement, DEFAULT_TIMEOUT);
    }

    /**
     * Waits for the specified time for {@link WebElement} to be invisible, using {@link WebDriverWait} and {@link ExpectedConditions#invisibilityOf(WebElement)}.
     *
     * @param htmlElement The element that should be invisible.
     * @param timeout     Timeout in seconds.
     */
    protected void waitForElementInvisible(WebElement htmlElement, int timeout) {
        new WebDriverWait(driver, timeout).until(ExpectedConditions.invisibilityOf(htmlElement));
    }

    /**
     * Waits for default timeout for {@link WebElement} to be invisible, using {@link WebDriverWait} and {@link ExpectedConditions#visibilityOf(WebElement)}.
     *
     * @param htmlElement The element that should be invisible.
     */
    protected void waitForElementInvisible(WebElement htmlElement) {
        new WebDriverWait(driver, DEFAULT_TIMEOUT).until(ExpectedConditions.invisibilityOf(htmlElement));
    }

    /**
     * Waits for the specified time for list of {@link WebElement} to be clickable, using {@link WebDriverWait} and {@link ExpectedConditions#elementToBeClickable(WebElement)}.
     *
     * @param timeout      Timeout in seconds.
     * @param htmlElements List of elements that should be clickable.
     */
    protected void waitForElementClickable(int timeout, WebElement... htmlElements) {
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        for (WebElement webElement : htmlElements) {
            wait.until(ExpectedConditions.elementToBeClickable(webElement));
        }
    }

    /**
     * Waits for the default timeout for list of {@link WebElement} to be clickable, using {@link WebDriverWait} and {@link ExpectedConditions#elementToBeClickable(WebElement)}.
     *
     * @param htmlElements List of elements that should be clickable.
     */
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
     * @return Default timeout in seconds
     */
    protected int getDefaultTimeout() {
        return DEFAULT_TIMEOUT;
    }

    /**
     * Method that tries to find element of page with given text. Searching occurs by xpath = {@code "//*[text()='" + text + "']"}
     *
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

    /**
     * Method to get instance of page.
     *
     * @param clazz class of page that should be instantiated.
     * @return instance of clazz.
     * @deprecated Use getPage(...) method.
     */
    @Deprecated
    protected <T extends AbstractPage> T initPage(Class<T> clazz) {
        return getPage(clazz);
    }
}

package ru.lanit.at.pages;

import org.aspectj.lang.ProceedingJoinPoint;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.support.PageFactory;
import ru.lanit.at.driver.DriverManager;
import ru.lanit.at.exceptions.FrameworkRuntimeException;

public class PageRefreshAspect {

    public PageRefreshAspect() {
    }

    private DriverManager driverManager;

    public void setDriverManager(DriverManager driverManager) {
        this.driverManager = driverManager;
    }

    public Object handleInvoke(ProceedingJoinPoint point) {
        try {
            return point.proceed();
        } catch (StaleElementReferenceException sere) {
            PageFactory.initElements(driverManager.getDriver(), point.getTarget());
            try {
                return point.proceed();
            } catch (Throwable throwable) {
                throw new FrameworkRuntimeException(throwable);
            }
        } catch (Throwable throwable) {
            throw new FrameworkRuntimeException(throwable);
        }
    }
}

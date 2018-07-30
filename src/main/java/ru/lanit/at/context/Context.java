package ru.lanit.at.context;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Context {
    private static final ThreadLocal<ApplicationContext> instance = new ThreadLocal<>();

    /**
     * Method to get double check locked, thread safe Spring classpath application context.
     *
     * @return Instance of Spring {@link ApplicationContext}.
     */
    public static ApplicationContext getInstance() {
        ApplicationContext localInstance = instance.get();
        if (localInstance == null) {
            synchronized (ApplicationContext.class) {
                localInstance = instance.get();
                if (localInstance == null) {
                    localInstance = new ClassPathXmlApplicationContext("spring-config.xml");
                    instance.set(localInstance);
                }
            }
        }
        return localInstance;
    }
}

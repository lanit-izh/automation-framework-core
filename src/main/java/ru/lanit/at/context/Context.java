package ru.lanit.at.context;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Context {
    private static volatile ApplicationContext instance;

    /**
     * Method to get double check locked, thread safe Spring classpath application context.
     *
     * @return Instance of Spring {@link ApplicationContext}.
     */
    public static ApplicationContext getInstance() {
        ApplicationContext localInstance = instance;
        if (localInstance == null) {
            synchronized (ApplicationContext.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance =
                            new ClassPathXmlApplicationContext("spring-config.xml");
                }
            }
        }
        return localInstance;
    }
}

package ru.lanit.at.context;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Context {
    private static volatile ApplicationContext instance;

    public static ApplicationContext getInstance(){
        ApplicationContext localInstance = instance;
        if (localInstance == null) {
            synchronized (ApplicationContext.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new ClassPathXmlApplicationContext();
                }
            }
        }
        return localInstance;
    }
}

package ru.lanit.at.plugins.proxyelements;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;

public class ProxyClassLoader {
    private static HashMap<String, Class> loadedClasses = new HashMap<>();

    public static Class GetClass(String url) {
        if (loadedClasses.containsKey(url)) {
            return loadedClasses.get(url);
        }
        String path = url.substring(0, url.lastIndexOf('\\') + 1);
        String className = url.replace(path, "");
        File file = new File(path);
        try {
            URL toURL = file.toURI().toURL();
            URL[] urls = new URL[]{toURL};
            ClassLoader cl = new URLClassLoader(urls);
            Class hndlr = cl.loadClass(className);
            loadedClasses.put(url, hndlr);
            return hndlr;
        } catch (MalformedURLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}

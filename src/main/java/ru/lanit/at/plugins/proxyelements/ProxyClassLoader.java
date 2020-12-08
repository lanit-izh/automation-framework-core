package ru.lanit.at.plugins.proxyelements;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;

@ProxyElement("")
public class ProxyClassLoader {
    private static HashMap<String, Class> loadedClasses = new HashMap<>();
    private static String defPlugDir = "src/main/resources/pluginclasses/";

    public static void setDefPluginDir(String newDir){
        defPlugDir = newDir;
    }

    public static Class loadClassByUrl(String url) {
        String classNameFromUrl = url.substring(url.lastIndexOf("\\") + 1).replace(".class", "");
        if (loadedClasses.containsKey(classNameFromUrl)) {
            return loadedClasses.get(classNameFromUrl);
        }
        String path = url.substring(0, url.lastIndexOf('\\') + 1);
        File file = new File(path);
        try {
            URL toURL = file.toURI().toURL();
            URL[] urls = new URL[]{toURL};
            ClassLoader cl = new URLClassLoader(urls);
            Class hndlr = cl.loadClass(classNameFromUrl);
            loadedClasses.put(classNameFromUrl, hndlr);
            return hndlr;
        } catch (MalformedURLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Class getClass(String className) {
        if (loadedClasses.containsKey(className)) {
            return loadedClasses.get(className);
        }
        throw new IllegalStateException(String.format("No class with name %s was loaded before. Load classes frow default directory using loadClassesFromPluginDirectory() method, or do it mannualy with loadClassByUrl() method", className));
    }

    public static void loadClassesFromPluginDirectory() {
        File root = new File(defPlugDir);
        for (File f : root.listFiles(x -> x.getName().endsWith(".class"))) {
            loadClassByUrl(f.toURI().toString());
        }
    }
}

package com.raffaeleconforti.classloading;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * utility class to find any Interface implementing classes in netx/icedtea-web
 */
public class ClassFinder {

    public static final String JAVA_CLASS_PATH_PROPERTY = "java.class.path";
    public static final String CUSTOM_CLASS_PATH_PROPERTY = "custom.class.path";

    public static <T> List<Class<? extends T>> findAllMatchingTypes(Set<String> packages, Class<T> toFind) {
        List<Class<? extends T>> returnedClasses = new ArrayList<>();
        Set<Class> foundClasses = walkClassPath(packages, toFind);
        for (Class<?> clazz : foundClasses) {
            if (!clazz.isInterface()) {
                returnedClasses.add((Class<? extends T>) clazz);
            }
        }
        return returnedClasses;
    }

    private static Set<Class> walkClassPath(Set<String> packages, Class toFind) {
        Set<Class> results = new HashSet<>();
        Set<String> classPathRoots = getClassPathRoots();
        for (String classpathEntry : classPathRoots) {
            File f = new File(classpathEntry);
            if (!f.exists()) {
                continue;
            }
            if (f.isDirectory()) {
                traverse(f.getAbsolutePath(), f, toFind, packages, results);
            } else {
                File jar = new File(classpathEntry);
                try {
                    JarInputStream is = new JarInputStream(new FileInputStream(jar));
                    JarEntry entry;
                    while ((entry = is.getNextJarEntry()) != null) {
                        Class c = determine(entry.getName(), toFind, packages);
                        if (c != null) {
                            results.add(c);
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (UnsatisfiedLinkError ule) {
                    ule.printStackTrace();
                }
            }
        }
        return results;
    }

    static private Set<String> getClassPathRoots() {
        String classapth1 = System.getProperty(CUSTOM_CLASS_PATH_PROPERTY);
        String classapth2 = System.getProperty(JAVA_CLASS_PATH_PROPERTY);
        String classpath = "";
        if (classapth1 != null) {
            classpath = classpath + classapth1 + File.pathSeparator;
        }
        if (classapth2 != null) {
            classpath = classpath + classapth2 + File.pathSeparator;
        }
        String[] pathElements = classpath.split(File.pathSeparator);
        Set<String> s = new HashSet<>(Arrays.asList(pathElements));
        return s;
    }

    static private Class determine(String name, Class toFind, Set<String> packages) {
        if (name.contains("$")) {
            return null;
        }
        try {
            if (name.endsWith(".class")) {
                name = name.replace(".class", "");
                name = name.replace("/", ".");
                name = name.replace("\\", ".");
                boolean accepted = false;
                for(String packageName : packages) {
                    if(name.startsWith(packageName)) {
                        accepted = true;
                        break;
                    }
                }
                if(accepted) {
                    Class clazz = Class.forName(name);
                    if (toFind.isAssignableFrom(clazz)) {
                        return clazz;
                    }
                }
            }
        } catch (Throwable ex) {
//            ex.printStackTrace();
        }
        return null;
    }

    static private void traverse(String root, File current, Class toFind, Set<String> packages, Set<Class> result) {
        File[] fs = current.listFiles();
        for (File f : fs) {
            if (f.isDirectory()) {
                traverse(root, f, toFind, packages, result);
            } else {
                String ff = f.getAbsolutePath();
                String name = ff.substring(root.length());
                while (name.startsWith(File.separator)) {
                    name = name.substring(1);
                }
                Class c = determine(name, toFind, packages);
                if (c != null) {
                    result.add(c);
                }
            }

        }
    }

}
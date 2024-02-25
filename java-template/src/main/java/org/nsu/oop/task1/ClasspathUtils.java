package org.nsu.oop.task1;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ClasspathUtils {
    public static List<String> findAllClasses() {
        final String classPath = System.getProperty("java.class.path", ".");
        final String[] classPathElements = classPath.split(File.pathSeparator);
        return Arrays.stream(classPathElements).map(it -> findAllClasses(it)).flatMap(List::stream).collect(Collectors.toList());
    }

    private static List<String> findAllClasses(final String element) {
        final List<String> result = new ArrayList<String>();
        final File file = new File(element);
        if (file.isDirectory()) {
            result.addAll(findClassesInFolder(file, file));
        } else {
            result.addAll(loadClassesFromJarFile(file));
        }
        return result;
    }

    private static List<String> loadClassesFromJarFile(final File file) {
        final List<String> result = new ArrayList<String>();

        try (ZipFile zf = new ZipFile(file)) {
            final Enumeration e = zf.entries();
            while (e.hasMoreElements()) {
                final ZipEntry ze = (ZipEntry) e.nextElement();
                final String fileName = ze.getName();
                var pos = fileName.toLowerCase().indexOf(".class");
                if (pos > 0) {
                    result.add(fileName.replace('/', '.').substring(0, pos));
                }
            }
        } catch (final IOException e) {
            throw new Error(e);
        }
        return result;
    }

    private static List<String> findClassesInFolder(
            final File directory,
            final File baseDir) {
        final List<String> result = new ArrayList<String>();
        final File[] fileList = directory.listFiles();
        for (final File file : fileList) {
            if (file.isDirectory()) {
                result.addAll(findClassesInFolder(file, baseDir));
            } else {
                final String fileName = file.getAbsolutePath().substring(baseDir.getAbsolutePath().length()+1);
                var pos = fileName.toLowerCase().indexOf(".class");
                if (pos > 0) {
                    result.add(fileName.replace(File.separator, ".").substring(0, pos));
                }
            }
        }
        return result;
    }
}
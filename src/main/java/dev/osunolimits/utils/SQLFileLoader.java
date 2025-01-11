package dev.osunolimits.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

public class SQLFileLoader {

    private final String directory;

    public SQLFileLoader(String directory) {
        this.directory = directory.endsWith("/") ? directory : directory + "/";
    }

    /**
     * Loads the content of all .sql files from the specified directory.
     *
     * @return List of file contents
     * @throws IOException        If an I/O error occurs
     * @throws URISyntaxException If the resource URI is invalid
     */
    public List<String> loadSQLFiles() throws IOException, URISyntaxException {
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(directory);
        if (!resources.hasMoreElements()) {
            throw new IllegalStateException("Directory not found: " + directory);
        }

        while (resources.hasMoreElements()) {
            URL resourceURL = resources.nextElement();

            if (resourceURL.getProtocol().equals("jar")) {
                return getFilesFromJar(resourceURL).stream()
                        .map(this::readResource)
                        .collect(Collectors.toList());
            } else {
                return getFilesFromFileSystem(resourceURL).stream()
                        .map(this::readResource)
                        .collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }

    private List<String> getFilesFromJar(URL jarUrl) {
        try (InputStream jarStream = jarUrl.openStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(jarStream))) {
            return reader.lines()
                    .filter(line -> line.startsWith(directory) && line.endsWith(".sql"))
                    .map(line -> directory + line.substring(directory.length())) // Strip prefix
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load files from JAR: " + jarUrl, e);
        }
    }

    private List<String> getFilesFromFileSystem(URL directoryUrl) {
        try {
            return Files.list(Paths.get(directoryUrl.toURI()))
                    .filter(path -> path.toString().endsWith(".sql"))
                    .map(path -> directory + path.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("Failed to load files from file system: " + directoryUrl, e);
        }
    }

    private String readResource(String resourcePath) {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read resource: " + resourcePath, e);
        }
    }
}

package com.util.io;

import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;


public class FileUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    public static List<File> getFiles(File base, String pattern) {
        Path path = Paths.get(pattern);
        List<Path> filePaths;
        List<String> segments = new ArrayList<>(Arrays.asList(pattern.split(Pattern.quote(File.separator))));
        if (path.isAbsolute()) {
            Path absStart = Paths.get(segments.remove(0) + File.separator);
            assert Files.isDirectory(absStart);
            filePaths = Collections.singletonList(absStart);
        } else {
            assert base.isDirectory();
            filePaths = Collections.singletonList(base.getAbsoluteFile().toPath());
        }
        while (!segments.isEmpty()) {
            String segment = segments.remove(0);
            ArrayList<Path> pathArrayList = new ArrayList<>();

            for (Path folder : filePaths) {
                PathMatcher matcher = FileSystems.getDefault()
                        .getPathMatcher("glob:" + segment);
                if (Files.isDirectory(folder)) {
                    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(folder)) {
                        for (Path filePath : directoryStream) {
                            if (matcher.matches(filePath.getFileName())) {
                                pathArrayList.add(filePath);
                            }
                        }
                    } catch (IOException ex) {
                        logger.error("", ex);
                    }

                }
            }
            filePaths = pathArrayList;
        }
        List<File> files = new ArrayList<>();
        for (Path filePath : filePaths) {
            files.add(filePath.toFile());
        }
        return files;
    }

    @SneakyThrows
    public static File getDirectory(String folderName) {
        logger.info("Folder: " + folderName);
        FileInputResource fileInputResource = new FileInputResource(folderName);
        Path path = Paths.get(fileInputResource.getFile().toURI());
        if (!Files.exists(path)) {
            path = Files.createDirectories(path);
        }
        return path.toFile();
    }

    @SneakyThrows
    public static File toFile(URL url) {
        return Paths.get(url.toURI()).toFile();
    }

    @SneakyThrows
    public static Path getPath(String fileName, boolean overwrite) {
        Path path = Paths.get(fileName);
        if (Files.exists(path)) {
            if (Files.isDirectory(path)) {
                throw new IOException("File '" + fileName + "' exists but is a directory");
            }

        } else {
            Files.createDirectories(path.getParent());
            Files.createFile(path);
        }
        final boolean append = !overwrite;
        new FileOutputStream(path.toFile(), append).close();

        return path;
    }

    public static File createFile(String fileName, boolean overwrite){
        return getPath(fileName, overwrite).toFile();
    }


    public static void main(String[] args) {
        System.err.println(FileUtils.getFiles(new File(""), "/essentialprogramming/dashencrypt/*/video/*.xml"));
    }
}

package com.util.io;

import com.util.text.StringUtils;
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
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class FileUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    public static List<File> getFiles(String basePath, String pattern) {
        Path path = Paths.get(basePath);
        if (path.isAbsolute()) {
            assert Files.isDirectory(path);
        } else {
            File base = new File("");
            assert base.isDirectory();
            path = base.getAbsoluteFile().toPath();
            if (StringUtils.isNotEmpty(basePath)) {
                path = path.resolve(basePath + File.separator);
            }
        }

        // Support all OS specific file separators
        pattern = String.join(File.separator, pattern.split("/"));
        List<String> parts = new ArrayList<>(Arrays.asList(pattern.split(Pattern.quote(File.separator))));
        parts.removeIf(item -> item == null || "".equals(item));

        while (!parts.isEmpty() && parts.size() > 1) {
            String segment = parts.remove(0);
            path = path.resolve(segment + File.separator);
        }
        String filePattern = parts.remove(0);
        List<Path> filePaths ;

        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + filePattern);
        try (Stream<Path> walk = Files.walk(path)){
            filePaths = walk.filter(filePath -> matcher.matches(filePath.getFileName()))
                    .collect(Collectors.toList());

        } catch (IOException ex) {
            filePaths = new ArrayList<>();
            logger.error("", ex);
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

    public static File createFile(String fileName, boolean overwrite) {
        return getPath(fileName, overwrite).toFile();
    }


    public static void main(String[] args) {
        System.err.println(FileUtils.getFiles("", "videos-submitted/*.mp4"));
        System.err.println(FileUtils.getFiles("videos-submitted", "/*.mp4"));
    }
}

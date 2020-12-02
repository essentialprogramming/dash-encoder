package com.service;

import com.api.controller.InMemoryFileHolder;
import com.util.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Queue;

@Service
public class UploadService {

    private final InMemoryFileHolder inMemoryFileHolder;
    String FILE_PATH;

    @Autowired
    public UploadService() {
        this.inMemoryFileHolder = new InMemoryFileHolder();
        File uploadFolder = FileUtils.getDirectory("videos-submitted");
        FILE_PATH = uploadFolder.toString() + File.separator;
    }

    @Transactional
    public void upload(InputStream uploadedInputStream, FormDataContentDisposition fileDetails) {
        String uploadedFileLocation = FILE_PATH + fileDetails.getFileName();
        inMemoryFileHolder.getInputList().add(uploadedFileLocation);
        writeToFile(uploadedInputStream, uploadedFileLocation);
    }

    public Queue<String> getList() {
        return inMemoryFileHolder.getInputList();
    }

    private void writeToFile(InputStream uploadedInputStream, String targetFileName) {
        try {
            Path targetFile = FileUtils.getPath(targetFileName, true);
            Files.copy(uploadedInputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);

            IOUtils.closeQuietly(uploadedInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

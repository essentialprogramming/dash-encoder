package com.api.controller;

import com.dash.encoder.DASHEncoder;
import com.dash.trackoptions.FileSelector;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import com.service.UploadService;
import com.web.json.JsonResponse;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

@Path("/dash/")
public class DASHController {

    private final UploadService uploadService;

    @Autowired
    public DASHController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @POST
    @Path("upload/file")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonResponse uploadFile(@FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetails) {

        this.uploadService.upload(uploadedInputStream, fileDetails);
        return new JsonResponse()
                .with("status", "Files successfully uploaded.")
                .done();
    }

    @GET
    @Path("fragment")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonResponse fragment() {
        Queue<String> fileList = uploadService.getList();
        List<FileSelector> fileSelectors = createFileSelectors(fileList);

        DASHEncoder dashEncoder = new DASHEncoder(fileSelectors);
        dashEncoder.encode();
        return new JsonResponse()
                .with("status", "File(s) successfully fragmented.")
                .done();
    }

    public List<FileSelector> createFileSelectors(Queue<String> inputStreams) {
        return inputStreams.stream().map(this::createFileSelector).collect(Collectors.toList());
    }

    @SneakyThrows
    private FileSelector createFileSelector(String file)  {
        return new FileSelector(file);
    }
}

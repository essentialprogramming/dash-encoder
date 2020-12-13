package com.api.controller;

import com.dash.encoder.DASHEncoder;
import com.dash.fileselector.FileSelector;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import com.api.service.UploadService;
import com.web.json.JsonResponse;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.util.Collections;

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
        FileSelector fileSelector = createFileSelector("videos-submitted/*.mp4");

        DASHEncoder dashEncoder = new DASHEncoder(Collections.singletonList(fileSelector));
        dashEncoder.encode();
        return new JsonResponse()
                .with("status", "File(s) successfully segmented.")
                .done();
    }

    @SneakyThrows
    private static FileSelector createFileSelector(String path)  {
        return new FileSelector(path);
    }
}

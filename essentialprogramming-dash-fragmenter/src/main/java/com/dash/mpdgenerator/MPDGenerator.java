package com.dash.mpdgenerator;

import io.mpd.MPDParser;
import io.mpd.data.BaseURL;
import io.mpd.data.MPD;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class MPDGenerator {

    final static String MANIFEST_FILE_NAME = "manifest.mpd";
    final static String BASE_URL_PREFIX = "/api/video/stream/mp4/";

    final static MPDParser mpdParser = new MPDParser();

    public static List<BaseURL> getBaseUrlValue(String trackName) {
        BaseURL baseURL = new BaseURL(BASE_URL_PREFIX + FilenameUtils.getBaseName(trackName));
        return Collections.singletonList(baseURL);
    }

    public static void writeManifestToFile(MPD mpdObject) throws IOException {
        String mpdXml = mpdParser.writeAsString(mpdObject);
        BufferedWriter writer = new BufferedWriter(new FileWriter(MANIFEST_FILE_NAME));
        writer.write(mpdXml);
        writer.close();
    }
}

package com.dash.mp4;



import io.mpd.data.Representation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.atomic.AtomicInteger;

public class Mp4Writer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Mp4Writer.class);

    public static long writeOnDemand(Mp4Representation mp4Representation, Representation representation, File outputDir) throws IOException {
        assert representation.getBaseURLs().size() == 1;
        assert representation.getBaseURLs().get(0).getValue() != null && !"".equals(representation.getBaseURLs().get(0).getValue());


        File outFile = new File(outputDir, representation.getBaseURLs().get(0).getValue().split("/")[5] + ".mp4");


        LOGGER.info("Writing " + outFile.getAbsolutePath());

        final FileOutputStream fileOutputStream = new FileOutputStream(outFile);
        final WritableByteChannel channel = Channels.newChannel(fileOutputStream);

        LOGGER.info("Writing init segment");
        mp4Representation.getInitSegment().writeContainer(channel);

        LOGGER.info("Writing index segment");
        mp4Representation.getIndexSegment().writeContainer(channel);

        LOGGER.info("Writing segments");
        AtomicInteger atomicInteger = new AtomicInteger();
        for (org.mp4parser.Container fragment : mp4Representation) {
            LOGGER.info("Writing segment " + atomicInteger.getAndIncrement());
            fragment.writeContainer(channel);
        }

        channel.close();

        return outFile.length();
    }
}

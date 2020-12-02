package com.dash.encoder;

import com.dash.trackoptions.FileSelector;
import com.dash.mpdgenerator.MPDGenerator;
import com.exception.ErrorCode;
import com.util.exceptions.ServiceException;
import io.mpd.data.*;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DASHEncoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DASHEncoder.class);

    private final DashSegmentationService segmentationService = DashSegmentationService.getInstance();
    private final List<FileSelector> inputs;

    public DASHEncoder(List<FileSelector> inputs) {
        this.inputs = inputs;
    }

    public void encode() {
        try {
            long startTime = System.currentTimeMillis();
            SegmentationResult<Period> segmentationResult = segmentationService.getFragments(inputs);

            MPD mpdObject = MPD.builder()
                    .withProgramInformations(ProgramInformation.builder()
                            .withMoreInformationURL("https://github.com/essentialprogramming")
                            .build())
                    .withMinBufferTime(Duration.ofDays(0)
                            .plusHours(0).plusMinutes(0).plusSeconds(4))
                    .withMediaPresentationDuration(segmentationResult.getPeriod().getDuration())
                    .withProfiles(Profile.MPEG_DASH_ON_DEMAND)
                    .withType(io.mpd.data.PresentationType.STATIC)
                    .withPeriods(segmentationResult.getPeriod())
                    .build();

            MPDGenerator.writeManifestToFile(mpdObject);
            LOGGER.info(String.format("Finished fragmenting of %dMB in %.1fs", segmentationResult.getTotalSize() / 1024 / 1024, (double) (System.currentTimeMillis() - startTime) / 1000));

        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new ServiceException(ErrorCode.ERROR_CREATING_MPD_FILE);
        }
    }

}

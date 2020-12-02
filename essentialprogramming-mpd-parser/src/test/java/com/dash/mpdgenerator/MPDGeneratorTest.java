package com.dash.mpdgenerator;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.mpd.MPDParser;
import io.mpd.data.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class MPDGeneratorTest {
    XmlMapper xmlMapper = new XmlMapper();
    MPDParser mpdParser = new MPDParser();
    @Test
    public void create_MPD_Success() throws Exception {
        MPD mpd = MPD.builder()
                .withProfiles(Profile.MPEG_DASH_ON_DEMAND, Profile.MPEG_DASH_LIVE)
                .withInteroperabilityPointsAndExtensions("mupp2k")
                .withMediaPresentationDuration(Duration.ofHours(1))
                .withPeriods(Period.builder()
                        .withAdaptationSet(AdaptationSet.builder()
                                        .withId(1L)
                                        .withMimeType("video/mp4")
                                        .withContentType("video")
                                        .withWidth(1024L)
                                        .withHeight(1024L)
                                        .withFrameRate(FrameRate.of(25))
                                        .withPar(Ratio.of(1, 1))
                                        .withSubsegmentAlignment("true")
                                        .withRepresentations(Representation.builder()
                                                .withId("x")
                                                .withBandwidth(4)
                                                .withSegmentTemplate(SegmentTemplate.builder()
                                                        .withSegmentTimeline(Segment.of(0, 4), Segment.of(4, 4))
                                                        .build())
                                                .build())
                                        .build(),
                                AdaptationSet.builder()
                                        .withId(2)
                                        .withWidth(1024)
                                        .withHeight(1024)
                                        .withFrameRate(FrameRate.of(25))
                                        .withMimeType("video/mp4")
                                        .withContentType("video")
                                        .withPar(Ratio.of(1, 1))
                                        .withSubsegmentAlignment("true")
                                        .withRepresentations(Representation.builder()
                                                .withId("x")
                                                .withBandwidth(4)
                                                .withSegmentTemplate(SegmentTemplate.builder()
                                                        .withSegmentTimeline(Segment.of(0, 4), Segment.of(4, 4))
                                                        .build())
                                                .build())
                                        .build())
                        .build())
                .build();
        String xml = mpdParser.writeAsString(mpd);

        System.out.println(xml);
    }
}

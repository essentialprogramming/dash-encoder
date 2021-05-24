package com.dash.mpd;

import com.dash.util.AudioChannelConfiguration;
import com.dash.mpdgenerator.MPDGenerator;
import com.dash.mpd.util.RepresentationUtil;
import com.dash.mpd.util.SegmentBaseUtil;
import com.dash.mp4.Mp4Representation;
import com.dash.util.DashHelper;
import io.mpd.data.Representation;
import io.mpd.data.SegmentBase;
import io.mpd.data.descriptor.GenericDescriptor;
import org.mp4parser.boxes.sampleentry.AudioSampleEntry;
import org.mp4parser.muxer.Track;

import java.util.Collections;
import java.util.List;

public class RepresentationBuilder {

    public static Representation buildVideoRepresentation(Track track, Mp4Representation mp4Representation) {

        return Representation.builder()
                .withId(RepresentationUtil.getRepresentationId(track.getName()))
                .withBandwidth(RepresentationUtil.getBandwidth(track))
                .withWidth(RepresentationUtil.getWidth(track))
                .withHeight(RepresentationUtil.getHeight(track))
                .withCodecs(RepresentationUtil.getCodecs(track))
                .withBaseURLs(MPDGenerator.getBaseUrlValue(track.getName(), "video"))
                .withFrameRate(RepresentationUtil.getFrameRate(track))
                .withSegmentBase(
                        SegmentBase.builder()
                                .withTimescale(SegmentBaseUtil.getSegmentBaseTimescale(mp4Representation))
                                .withIndexRangeExact(true)
                                .withIndexRange(SegmentBaseUtil.getSegmentBaseIndexRange(mp4Representation))
                                .withInitialization(SegmentBaseUtil.getSegmentBaseInitialization(mp4Representation, MPDGenerator.getBaseUrlValue(track.getName(), "video")))
                                .build())
                .build();
    }

    public static List<Representation> buildAudioRepresentation(Track track, Mp4Representation mp4RepresentationBuilder) {

        AudioSampleEntry audioSampleEntry = RepresentationUtil.getAudioSampleEntry(track);
        assert audioSampleEntry != null;

        GenericDescriptor audioChannelConfiguration;
        AudioChannelConfiguration channelConfiguration = AudioChannelConfiguration.getChannelConfiguration(audioSampleEntry);
        if (channelConfiguration != null) {
            audioChannelConfiguration = new GenericDescriptor(channelConfiguration.schemeIdUri, channelConfiguration.value);
        } else {
            audioChannelConfiguration = new GenericDescriptor("", "");
        }

        Representation audioRepresentation = buildAudioRepresentation(mp4RepresentationBuilder,
                track, audioSampleEntry, audioChannelConfiguration);


        return Collections.singletonList(audioRepresentation);
    }

    private static Representation buildAudioRepresentation(Mp4Representation mp4Representation,
                                                           Track track,
                                                           AudioSampleEntry audioSample,
                                                           GenericDescriptor audioChannelConfigurations) {
        return Representation.builder()
                .withId(RepresentationUtil.getRepresentationId(track.getName()))
                .withCodecs(RepresentationUtil.getCodecs(track))
                .withAudioSamplingRate("" + DashHelper.getAudioSamplingRate(audioSample))
                .withBandwidth(RepresentationUtil.getBandwidth(track))
                .withBaseURLs(MPDGenerator.getBaseUrlValue(RepresentationUtil.getRepresentationId(track.getName()) + ".mp4", "audio"))
                .withAudioChannelConfigurations(audioChannelConfigurations)
                .withSegmentBase(
                        SegmentBase.builder()
                                .withTimescale(SegmentBaseUtil.getSegmentBaseTimescale(mp4Representation))
                                .withIndexRangeExact(true)
                                .withIndexRange(SegmentBaseUtil.getSegmentBaseIndexRange(mp4Representation))
                                .withInitialization(SegmentBaseUtil.getSegmentBaseInitialization(mp4Representation, MPDGenerator.getBaseUrlValue(track.getName(),"audio")))
                                .build())
                .build();
    }

}
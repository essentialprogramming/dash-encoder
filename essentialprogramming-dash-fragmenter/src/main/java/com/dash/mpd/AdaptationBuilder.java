package com.dash.mpd;

import com.dash.mpd.util.AdaptionSetUtil;
import io.mpd.data.AdaptationSet;
import io.mpd.data.Ratio;
import io.mpd.data.Representation;
import io.mpd.data.RepresentationBase;
import io.mpd.data.descriptor.Descriptor;

import java.util.List;
import java.util.Locale;

public class AdaptationBuilder {

    private static final String AUDIO_MIME_TYPE = "audio/mp4";
    private static final String VIDEO_MIME_TYPE = "video/mp4";
    private static final String DEFAULT_LANGUAGE = "eng";

    private static final String ADAPTATION_PROFILE = "urn:mpeg:dash:profile:isoff-on-demand:2011";

    public static AdaptationSet buildAudioAdaptation(Descriptor audioRoleDescriptor, List<Representation> audioRepresentations) {
        return AdaptationSet.builder()
                .withLang(new Locale(DEFAULT_LANGUAGE).toLanguageTag())
                .withFrameRate(AdaptionSetUtil.adjustAdaptationSetFrameRate(audioRepresentations,Representation::getFrameRate))
                .withMimeType(AUDIO_MIME_TYPE)
                .withProfiles(ADAPTATION_PROFILE)
                .withRepresentations(audioRepresentations)
                .build();
    }


    public static AdaptationSet buildVideoAdaptation(List<Representation> videoRepresentations) {
        return AdaptationSet.builder()
                .withMinWidth(AdaptionSetUtil.adjustMin(videoRepresentations,Representation::getWidth))
                .withMaxWidth(AdaptionSetUtil.adjustMax(videoRepresentations,Representation::getWidth))
                .withMinBandwidth(AdaptionSetUtil.adjustMin(videoRepresentations,Representation::getBandwidth))
                .withMaxBandwidth(AdaptionSetUtil.adjustMax(videoRepresentations,Representation::getBandwidth))
                .withMinHeight(AdaptionSetUtil.adjustMin(videoRepresentations,Representation::getHeight))
                .withMaxHeight(AdaptionSetUtil.adjustMax(videoRepresentations,RepresentationBase::getHeight))
                .withFrameRate(AdaptionSetUtil.adjustAdaptationSetFrameRate(videoRepresentations,Representation::getFrameRate))
                .withMimeType(VIDEO_MIME_TYPE)
                .withProfiles(ADAPTATION_PROFILE)
                .withSar(new Ratio((long) 1,(long) 1))
                .withRepresentations(videoRepresentations)
                .build();
    }
}

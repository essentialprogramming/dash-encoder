package com.dash.mpd.util;

import com.dash.util.DashHelper;
import io.mpd.data.FrameRate;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.math.Fraction;
import org.mp4parser.boxes.sampleentry.AudioSampleEntry;
import org.mp4parser.boxes.sampleentry.SampleEntry;
import org.mp4parser.muxer.Sample;
import org.mp4parser.muxer.Track;

import java.util.*;
import java.util.List;

public class RepresentationUtil {

    public static String getRepresentationId(String trackName) {
        return FilenameUtils.getBaseName(trackName);
    }
    /**
     *
       Average bandwidth for the stream (number of bytes divided by duration)
     */
    public static long getBandwidth(Track track) {
        long size = 0;
        List<Sample> samples = track.getSamples();
        int increment = samples.size() / Math.min(samples.size(), 10000);
        int sampleSize = 0;
        for (int i = 0; i < (samples.size() - increment); i += increment) {
            size += samples.get(i).getSize();
            sampleSize++;
        }
        sampleSize = sampleSize > 0 ? sampleSize : 1; // avoid division by zero
        size = (size / sampleSize) * track.getSamples().size();

        double duration = (double) track.getDuration() / track.getTrackMetaData().getTimescale();
        return (long) ((size * 8 / duration / 100)) * 100;
    }

    public static long getWidth(Track track) {
        return (long) track.getTrackMetaData().getWidth();
    }

    public static long getHeight(Track track) {

        return (long) track.getTrackMetaData().getHeight();
    }

    public static String getCodecs(Track track) {
        LinkedHashSet<String> codecs = new LinkedHashSet<>();
        for (SampleEntry sampleEntry : track.getSampleEntries()) {
            codecs.add(DashHelper.getRfc6381Codec(sampleEntry));
        }
        return String.join(",", codecs);
    }

    public static AudioSampleEntry getAudioSampleEntry(Track track) {
        AudioSampleEntry audioSampleEntry;
        for (SampleEntry sampleEntry : track.getSampleEntries()) {
            if (sampleEntry instanceof AudioSampleEntry) {
                audioSampleEntry = (AudioSampleEntry) sampleEntry;
                return audioSampleEntry;
            }
        }
        return null;
    }


    public static FrameRate getFrameRate(Track track) {
        double framesPerSecond = (double) (track.getSamples().size() * track.getTrackMetaData().getTimescale()) / track.getDuration();
        return convertFrameRate(framesPerSecond);
    }

    private static FrameRate convertFrameRate(double vRate) {
        Fraction f1 = Fraction.getFraction((int) (vRate * 1001), 1001);
        Fraction f2 = Fraction.getFraction((int) (vRate * 1000), 1000);
        double d1 = Math.abs(f1.doubleValue() - vRate);
        double d2 = Math.abs(f2.doubleValue() - vRate);
        if (d1 < d2) {
            return new FrameRate(Long.parseLong(f1.toString().split("/")[0]), Long.valueOf(f1.toString().split("/")[1]));
        } else {
            return new FrameRate(Long.parseLong(f2.toString().split("/")[0]), Long.valueOf(f2.toString().split("/")[1]));

        }
    }

}

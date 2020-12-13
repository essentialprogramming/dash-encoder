package com.dash.mp4.util;

import com.dash.util.DashHelper;
import org.mp4parser.muxer.Track;

import java.util.ArrayList;
import java.util.List;

public class Mp4Utils {

    public static boolean hasTrack(List<Track> tracks, String... codecs) {
        for (Track track : tracks) {
            String codec = DashHelper.getFormat(track);
            if (codec == null) {
                return false;
            } else {
                for (String oneOf : codecs) {
                    if (codec.equals(oneOf)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static List<Track> getTrack(List<Track> tracks, String... codecs) {
        List<Track> theTracks = new ArrayList<>();

        for (Track track : tracks) {
            String codec = DashHelper.getFormat(track);
            if (codec != null) {
                for (String oneOf : codecs) {
                    if (codec.equals(oneOf)) {
                        theTracks.add(track);
                    }
                }
            }
        }
        return theTracks;
    }
}

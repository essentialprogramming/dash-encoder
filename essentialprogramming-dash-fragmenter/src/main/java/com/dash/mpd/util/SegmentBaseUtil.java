package com.dash.mpd.util;

import com.dash.mp4.Mp4Representation;
import io.mpd.data.BaseURL;
import io.mpd.data.URLType;
import org.mp4parser.Box;
import org.mp4parser.boxes.iso14496.part12.MovieHeaderBox;
import org.mp4parser.tools.Path;

import java.util.List;

public class SegmentBaseUtil {

    public static long getSegmentBaseTimescale(Mp4Representation mp4Representation) {
        return ((MovieHeaderBox) Path.getPath(mp4Representation.getInitSegment(), "moov[0]/mvhd[0]")).getTimescale();
    }

    public static String getSegmentBaseIndexRange(Mp4Representation mp4Representation) {
        long initSize = computeSegmentBaseInitSize(mp4Representation);
        long indexSize = 0;
        for (Box box : mp4Representation.getIndexSegment().getBoxes()) {
            indexSize += box.getSize();
        }

        return String.format("%s-%s", initSize, initSize + indexSize - 1);
    }

    public static URLType getSegmentBaseInitialization(Mp4Representation mp4Representation, List<BaseURL> baseURLS) {
        long initSize = computeSegmentBaseInitSize(mp4Representation);
        return new URLType(null,String.format("0-%s", initSize - 1));
    }

    private static long computeSegmentBaseInitSize(Mp4Representation mp4Representation){
        long initSize = 0;
        for (Box box : mp4Representation.getInitSegment().getBoxes()) {
            initSize += box.getSize();
        }
        return initSize;
    }

}

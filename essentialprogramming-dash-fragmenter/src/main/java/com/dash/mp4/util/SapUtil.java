package com.dash.mp4.util;

import org.mp4parser.boxes.iso14496.part12.SampleFlags;
import org.mp4parser.boxes.iso14496.part12.TrackExtendsBox;
import org.mp4parser.boxes.iso14496.part12.TrackRunBox;

import java.util.Arrays;

public class SapUtil {

    public static byte getFirstFrameSapType(long[] ptss, SampleFlags sapSampleFlags) {
        // I_SAP, T_SAP, I_SAU, T_DEC, T_EPT,  T_PTF
        if (sapSampleFlags.isSampleIsDifferenceSample()) {
            return 0;
        } else {
            long sapTS = ptss[0];
            Arrays.sort(ptss);
            if (sapTS == ptss[0]) {
                return 1; // pts = cts
            } else {
                return 2; // pts != cts
            }
        }
    }


    public static SampleFlags getSampleFlags(int i, TrackRunBox trackRunBox, TrackExtendsBox trackExtendsBox) {
        return trackRunBox.isFirstSampleFlagsPresent() ? trackRunBox.getFirstSampleFlags() :
                (trackRunBox.isSampleFlagsPresent() ? trackRunBox.getEntries().get(i).getSampleFlags() : trackExtendsBox.getDefaultSampleFlags());

    }

}

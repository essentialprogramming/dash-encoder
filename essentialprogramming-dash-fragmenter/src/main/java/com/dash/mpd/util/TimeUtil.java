package com.dash.mpd.util;

import org.mp4parser.Container;
import org.mp4parser.boxes.iso14496.part12.EditListBox;
import org.mp4parser.boxes.iso14496.part12.MediaHeaderBox;
import org.mp4parser.boxes.iso14496.part12.MovieHeaderBox;
import org.mp4parser.boxes.iso14496.part12.TrackRunBox;
import org.mp4parser.tools.Path;

import java.util.List;

public class TimeUtil {


    public static long getDuration(TrackRunBox trackRunBox) {
        long[] ptss = new long[trackRunBox.getEntries().size()];
        long duration = 0;
        for (int index = 0; index < ptss.length; index++) {
            duration += trackRunBox.getEntries().get(index).getSampleDuration();
        }
        return duration;
    }



    public static long[] getPtss(TrackRunBox trackRunBox) {
        long currentTime = 0;
        long[] ptss = new long[trackRunBox.getEntries().size()];
        for (int j = 0; j < ptss.length; j++) {
            ptss[j] = currentTime + trackRunBox.getEntries().get(j).getSampleCompositionTimeOffset();
            currentTime += trackRunBox.getEntries().get(j).getSampleDuration();
        }
        return ptss;
    }


    public static long getTimeMappingEditTime(Container file) {
        final EditListBox editList = Path.getPath(file, "moov[0]/trak[0]/edts[0]/elst[0]");
        final MediaHeaderBox mediaHeaderBox = Path.getPath(file, "moov[0]/trak[0]/mdia[0]/mdhd[0]");
        final MovieHeaderBox movieHeaderBox = Path.getPath(file, "moov[0]/mvhd[0]");

        if (editList != null) {
            double editStartTime = 0;
            final List<EditListBox.Entry> entries = editList.getEntries();
            boolean acceptDwell = true;
            boolean acceptEdit = true;
            for (EditListBox.Entry edit : entries) {
                if (edit.getMediaTime() == -1 && !acceptDwell) {
                    throw new RuntimeException("Cannot accept edit list for processing (1)");
                }
                if (edit.getMediaTime() >= 0 && !acceptEdit) {
                    throw new RuntimeException("Cannot accept edit list for processing (2)");
                }
                if (edit.getMediaTime() == -1) {
                    assert movieHeaderBox != null;
                    editStartTime -= (double) edit.getSegmentDuration() / movieHeaderBox.getTimescale();
                } else /* if edit.getMediaTime() >= 0 */ {
                    assert mediaHeaderBox != null;
                    editStartTime += (double) edit.getMediaTime() / mediaHeaderBox.getTimescale();
                    acceptEdit = false;
                    acceptDwell = false;
                }
            }
            assert mediaHeaderBox != null;
            return (long) (editStartTime * mediaHeaderBox.getTimescale());
        }
        return 0;
    }


}

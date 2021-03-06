package com.dash.encoder;

import com.dash.mp4.Mp4Writer;
import com.dash.mp4.util.Mp4Utils;
import com.dash.mpd.AdaptationBuilder;
import com.dash.mpd.RepresentationBuilder;
import com.dash.fileselector.FileSelector;
import com.dash.mp4.Mp4Representation;
import com.util.io.FileUtils;
import io.mpd.data.*;
import io.mpd.data.descriptor.Descriptor;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.builder.DefaultFragmenterImpl;
import org.mp4parser.muxer.builder.Fragmenter;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.List;

public class DashSegmentationService {
    private static final double MIN_AUDIO_SEGMENT_DURATION = 15;
    private static final double MIN_VIDEO_SEGMENT_DURATION = 4;

    private static final String FILE_PATH = "videos-formatted";
    private final File outputDirectory =  FileUtils.getDirectory(FILE_PATH);

    private static class DashSegmentationServiceHolder {
        static final DashSegmentationService DASH_SEGMENTATION_SERVICE = new DashSegmentationService();
    }

    private DashSegmentationService() {
    }

    public static DashSegmentationService getInstance(){
        return DashSegmentationServiceHolder.DASH_SEGMENTATION_SERVICE;
    }

    public SegmentationResult<Period> getFragments(List<FileSelector> inputs) throws IOException {

        Fragmenter videoFragmenter = new DefaultFragmenterImpl(MIN_VIDEO_SEGMENT_DURATION);
        Fragmenter audioFragmenter = new DefaultFragmenterImpl(MIN_AUDIO_SEGMENT_DURATION);

        List<Representation> audioRepresentations = Collections.emptyList();
        List<Representation> videoRepresentations = new LinkedList<>();

        Duration minDuration = java.time.Duration.ofSeconds(Integer.MAX_VALUE);
        Descriptor audioRoleDescriptor = new Descriptor(null,null);

        long totalSize = 0;

        for (FileSelector inputSource : inputs) {
            List<Track> tracks = inputSource.getSelectedTracks();
            if (Mp4Utils.hasTrack(tracks, "hvc1", "hev1", "avc1", "avc3")) {
                for (Track track : Mp4Utils.getTrack(tracks, "hvc1", "hev1", "avc1", "avc3")) {
                    long[] fragments = videoFragmenter.sampleNumbers(track);
                    Mp4Representation mp4Representation = new Mp4Representation(track, null, "video", fragments, fragments);
                    Representation videoRepresentation = RepresentationBuilder.buildVideoRepresentation(track, mp4Representation);
                    minDuration = updateDuration((double) track.getDuration() / track.getTrackMetaData().getTimescale(), minDuration);
                    totalSize += Mp4Writer.writeOnDemand(mp4Representation, videoRepresentation, outputDirectory);

                    videoRepresentations.add(videoRepresentation);
                }
            }
            for (Track track : Mp4Utils.getTrack(tracks, "dtsl", "dtse", "ec-3", "ac-3", "mlpa", "mp4a")) {
                long[] fragments = audioFragmenter.sampleNumbers(track);
                Mp4Representation mp4Representation = new Mp4Representation(track, null, "audio", fragments, fragments);
                audioRepresentations = RepresentationBuilder.buildAudioRepresentation(track,mp4Representation);
                minDuration = updateDuration((double) track.getDuration() / track.getTrackMetaData().getTimescale(), minDuration);
                totalSize += Mp4Writer.writeOnDemand(mp4Representation, audioRepresentations.get(0), outputDirectory);

            }
        }

        Period period =  Period.builder().
                withId("main").
                withStart(java.time.Duration.ofNanos(0)).
                withDuration(minDuration).
                    withAdaptationSets(Arrays.asList(
                        AdaptationBuilder.buildVideoAdaptation(videoRepresentations),
                        AdaptationBuilder.buildAudioAdaptation(audioRoleDescriptor, audioRepresentations))).
                build();

        return new SegmentationResult<>(period, totalSize);


    }

    static Duration updateDuration(double duration, Duration lastDuration) {
        if (duration > lastDuration.getSeconds()) return lastDuration;
        return Duration.ofDays(0)
                .plusHours((int) (duration / 3600))
                .plusMinutes((int) ((duration % 3600) / 60))
                .plusSeconds( (int) (duration % 60));
     }

}

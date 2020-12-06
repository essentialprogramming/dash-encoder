package com.dash.mp4;

import com.dash.mp4.util.SapHelper;
import com.dash.mp4.util.TimeUtil;
import org.mp4parser.*;
import org.mp4parser.boxes.iso14496.part12.*;
import org.mp4parser.boxes.iso23001.part7.CencSampleAuxiliaryDataFormat;
import org.mp4parser.boxes.iso23001.part7.ProtectionSystemSpecificHeaderBox;
import org.mp4parser.boxes.iso23001.part7.SampleEncryptionBox;
import org.mp4parser.boxes.iso23001.part7.TrackEncryptionBox;
import org.mp4parser.boxes.sampleentry.SampleEntry;
import org.mp4parser.boxes.samplegrouping.GroupEntry;
import org.mp4parser.boxes.samplegrouping.SampleGroupDescriptionBox;
import org.mp4parser.boxes.samplegrouping.SampleToGroupBox;
import org.mp4parser.muxer.DataSource;
import org.mp4parser.muxer.Edit;
import org.mp4parser.muxer.Sample;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.tracks.encryption.CencEncryptedTrack;
import org.mp4parser.tools.IsoTypeWriter;
import org.mp4parser.tools.Path;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import static org.mp4parser.tools.CastUtils.l2i;

public class Mp4Representation extends AbstractList<Container> {
    protected Track track;
    protected final List<ProtectionSystemSpecificHeaderBox> psshs;
    protected Date date = Date.from(Instant.now());
    protected String source;
    protected long[] segmentStartSamples;
    protected long[] fragmentStartSamples;

    public Mp4Representation(Track track, List<ProtectionSystemSpecificHeaderBox> protectionSystemSpecificHeaderBoxes, String source, long[] segmentStartSamples, long[] fragmentStartSamples) {
        this.track = track;
        this.psshs = protectionSystemSpecificHeaderBoxes;
        this.source = source;
        this.fragmentStartSamples = fragmentStartSamples;
        this.segmentStartSamples = segmentStartSamples;
    }

    public Track getTrack() {
        return track;
    }

    public Container getInitSegment() {
        List<Box> initSegment = new ArrayList<>();
        List<String> minorBrands = new ArrayList<>();
        minorBrands.add("isom");
        minorBrands.add("iso6");
        minorBrands.add("avc1");
        initSegment.add(new FileTypeBox("isom", 0, minorBrands));
        initSegment.add(createMovieBox());
        return new BasicContainer(initSegment);
    }

    protected Box createMovieBox() {
        MovieBox movieBox = new MovieBox();

        movieBox.addBox(createMovieHeaderBox());
        movieBox.addBox(createTrack(track));
        movieBox.addBox(createMovieExtendsBox());
        if (psshs != null) {
            for (ProtectionSystemSpecificHeaderBox pssh : psshs) {
                movieBox.addBox(pssh);
            }
        }
        // metadata here
        return movieBox;
    }


    protected Box createTrackExtendsBox(Track track) {
        TrackExtendsBox trackExtendsBox = new TrackExtendsBox();
        trackExtendsBox.setTrackId(track.getTrackMetaData().getTrackId());
        trackExtendsBox.setDefaultSampleDescriptionIndex(1);
        trackExtendsBox.setDefaultSampleDuration(0);
        trackExtendsBox.setDefaultSampleSize(0);
        SampleFlags sf = new SampleFlags();
        if ("soun".equals(track.getHandler()) || "subt".equals(track.getHandler()) || "text".equals(track.getHandler())) {
            // as far as I know there is no audio encoding
            // where the sample are not self contained.
            // same seems to be true for subtitle tracks
            sf.setSampleDependsOn(2);
            sf.setSampleIsDependedOn(2);
        }
        trackExtendsBox.setDefaultSampleFlags(sf);
        return trackExtendsBox;
    }

    protected Box createMovieExtendsBox() {
        final MovieExtendsBox movieExtendsBox = new MovieExtendsBox();
        final MovieExtendsHeaderBox movieExtendsHeaderBox = new MovieExtendsHeaderBox();
        movieExtendsHeaderBox.setVersion(1);
        movieExtendsHeaderBox.setFragmentDuration(track.getDuration());
        movieExtendsBox.addBox(movieExtendsHeaderBox);

        movieExtendsBox.addBox(createTrackExtendsBox(track));
        return movieExtendsBox;
    }

    protected Box createTrackHeaderBox(Track track) {
        TrackHeaderBox trackHeaderBox = new TrackHeaderBox();
        trackHeaderBox.setVersion(1);
        trackHeaderBox.setFlags(7); // enabled, in movie, in preview, in poster

        trackHeaderBox.setAlternateGroup(track.getTrackMetaData().getGroup());
        trackHeaderBox.setCreationTime(track.getTrackMetaData().getCreationTime());
        // We need to take edit list box into account in trackheader duration
        // but as long as I don't support edit list boxes it is sufficient to
        // just translate media duration to movie timescale
        if (track.getEdits().isEmpty()) {
            trackHeaderBox.setDuration(track.getDuration());
        } else {
            long duration = 0;
            for (Edit edit : track.getEdits()) {
                duration += edit.getMediaTime() != -1 ?
                        edit.getSegmentDuration() * track.getTrackMetaData().getTimescale() : 0;
            }
            trackHeaderBox.setDuration(duration);
        }


        trackHeaderBox.setHeight(track.getTrackMetaData().getHeight());
        trackHeaderBox.setWidth(track.getTrackMetaData().getWidth());
        trackHeaderBox.setLayer(track.getTrackMetaData().getLayer());
        trackHeaderBox.setModificationTime(getDate());
        trackHeaderBox.setTrackId(track.getTrackMetaData().getTrackId());
        trackHeaderBox.setVolume(track.getTrackMetaData().getVolume());
        return trackHeaderBox;
    }


    protected Box createMovieHeaderBox() {
        MovieHeaderBox movieHeaderBox = new MovieHeaderBox();
        movieHeaderBox.setVersion(1);
        movieHeaderBox.setCreationTime(getDate());
        movieHeaderBox.setModificationTime(getDate());
        movieHeaderBox.setDuration(0);//no duration in moov for fragmented movies
        long movieTimeScale = track.getTrackMetaData().getTimescale();
        movieHeaderBox.setTimescale(movieTimeScale);
        movieHeaderBox.setNextTrackId(track.getTrackMetaData().getTrackId() + 1);
        return movieHeaderBox;
    }


    protected Box createSampleTableBox(Track track) {
        SampleTableBox sampleTableBox = new SampleTableBox();

        createSampleDescriptionBox(track, sampleTableBox);
        sampleTableBox.addBox(new TimeToSampleBox());
        sampleTableBox.addBox(new SampleToChunkBox());
        sampleTableBox.addBox(new SampleSizeBox());
        sampleTableBox.addBox(new StaticChunkOffsetBox());
        return sampleTableBox;
    }

    protected void createSampleDescriptionBox(Track track, SampleTableBox sampleTableBox) {
        SampleDescriptionBox sampleDescriptionBox = new SampleDescriptionBox();
        sampleDescriptionBox.setBoxes(track.getSampleEntries());
        sampleTableBox.addBox(sampleDescriptionBox);
    }

    protected Box createMediaInformationBox(Track track) {
        MediaInformationBox mediaInformationBox = new MediaInformationBox();
        switch (track.getHandler()) {
            case "vide":
                mediaInformationBox.addBox(new VideoMediaHeaderBox());
                break;
            case "soun":
                mediaInformationBox.addBox(new SoundMediaHeaderBox());
                break;
            case "text":
            case "sbtl":
                mediaInformationBox.addBox(new NullMediaHeaderBox());
                break;
            case "subt":
                mediaInformationBox.addBox(new SubtitleMediaHeaderBox());
                break;
            case "hint":
                mediaInformationBox.addBox(new HintMediaHeaderBox());
                break;
        }
        mediaInformationBox.addBox(createDataInformationBox());
        mediaInformationBox.addBox(createSampleTableBox(track));
        return mediaInformationBox;
    }

    protected Box createMediaHandlerBox(Track track) {
        HandlerBox handlerBox = new HandlerBox();
        handlerBox.setHandlerType(track.getHandler());
        return handlerBox;
    }

    protected Box createMediaHeaderBox(Track track) {
        MediaHeaderBox mediaHeaderBox = new MediaHeaderBox();
        mediaHeaderBox.setCreationTime(track.getTrackMetaData().getCreationTime());
        mediaHeaderBox.setModificationTime(getDate());
        mediaHeaderBox.setDuration(0);//no duration in moov for fragmented movies
        mediaHeaderBox.setTimescale(track.getTrackMetaData().getTimescale());
        mediaHeaderBox.setLanguage(track.getTrackMetaData().getLanguage());
        return mediaHeaderBox;
    }

    public Date getDate() {
        return date;
    }


    protected DataInformationBox createDataInformationBox() {
        DataInformationBox dataInformationBox = new DataInformationBox();
        DataReferenceBox dataReferenceBox = new DataReferenceBox();
        dataInformationBox.addBox(dataReferenceBox);
        DataEntryUrlBox url = new DataEntryUrlBox();
        url.setFlags(1);
        dataReferenceBox.addBox(url);
        return dataInformationBox;
    }

    protected Box createMediaBox(Track track) {
        MediaBox mediaBox = new MediaBox();
        mediaBox.addBox(createMediaHeaderBox(track));
        mediaBox.addBox(createMediaHandlerBox(track));
        mediaBox.addBox(createMediaInformationBox(track));
        return mediaBox;
    }

    protected Box createTrack(Track track) {
        TrackBox trackBox = new TrackBox();
        trackBox.addBox(createTrackHeaderBox(track));
        Box edts = createEdts(track);
        if (edts != null) {
            trackBox.addBox(edts);
        }
        trackBox.addBox(createMediaBox(track));
        return trackBox;
    }

    protected Box createEdts(Track track) {
        if (track.getEdits() != null && track.getEdits().size() > 0) {
            EditListBox elst = new EditListBox();
            elst.setVersion(1);
            List<EditListBox.Entry> entries = new ArrayList<>();

            for (Edit edit : track.getEdits()) {
                entries.add(new EditListBox.Entry(elst,
                        Math.round(edit.getSegmentDuration() * track.getTrackMetaData().getTimescale()),
                        edit.getMediaTime() * track.getTrackMetaData().getTimescale() / edit.getTimeScale(),
                        edit.getMediaRate()));
            }

            elst.setEntries(entries);
            EditBox edts = new EditBox();
            edts.addBox(elst);
            return edts;
        } else {
            return null;
        }
    }


    public int size() {
        return segmentStartSamples.length;
    }

    public Container get(int index) {

        List<Box> moofMdat = new ArrayList<>();
        long startSample = segmentStartSamples[index];
        long endSample;
        if (index + 1 < segmentStartSamples.length) {
            endSample = segmentStartSamples[index + 1];
        } else {
            endSample = track.getSamples().size() - 1;
        }

        long fragmentStartSample;
        long fragmentEndSample;
        do {
            fragmentStartSample = startSample;
            int fIndex = Arrays.binarySearch(fragmentStartSamples, startSample);
            if (fIndex + 1 < fragmentStartSamples.length) {
                fragmentEndSample = fragmentStartSamples[index + 1];
            } else {
                fragmentEndSample = track.getSamples().size() + 1;
            }
            moofMdat.add(createMoof(fragmentStartSample, fragmentEndSample, track, fIndex + 1)); // it's one bases
            moofMdat.add(createMdat(fragmentStartSample, fragmentEndSample));

        } while (fragmentEndSample < endSample);


        return new BasicContainer(moofMdat);
    }


    /**
     * Creates a 'moof' box for a given sequence of samples.
     *
     * @param startSample    low endpoint (inclusive) of the sample sequence
     * @param endSample      high endpoint (exclusive) of the sample sequence
     * @param track          source of the samples
     * @param sequenceNumber the fragment index of the requested list of samples
     * @return the list of TrackRun boxes.
     */
    protected Box createMoof(long startSample, long endSample, Track track, int sequenceNumber) {
        MovieFragmentBox moof = new MovieFragmentBox();
        createMfhd(sequenceNumber, moof);
        createTrackFragmentBox(startSample, endSample, track, moof);

        TrackRunBox firstTrun = moof.getTrackRunBoxes().get(0);
        firstTrun.setDataOffset(1); // dummy to make size correct
        firstTrun.setDataOffset((int) (8 + moof.getSize())); // mdat header + moof size

        return moof;
    }

    protected void createMfhd(int sequenceNumber, MovieFragmentBox parent) {
        MovieFragmentHeaderBox mfhd = new MovieFragmentHeaderBox();
        mfhd.setSequenceNumber(sequenceNumber);
        parent.addBox(mfhd);
    }


    public Container getIndexSegment() {
        SegmentIndexBox segmentIndexBox = new SegmentIndexBox();
        segmentIndexBox.setVersion(0);
        segmentIndexBox.setFlags(0);
        segmentIndexBox.setReserved(0);

        Container initSegment = getInitSegment();
        TrackHeaderBox trackHeaderBox = Path.getPath(initSegment, "moov[0]/trak[0]/tkhd[0]");
        MediaHeaderBox mediaHeaderBox = Path.getPath(initSegment, "moov[0]/trak[0]/mdia[0]/mdhd[0]");
        segmentIndexBox.setReferenceId(trackHeaderBox.getTrackId());
        segmentIndexBox.setTimeScale(mediaHeaderBox.getTimescale());
        // we only have one
        long[] ptss = TimeUtil.getPtss(Path.getPath(get(0), "moof[0]/traf[0]/trun[0]"));
        Arrays.sort(ptss); // index 0 has now the earliest presentation time stamp!
        long timeMappingEdit = TimeUtil.getTimeMappingEditTime(initSegment);
        segmentIndexBox.setEarliestPresentationTime(ptss[0] - timeMappingEdit < 0 ? 0 : ptss[0] - timeMappingEdit);
        List<SegmentIndexBox.Entry> entries = segmentIndexBox.getEntries();

        TrackExtendsBox trackExtendsBox = Path.getPath(initSegment, "moov[0]/mvex[0]/trex[0]");


        for (Container container : this) {
            int size = 0;
            for (Box box : container.getBoxes()) {
                size += l2i(box.getSize());
            }
            MovieFragmentBox moof = Path.getPath(container, "moof[0]");
            SegmentIndexBox.Entry entry = new SegmentIndexBox.Entry();
            entries.add(entry);
            entry.setReferencedSize(size);
            TrackRunBox trackRunBox = Path.getPath(moof, "traf[0]/trun[0]");
            ptss = TimeUtil.getPtss(trackRunBox);
            entry.setSapType(SapHelper.getFirstFrameSapType(ptss, SapHelper.getSampleFlags(0, trackRunBox, trackExtendsBox)));
            entry.setSubsegmentDuration(TimeUtil.getDuration(Path.getPath(moof, "traf[0]/trun[0]")));
            entry.setStartsWithSap((byte) 1); // we know it - no need to lookup
        }

        segmentIndexBox.setFirstOffset(0);
        return new BasicContainer(Collections.singletonList(segmentIndexBox));
    }

    protected void createTrackFragmentBaseMediaDecodeTimeBox(long startSample, Track track, TrackFragmentBox parent) {
        TrackFragmentBaseMediaDecodeTimeBox timeBox = new TrackFragmentBaseMediaDecodeTimeBox();
        timeBox.setVersion(1);
        long startTime = 0;
        long[] times = track.getSampleDurations();
        for (int i = 1; i < startSample; i++) {
            startTime += times[i - 1];
        }
        timeBox.setBaseMediaDecodeTime(startTime);
        parent.addBox(timeBox);
    }

    /**
     * Gets the sizes of a sequence of samples.
     *
     * @param startSample low endpoint (inclusive) of the sample sequence
     * @param endSample   high endpoint (exclusive) of the sample sequence
     * @return the sample sizes in the given interval
     */
    protected long[] getSampleSizes(long startSample, long endSample) {
        List<Sample> samples = getSamples(startSample, endSample);

        long[] sampleSizes = new long[samples.size()];
        for (int i = 0; i < sampleSizes.length; i++) {
            sampleSizes[i] = samples.get(i).getSize();
        }
        return sampleSizes;
    }


    /**
     * Creates one or more track run boxes for a given sequence.
     *
     * @param startSample low endpoint (inclusive) of the sample sequence
     * @param endSample   high endpoint (exclusive) of the sample sequence
     * @param track       source of the samples
     * @param parent      the created box must be added to this box
     */
    protected void createTrackRunBox(long startSample, long endSample, Track track, TrackFragmentBox parent) {
        TrackRunBox trackRunBox = new TrackRunBox();
        trackRunBox.setVersion(1);
        long[] sampleSizes = getSampleSizes(startSample, endSample);

        trackRunBox.setSampleDurationPresent(true);
        trackRunBox.setSampleSizePresent(true);
        List<TrackRunBox.Entry> entries = new ArrayList<>(l2i(endSample - startSample));


        List<CompositionTimeToSample.Entry> compositionTimeEntries = track.getCompositionTimeEntries();
        int compositionTimeQueueIndex = 0;
        CompositionTimeToSample.Entry[] compositionTimeQueue =
                compositionTimeEntries != null && compositionTimeEntries.size() > 0 ?
                        compositionTimeEntries.toArray(new CompositionTimeToSample.Entry[compositionTimeEntries.size()]) : null;
        long compositionTimeEntriesLeft = compositionTimeQueue != null ? compositionTimeQueue[compositionTimeQueueIndex].getCount() : -1;


        trackRunBox.setSampleCompositionTimeOffsetPresent(compositionTimeEntriesLeft > 0);

        // fast forward composition stuff
        for (long i = 1; i < startSample; i++) {
            if (compositionTimeQueue != null) {
                //trun.setSampleCompositionTimeOffsetPresent(true);
                if (--compositionTimeEntriesLeft == 0 && (compositionTimeQueue.length - compositionTimeQueueIndex) > 1) {
                    compositionTimeQueueIndex++;
                    compositionTimeEntriesLeft = compositionTimeQueue[compositionTimeQueueIndex].getCount();
                }
            }
        }

        boolean sampleFlagsRequired = (track.getSampleDependencies() != null && !track.getSampleDependencies().isEmpty() ||
                track.getSyncSamples() != null && track.getSyncSamples().length != 0);

        trackRunBox.setSampleFlagsPresent(sampleFlagsRequired);

        for (int i = 0; i < sampleSizes.length; i++) {
            TrackRunBox.Entry entry = new TrackRunBox.Entry();
            entry.setSampleSize(sampleSizes[i]);
            if (sampleFlagsRequired) {
                //if (false) {
                SampleFlags sflags = new SampleFlags();

                if (track.getSampleDependencies() != null && !track.getSampleDependencies().isEmpty()) {
                    SampleDependencyTypeBox.Entry e = track.getSampleDependencies().get(i);
                    sflags.setSampleDependsOn(e.getSampleDependsOn());
                    sflags.setSampleIsDependedOn(e.getSampleIsDependedOn());
                    sflags.setSampleHasRedundancy(e.getSampleHasRedundancy());
                }
                if (track.getSyncSamples() != null && track.getSyncSamples().length > 0) {
                    // we have to mark non-sync samples!
                    if (Arrays.binarySearch(track.getSyncSamples(), startSample + i) >= 0) {
                        sflags.setSampleIsDifferenceSample(false);
                        sflags.setSampleDependsOn(2);
                    } else {
                        sflags.setSampleIsDifferenceSample(true);
                        sflags.setSampleDependsOn(1);
                    }
                }
                // i don't have sample degradation
                entry.setSampleFlags(sflags);

            }

            entry.setSampleDuration(track.getSampleDurations()[l2i(startSample + i - 1)]);

            if (compositionTimeQueue != null) {
                entry.setSampleCompositionTimeOffset(compositionTimeQueue[compositionTimeQueueIndex].getOffset());
                if (--compositionTimeEntriesLeft == 0 && (compositionTimeQueue.length - compositionTimeQueueIndex) > 1) {
                    compositionTimeQueueIndex++;
                    compositionTimeEntriesLeft = compositionTimeQueue[compositionTimeQueueIndex].getCount();
                }
            }
            entries.add(entry);
        }

        trackRunBox.setEntries(entries);

        parent.addBox(trackRunBox);
    }

    protected void createTrackFragmentBox(long startSample, long endSample, Track track, MovieFragmentBox parent) {
        TrackFragmentBox trackFragmentBox = new TrackFragmentBox();
        parent.addBox(trackFragmentBox);
        createTrackFragmentHeaderBox(trackFragmentBox);
        TrackFragmentHeaderBox trackFragmentHeaderBox = (TrackFragmentHeaderBox) trackFragmentBox.getBoxes().get(trackFragmentBox.getBoxes().size() - 1);
        createTrackFragmentBaseMediaDecodeTimeBox(startSample, track, trackFragmentBox);
        createTrackRunBox(startSample, endSample, track, trackFragmentBox);
        TrackRunBox trackRunBox = (TrackRunBox) trackFragmentBox.getBoxes().get(trackFragmentBox.getBoxes().size() - 1);
        SampleFlags first = null;
        SampleFlags second = null;
        boolean allFllowingSame = true;

        for (TrackRunBox.Entry entry : trackRunBox.getEntries()) {
            if (first == null) {
                first = entry.getSampleFlags();
            } else if (second == null) {
                second = entry.getSampleFlags();
            } else {
                allFllowingSame &= second.equals(entry.getSampleFlags());
            }
        }
        if (allFllowingSame && second != null) {
            trackRunBox.setSampleFlagsPresent(false);
            trackRunBox.setFirstSampleFlags(first);
            trackFragmentHeaderBox.setDefaultSampleFlags(second);
        }

        SampleEntry current = track.getSamples().get(l2i(startSample-1)).getSampleEntry();
        int sdi = 1;
        for (SampleEntry entry : track.getSampleEntries()) {
            if (current.equals(entry)) {
                trackFragmentHeaderBox.setSampleDescriptionIndex(sdi);
            }
            sdi++;
        }


        createSubs(startSample, endSample, track, trackFragmentBox);

        if (track instanceof CencEncryptedTrack) {
            createSaiz(startSample, endSample, (CencEncryptedTrack) track, trackFragmentBox);
            createSenc(startSample, endSample, (CencEncryptedTrack) track, trackFragmentBox);
            createSaio(trackFragmentBox, parent);
        }


        Map<String, List<GroupEntry>> groupEntryFamilies = new HashMap<>();
        for (Map.Entry<GroupEntry, long[]> sg : track.getSampleGroups().entrySet()) {
            String type = sg.getKey().getType();
            List<GroupEntry> groupEntries = groupEntryFamilies.computeIfAbsent(type, k -> new ArrayList<>());
            groupEntries.add(sg.getKey());
        }


        for (Map.Entry<String, List<GroupEntry>> sg : groupEntryFamilies.entrySet()) {
            SampleGroupDescriptionBox sgpd = new SampleGroupDescriptionBox();
            String type = sg.getKey();
            sgpd.setGroupEntries(sg.getValue());
            SampleToGroupBox sbgp = new SampleToGroupBox();
            sbgp.setGroupingType(type);
            SampleToGroupBox.Entry last = null;
            for (int i = l2i(startSample - 1); i < l2i(endSample - 1); i++) {
                int index = 0;
                for (int j = 0; j < sg.getValue().size(); j++) {
                    GroupEntry groupEntry = sg.getValue().get(j);
                    long[] sampleNums = track.getSampleGroups().get(groupEntry);
                    if (Arrays.binarySearch(sampleNums, i) >= 0) {
                        index = j + 1;
                    }
                }
                if (last == null || last.getGroupDescriptionIndex() != index) {
                    last = new SampleToGroupBox.Entry(1, index);
                    sbgp.getEntries().add(last);
                } else {
                    last.setSampleCount(last.getSampleCount() + 1);
                }
            }
            trackFragmentBox.addBox(sgpd);
            trackFragmentBox.addBox(sbgp);
        }


    }

    protected void createSubs(long startSample, long endSample, Track track, TrackFragmentBox traf) {
        SubSampleInformationBox subs = track.getSubsampleInformationBox();
        if (subs != null) {
            SubSampleInformationBox fragmentSubs = new SubSampleInformationBox();
            fragmentSubs.setEntries(subs.getEntries().subList(l2i(startSample - 1), l2i(endSample - 1)));
            traf.addBox(fragmentSubs);
        }

    }

    protected void createTrackFragmentHeaderBox(TrackFragmentBox parent) {
        TrackFragmentHeaderBox trackFragmentHeaderBox = new TrackFragmentHeaderBox();
        SampleFlags sf = new SampleFlags();

        trackFragmentHeaderBox.setDefaultSampleFlags(sf);
        trackFragmentHeaderBox.setBaseDataOffset(-1);

        trackFragmentHeaderBox.setTrackId(track.getTrackMetaData().getTrackId());
        trackFragmentHeaderBox.setDefaultBaseIsMoof(true);
        parent.addBox(trackFragmentHeaderBox);
    }


    protected void createSenc(long startSample, long endSample, CencEncryptedTrack track, TrackFragmentBox parent) {
        TrackEncryptionBox tenc = Path.getPath((Container) track.getSamples().get(l2i(startSample-1)).getSampleEntry(), "sinf[0]/schi[0]/tenc[0]");
        if (tenc != null) {
            SampleEncryptionBox senc = new SampleEncryptionBox();
            senc.setSubSampleEncryption(track.hasSubSampleEncryption());
            senc.setEntries(track.getSampleEncryptionEntries().subList(l2i(startSample - 1), l2i(endSample - 1)));
            parent.addBox(senc);
        }
    }

    protected void createSaio(TrackFragmentBox parent, MovieFragmentBox moof) {
        SampleAuxiliaryInformationOffsetsBox saio = new SampleAuxiliaryInformationOffsetsBox();

        assert parent.getBoxes(TrackRunBox.class).size() == 1 : "Don't know how to deal with multiple Track Run Boxes when encrypting";
        saio.setAuxInfoType("cenc");
        saio.setFlags(1);
        long offset = 0;
        boolean add = false;
        offset += 8; // traf header till 1st child box
        for (Box box : parent.getBoxes()) {
            if (box instanceof SampleEncryptionBox) {
                offset += ((SampleEncryptionBox) box).getOffsetToFirstIV();
                add = true;
                break;
            } else {
                offset += box.getSize();
            }
        }
        offset += 16; // traf header till 1st child box
        for (Box box : moof.getBoxes()) {
            if (box == parent) {
                break;
            } else {
                offset += box.getSize();
            }

        }
        saio.setOffsets(new long[]{offset});
        if (add) {
            parent.addBox(saio);
        }

    }

    protected void createSaiz(long startSample, long endSample, CencEncryptedTrack track, TrackFragmentBox parent) {

        TrackEncryptionBox tenc = Path.getPath((Container) track.getSamples().get(l2i(startSample-1)).getSampleEntry(), "sinf[0]/schi[0]/tenc[0]");
        if (tenc != null) {
            SampleAuxiliaryInformationSizesBox saiz = new SampleAuxiliaryInformationSizesBox();
            saiz.setAuxInfoType("cenc");
            saiz.setFlags(1);
            if (track.hasSubSampleEncryption()) {
                short[] sizes = new short[l2i(endSample - startSample)];
                List<CencSampleAuxiliaryDataFormat> auxs =
                        track.getSampleEncryptionEntries().subList(l2i(startSample - 1), l2i(endSample - 1));
                for (int i = 0; i < sizes.length; i++) {
                    sizes[i] = (short) auxs.get(i).getSize();
                }
                saiz.setSampleInfoSizes(sizes);
            } else {

                saiz.setDefaultSampleInfoSize(tenc.getDefaultIvSize());
                saiz.setSampleCount(l2i(endSample - startSample));
            }
            parent.addBox(saiz);
        }
    }

    protected Box createMdat(final long startSample, final long endSample) {

        class Mdat implements Box {
            Container parent;
            long size_ = -1;

            public long getSize() {
                if (size_ != -1) return size_;
                long size = 8; // I don't expect 2gig fragments
                for (Sample sample : getSamples(startSample, endSample)) {
                    size += sample.getSize();
                }
                size_ = size;
                return size;
            }

            public String getType() {
                return "mdat";
            }

            public void getBox(WritableByteChannel writableByteChannel) throws IOException {
                ByteBuffer header = ByteBuffer.allocate(8);
                IsoTypeWriter.writeUInt32(header, l2i(getSize()));
                header.put(IsoFile.fourCCtoBytes(getType()));
                header.rewind();
                writableByteChannel.write(header);

                List<Sample> samples = getSamples(startSample, endSample);
                for (Sample sample : samples) {
                    sample.writeTo(writableByteChannel);
                }


            }

            public void parse(DataSource fileChannel, ByteBuffer header, long contentSize, BoxParser boxParser) throws IOException {

            }
        }

        return new Mdat();
    }

    /**
     * Gets all samples starting with <code>startSample</code> (one based -&gt; one is the first) and
     * ending with <code>endSample</code> (exclusive).
     *
     * @param startSample low endpoint (inclusive) of the sample sequence
     * @param endSample   high endpoint (exclusive) of the sample sequence
     * @return a <code>List&lt;ByteBuffer&gt;</code> of raw samples
     */
    protected List<Sample> getSamples(long startSample, long endSample) {
        // since startSample and endSample are one-based substract 1 before addressing list elements
        return track.getSamples().subList(l2i(startSample) - 1, l2i(endSample) - 1);
    }


}

package com.dash.trackoptions;


import com.util.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.mp4parser.IsoFile;
import org.mp4parser.boxes.iso14496.part12.TrackBox;
import org.mp4parser.muxer.FileDataSourceImpl;
import org.mp4parser.muxer.FileRandomAccessSourceImpl;
import org.mp4parser.muxer.Mp4TrackImpl;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.tracks.AACTrackImpl;
import org.mp4parser.muxer.tracks.h264.H264TrackImpl;
import org.mp4parser.muxer.tracks.h265.H265TrackImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.dash.util.DashHelper.getTextTrackLocale;


/**
 * Represents one stream.
 */
public class FileSelector {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileSelector.class);

    public static final List<Track> TEXT_TRACKS = new ArrayList<>();
    public static final List<Track> THUMB_TRACKS = new ArrayList<>();
    private List<Track> tracks = new ArrayList<>();
    private List<File> files;


    private static final Set<String> MP4_FILE_EXTENSIONS = new HashSet<>(Arrays.asList("mp4", "m4a", "m4v", "ismv", "isma", "mov"));
    private static final Set<String> THUMB_FILE_EXTENSIONS = new HashSet<>(Arrays.asList("jpg", "jpeg"));
    private static final Set<String> H264_FILE_EXTENSIONS = new HashSet<>(Arrays.asList("h264", "264"));
    private static final Set<String> H265_FILE_EXTENSIONS = new HashSet<>(Arrays.asList("h265", "265"));
    private static final Set<String> AAC_FILE_EXTENSIONS = new HashSet<>(Collections.singletonList("aac"));
    private static final Set<String> TEXT_FILE_EXTENSIONS = new HashSet<>(Arrays.asList("dfxp", "vtt"));
    private static final Map<String, Set<String>> allowedOutputOptions = new HashMap<>();
    private static final Map<String, Set<String>> allowedInputOptions = new HashMap<>();
    private static final Map<String, Set<String>> mandatoryOutputOptions = new HashMap<>();

    static {

        for (String extension : MP4_FILE_EXTENSIONS) {
            allowedInputOptions.put(extension, new HashSet<>(Arrays.asList("type", "lang", "trackNo")));
            allowedOutputOptions.put(extension, new HashSet<>(Arrays.asList("lang", "period", "role")));
            mandatoryOutputOptions.put(extension, Collections.emptySet());
        }
        for (String ext : THUMB_FILE_EXTENSIONS) {
            allowedInputOptions.put(ext, Collections.emptySet());
            allowedOutputOptions.put(ext, new HashSet<>(Arrays.asList("period", "role", "htiles", "vtiles", "thduration")));
            mandatoryOutputOptions.put(ext, new HashSet<>(Arrays.asList("htiles", "vtiles", "thduration")));
        }
        for (String ext : H264_FILE_EXTENSIONS) {
            allowedInputOptions.put(ext, Collections.emptySet());
            allowedOutputOptions.put(ext, new HashSet<>(Arrays.asList("period", "role")));
            mandatoryOutputOptions.put(ext, Collections.emptySet());
        }
        for (String ext : H265_FILE_EXTENSIONS) {
            allowedInputOptions.put(ext, Collections.emptySet());
            allowedOutputOptions.put(ext, new HashSet<>(Arrays.asList("period", "role")));
            mandatoryOutputOptions.put(ext, Collections.emptySet());
        }
        for (String ext : AAC_FILE_EXTENSIONS) {
            allowedInputOptions.put(ext, Collections.emptySet());
            allowedOutputOptions.put(ext, new HashSet<>(Arrays.asList("lang", "period", "role")));
            mandatoryOutputOptions.put(ext, Collections.emptySet());
        }
        for (String ext : TEXT_FILE_EXTENSIONS) {
            allowedInputOptions.put(ext, Collections.emptySet());
            allowedOutputOptions.put(ext, new HashSet<>(Arrays.asList("lang", "period", "role")));
            mandatoryOutputOptions.put(ext, Collections.emptySet());
        }

    }

    public FileSelector(String filePattern) throws IOException {
        this(filePattern, Collections.emptyMap(), Collections.emptyMap());
    }

    public FileSelector(String filePattern, Map<String, String> inputOptions, Map<String, String> outputOptions) throws IOException {
        this.files = FileUtils.getFiles(new File(""), filePattern);
        if (files.isEmpty()) {
            throw new IllegalArgumentException("The file pattern " + filePattern + " doesn't yield any results.");
        }

        if (files.stream().map(f -> FilenameUtils.getExtension(f.getName()).toLowerCase()).distinct().count() > 1) {
            throw new IllegalArgumentException("The pattern " + filePattern + " includes multiple file types: " +
                    files.stream().map(f -> FilenameUtils.getExtension(f.getName()).toLowerCase()).distinct().collect(Collectors.joining(", ")) +
                    ". All files captured by the pattern need to have the same extension!");
        }


        for (File file : files) {
            final String fileExtension = FilenameUtils.getExtension(file.getName()).toLowerCase();
            FileSelector.validate(file, inputOptions, outputOptions);

            if (MP4_FILE_EXTENSIONS.contains(fileExtension)) {
                IsoFile isoFile = new IsoFile(file);
                List<TrackBox> trackBoxes = isoFile.getMovieBox().getBoxes(TrackBox.class);

                String type = inputOptions.get("type");
                String language = inputOptions.get("lang");
                String trackNo = inputOptions.get("trackNo");

                int no = 0;

                for (TrackBox trackBox : trackBoxes) {
                    boolean include = true;
                    String handler = trackBox.getMediaBox().getHandlerBox().getHandlerType();
                    if (language != null && !trackBox.getMediaBox().getMediaHeaderBox().getLanguage().equals(language)) {
                        LOGGER.info(file.getName() + ": Excluding track " + trackBox.getTrackHeaderBox().getTrackId() + " from processing as language is " + trackBox.getMediaBox().getMediaHeaderBox().getLanguage() + " but not " + language + ".");
                        include = false;
                    }
                    if (type != null) {

                        if (!handlerToType.computeIfAbsent(handler, e -> e).equals(type)) {
                            LOGGER.info(file.getName() + ": Excluding track " + trackBox.getTrackHeaderBox().getTrackId() + " from processing as type is " + handlerToType.computeIfAbsent(handler, e -> e) + " but not " + type + ".");
                            include = false;
                        }
                    }
                    if (trackNo != null) {
                        if (Integer.parseInt(trackNo) != no) {
                            LOGGER.info(file.getName() + ": Excluding track " + no + "("+ handler +") as only " + trackNo + " is included");
                            include = false;
                        }
                    }
                    if (include) {
                        LOGGER.info(file.getName() + ": Selecting track " + no + " (" + handler + ")");
                        tracks.add(new Mp4TrackImpl(trackBox.getTrackHeaderBox().getTrackId(), isoFile, new FileRandomAccessSourceImpl(new RandomAccessFile(file, "r")), file.getName()));
                    }
                }
                if (tracks.isEmpty()) {
                    throw new IllegalArgumentException("File Extension of " + file + " unknown");
                }
            } else if (H264_FILE_EXTENSIONS.contains(fileExtension)) {
                tracks.add(new H264TrackImpl(new FileDataSourceImpl(file)));
            } else if (H265_FILE_EXTENSIONS.contains(fileExtension)) {
                tracks.add(new H265TrackImpl(new FileDataSourceImpl(file)));
            } else if (AAC_FILE_EXTENSIONS.contains(fileExtension)) {
                AACTrackImpl a = new AACTrackImpl(new FileDataSourceImpl(file));
                String lang = "eng";
                if (!outputOptions.containsKey("lang")) {
                    lang = Locale.forLanguageTag(outputOptions.get("lang")).getISO3Language();
                }
                a.getTrackMetaData().setLanguage(lang);
                tracks.add(a);
            } else if (TEXT_FILE_EXTENSIONS.contains(fileExtension)) {
                tracks = TEXT_TRACKS;
                if (!outputOptions.containsKey("lang")) {
                    outputOptions.put("lang", getTextTrackLocale(file).toLanguageTag());
                }
            } else if (THUMB_FILE_EXTENSIONS.contains(fileExtension)) {
                tracks = THUMB_TRACKS;

                List<File> sortedFiles = new ArrayList<>(files);
                sortedFiles.sort(new WindowsExplorerComparator());
                this.files = sortedFiles;
                break;
            } else {
                throw new IllegalArgumentException("File Extension of " + file + " unknown");
            }
        }


    }

    public List<File> getFiles() {
        return files;
    }

    public List<Track> getSelectedTracks() {
        return tracks;
    }


    private static final Map<String, String> handlerToType = new HashMap<>();

    static {
        handlerToType.put("soun", "audio");
        handlerToType.put("vide", "video");
    }

    public static class WindowsExplorerComparator implements Comparator<File> {

        private static final Pattern splitPattern = Pattern.compile("\\d+|\\.|\\s");

        @Override
        public int compare(File f1, File f2) {
            String str1 = f1.getName();
            String str2 = f2.getName();
            Iterator<String> i1 = splitStringPreserveDelimiter(str1).iterator();
            Iterator<String> i2 = splitStringPreserveDelimiter(str2).iterator();
            while (true) {
                //Til here all is equal.
                if (!i1.hasNext() && !i2.hasNext()) {
                    return 0;
                }
                //first has no more parts -> comes first
                if (!i1.hasNext()) {
                    return -1;
                }
                //first has more parts than i2 -> comes after
                if (!i2.hasNext()) {
                    return 1;
                }

                String data1 = i1.next();
                String data2 = i2.next();
                int result;
                try {
                    //If both datas are numbers, then compare numbers
                    result = Long.compare(Long.parseLong(data1), Long.parseLong(data2));
                    //If numbers are equal than longer comes first
                    if (result == 0) {
                        result = -Integer.compare(data1.length(), data2.length());
                    }
                } catch (NumberFormatException ex) {
                    //compare text case insensitive
                    result = data1.compareToIgnoreCase(data2);
                }

                if (result != 0) {
                    return result;
                }
            }
        }

        private List<String> splitStringPreserveDelimiter(String str) {
            Matcher matcher = splitPattern.matcher(str);
            List<String> list = new ArrayList<>();
            int pos = 0;
            while (matcher.find()) {
                list.add(str.substring(pos, matcher.start()));
                list.add(matcher.group());
                pos = matcher.end();
            }
            list.add(str.substring(pos));
            return list;
        }
    }

    private static void validate(File file,  Map<String, String> inputOptions, Map<String, String> outputOptions){
        String fileExtension = FilenameUtils.getExtension(file.getName()).toLowerCase();

        if (!allowedInputOptions.containsKey(fileExtension)) {
            throw new IllegalArgumentException("File Extension of " + file + " is not supported.");
        }
        if (inputOptions.keySet().stream().anyMatch(s -> !allowedInputOptions.get(fileExtension).contains(s))) {
            String wrong = inputOptions.keySet().stream().filter(s -> !allowedInputOptions.get(fileExtension).contains(s)).collect(Collectors.joining(", "));
            throw new IllegalArgumentException("The input options " + wrong + " are invalid for input files with extension " + fileExtension + ". Valid input options are: " + allowedInputOptions.get(fileExtension));
        }


        if (!allowedOutputOptions.containsKey(fileExtension)) {
            throw new IllegalArgumentException("File Extension of " + file + " is not supported.");
        }
        if (outputOptions.keySet().stream().anyMatch(s -> !allowedOutputOptions.get(fileExtension).contains(s))) {
            String wrong = outputOptions.keySet().stream().filter(s -> !allowedOutputOptions.get(fileExtension).contains(s)).collect(Collectors.joining(", "));
            throw new IllegalArgumentException("The output options " + wrong + " are invalid for input files with extension " + fileExtension + ". Valid output options are: " + allowedOutputOptions.get(fileExtension));
        }

        if (!mandatoryOutputOptions.containsKey(fileExtension)) {
            throw new IllegalArgumentException("File Extension of " + file + " is not supported.");
        }
        if (mandatoryOutputOptions.get(fileExtension).stream().anyMatch(opt -> !outputOptions.containsKey(opt))) {
            String requiredOptions = String.join(", ", mandatoryOutputOptions.get(fileExtension));
            throw new IllegalArgumentException("The output options " + requiredOptions + " are required for input files with extension " + fileExtension);
        }

    }

}

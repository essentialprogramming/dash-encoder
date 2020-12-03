package com.dash.util;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.mp4parser.Box;
import org.mp4parser.Container;
import org.mp4parser.boxes.dolby.*;
import org.mp4parser.boxes.iso14496.part1.objectdescriptors.AudioSpecificConfig;
import org.mp4parser.boxes.iso14496.part1.objectdescriptors.DecoderConfigDescriptor;
import org.mp4parser.boxes.iso14496.part12.OriginalFormatBox;
import org.mp4parser.boxes.iso14496.part14.ESDescriptorBox;
import org.mp4parser.boxes.iso14496.part15.AvcConfigurationBox;
import org.mp4parser.boxes.iso14496.part15.HevcConfigurationBox;
import org.mp4parser.boxes.iso14496.part30.XMLSubtitleSampleEntry;
import org.mp4parser.boxes.sampleentry.AudioSampleEntry;
import org.mp4parser.boxes.sampleentry.SampleEntry;
import org.mp4parser.muxer.Track;
import org.mp4parser.tools.Hex;
import org.mp4parser.tools.Path;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gets the precise MIME type according to RFC6381.
 * http://tools.ietf.org/html/rfc6381
 */
public final class DashHelper {

    public static long getAudioSamplingRate(AudioSampleEntry e) {
        ESDescriptorBox esDescriptorBox = Path.getPath(e, "esds");
        if (esDescriptorBox != null) {
            final DecoderConfigDescriptor decoderConfigDescriptor = esDescriptorBox.getEsDescriptor().getDecoderConfigDescriptor();
            final AudioSpecificConfig audioSpecificConfig = decoderConfigDescriptor.getAudioSpecificInfo();
            if (audioSpecificConfig.getExtensionAudioObjectType() > 0 && audioSpecificConfig.sbrPresentFlag) {
                return audioSpecificConfig.getExtensionSamplingFrequency();
            } else {
                return audioSpecificConfig.getSamplingFrequency();
            }
        } else {
            return e.getSampleRate();
        }
    }


    /**
     * Gets the codec according to RFC 6381 from a <code>SampleEntry</code>.
     *
     * @param sampleEntry <code>SampleEntry</code> to id the codec.
     * @return codec according to RFC
     */
    public static String getRfc6381Codec(SampleEntry sampleEntry) {

        OriginalFormatBox formatBox = Path.getPath((Box) sampleEntry, "sinf/frma");
        String type;
        if (formatBox != null) {
            type = formatBox.getDataFormat();
        } else {
            type = sampleEntry.getType();
        }


        if ("avc1".equals(type) || "avc2".equals(type) || "avc3".equals(type) || "avc4".equals(type)) {
            AvcConfigurationBox avcConfigurationBox = Path.getPath((Box) sampleEntry, "avcC");
            List<ByteBuffer> spsbytes = avcConfigurationBox.getSequenceParameterSets();
            byte[] CodecInit = new byte[3];
            CodecInit[0] = spsbytes.get(0).get(1);
            CodecInit[1] = spsbytes.get(0).get(2);
            CodecInit[2] = spsbytes.get(0).get(3);
            return (type + "." + Hex.encodeHex(CodecInit)).toLowerCase();
        } else if (type.equals("mp4a")) {
            ESDescriptorBox esDescriptorBox = Path.getPath((Box) sampleEntry, "esds");
            if (esDescriptorBox == null) {
                esDescriptorBox = Path.getPath((Box) sampleEntry, "..../esds"); // Apple does weird things
            }
            final DecoderConfigDescriptor decoderConfigDescriptor = esDescriptorBox.getEsDescriptor().getDecoderConfigDescriptor();
            final AudioSpecificConfig audioSpecificConfig = decoderConfigDescriptor.getAudioSpecificInfo();
            if (audioSpecificConfig != null && audioSpecificConfig.sbrPresentFlag && !audioSpecificConfig.psPresentFlag) {
                return "mp4a.40.5";
            } else if (audioSpecificConfig != null && audioSpecificConfig.sbrPresentFlag && audioSpecificConfig.psPresentFlag) {
                return "mp4a.40.29";
            } else {
                return "mp4a.40.2";
            }
        } else if (type.equals("mp4v")) {
            ESDescriptorBox esDescriptorBox = Path.getPath((Box) sampleEntry, "esds");
            if (esDescriptorBox == null) {
                esDescriptorBox = Path.getPath((Box) sampleEntry, "..../esds"); // Apple does weird things
            }
            if (esDescriptorBox.getEsDescriptor().getDecoderConfigDescriptor().getObjectTypeIndication() == 0x6C) {
                return "mp4v." +
                        Integer.toHexString(esDescriptorBox.getEsDescriptor().getDecoderConfigDescriptor().getObjectTypeIndication());
            } else {
                throw new RuntimeException("I don't know how to construct codec for mp4v with OTI " +
                        esDescriptorBox.getEsDescriptor().getDecoderConfigDescriptor().getObjectTypeIndication()
                );
            }
        } else if (type.equals("dtsl") || type.equals("dtsl") || type.equals("dtse")) {
            return type;
        } else if (type.equals("ec-3") || type.equals("ac-3") || type.equals("mlpa")) {
            return type;
        } else if (type.equals("hev1") || type.equals("hvc1")) {
            int c;
            HevcConfigurationBox hvcc = Path.getPath((Box) sampleEntry, "hvcC");

            String codec = type + ".";

            if (hvcc.getGeneral_profile_space() == 1) {
                codec += "A";
            } else if (hvcc.getGeneral_profile_space() == 2) {
                codec += "B";
            } else if (hvcc.getGeneral_profile_space() == 3) {
                codec += "C";
            }
            //profile idc encoded as a decimal number
            codec += hvcc.getGeneral_profile_idc();
            //general profile compatibility flags: hexa, bit-reversed
            {
                long val = hvcc.getGeneral_profile_compatibility_flags();
                long i, res = 0;
                for (i = 0; i < 31; i++) {
                    res |= val & 1;
                    res <<= 1;
                    val >>= 1;
                }
                res |= val & 1;
                codec += ".";
                codec += Long.toHexString(res);
            }

            if (hvcc.isGeneral_tier_flag()) {
                codec += ".H";
            } else {
                codec += ".L";
            }
            codec += hvcc.getGeneral_level_idc();


            long _general_constraint_indicator_flags = hvcc.getGeneral_constraint_indicator_flags();
            if (hvcc.getHevcDecoderConfigurationRecord().isFrame_only_constraint_flag()) {
                _general_constraint_indicator_flags |= 1l << 47;
            }
            if (hvcc.getHevcDecoderConfigurationRecord().isNon_packed_constraint_flag()) {
                _general_constraint_indicator_flags |= 1l << 46;
            }
            if (hvcc.getHevcDecoderConfigurationRecord().isInterlaced_source_flag()) {
                _general_constraint_indicator_flags |= 1l << 45;
            }
            if (hvcc.getHevcDecoderConfigurationRecord().isProgressive_source_flag()) {
                _general_constraint_indicator_flags |= 1l << 44;
            }

            codec += "." + hexByte(_general_constraint_indicator_flags >> 40);


            if ((_general_constraint_indicator_flags & 0xFFFFFFFFFFL) > 0) {
                codec += "." + hexByte(_general_constraint_indicator_flags >> 32);

                if ((_general_constraint_indicator_flags & 0xFFFFFFFFL) > 0) {
                    codec += "." + hexByte(_general_constraint_indicator_flags >> 24);
                    if ((_general_constraint_indicator_flags & 0xFFFFFFL) > 0) {
                        codec += "." + hexByte(_general_constraint_indicator_flags >> 16);
                        if (((_general_constraint_indicator_flags & 0xFFFFL)) > 0) {
                            codec += "." + hexByte(_general_constraint_indicator_flags >> 8);
                            if (((_general_constraint_indicator_flags & 0xFFL)) > 0) {
                                codec += "." + hexByte(_general_constraint_indicator_flags);
                            }
                        }
                    }
                }
            }

            return codec;
        } else if (type.equals("stpp")) {
            XMLSubtitleSampleEntry stpp = (XMLSubtitleSampleEntry) sampleEntry;
            if (stpp.getSchemaLocation().contains("cff-tt-text-ttaf1-dfxp")) {
                return "cfft";
            } else if (stpp.getSchemaLocation().contains("cff-tt-image-ttaf1-dfxp")) {
                return "cffi";
            } else {
                return "stpp";
            }

        } else if (type.equals("dvav") || type.equals("dva1") || type.equals("dvhe") || type.equals("dvh1")) {
            DoViConfigurationBox dvcC = Path.getPath((Box) sampleEntry, "dvcC");
            return type + String.format(".%02d.%02d", dvcC.getDvProfile(), dvcC.getDvLevel());
        } else {
            return null;
        }

    }

    static String hexByte(long l) {
        return Hex.encodeHex(new byte[]{(byte) (l & 0xFF)});
    }

    public static String getFormat(Track track) {
        List<SampleEntry> sampleEntries = track.getSampleEntries();
        String format = null;
        for (SampleEntry sampleEntry : sampleEntries) {

            OriginalFormatBox formatBox = Path.getPath((Container) sampleEntry, "sinf/frma");
            if (formatBox != null) {
                if (format == null || format.equals(formatBox.getDataFormat())) {
                    format = formatBox.getDataFormat();
                } else {
                    throw new RuntimeException("can't determine format of track");
                }

            } else {
                if (format == null || format.equals(sampleEntry.getType())) {
                    format = sampleEntry.getType();
                } else {
                    throw new RuntimeException("can't determine format of track");
                }
            }
        }
        return format;
    }



    public static Locale getTextTrackLocale(File textTrack) throws IOException {
        Pattern patternFilenameIncludesLanguage = Pattern.compile(".*[-_](.+)$");
        String ext = FilenameUtils.getExtension(textTrack.getName());
        String basename = FilenameUtils.getBaseName(textTrack.getName());
        if (ext.equals("vtt")) {
            Matcher m = patternFilenameIncludesLanguage.matcher(basename);
            if (m.matches()) {
                return Locale.forLanguageTag(m.group(1));
            } else {
                throw new IOException("Cannot determine language of " + textTrack + " please use the pattern filename-[language-tag].vtt");
            }
        } else if (ext.equals("xml") || ext.equals("dfxp") || ext.equals("ttml")) {
            DocumentBuilderFactory builderFactory =
                    DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder builder = builderFactory.newDocumentBuilder();

                String xml = FileUtils.readFileToString(textTrack);
                Document xmlDocument = builder.parse(new ByteArrayInputStream(xml.getBytes()));

                String lang = xmlDocument.getDocumentElement().getAttribute("xml:lang");
                NodeList nl = xmlDocument.getDocumentElement().getElementsByTagName("div");
                for (int i = 0; i < nl.getLength(); i++) {
                    Attr langInDiv = (Attr) nl.item(i).getAttributes().getNamedItem("xml:lang");
                    if (langInDiv != null) {
                        lang = langInDiv.getValue();
                    }

                }
                if (lang != null) {
                    return Locale.forLanguageTag(lang);
                } else {
                    Matcher m2 = patternFilenameIncludesLanguage.matcher(basename);
                    if (m2.matches()) {
                        return Locale.forLanguageTag(m2.group(1));
                    } else {
                        throw new IOException("Cannot determine language of " + textTrack + " please use either the xml:lang attribute or a filename pattern like filename-[language-tag].[xml|dfxp]");
                    }
                }
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
                throw new IOException("Cannot instantiate XML parser to determine textTrack language");
            } catch (SAXException e) {
                e.printStackTrace();
                throw new IOException("Cannot parse XML to extract text track's language");
            }


        } else {
            throw new IOException("Unknown subtitle format in " + textTrack);
        }


    }

}

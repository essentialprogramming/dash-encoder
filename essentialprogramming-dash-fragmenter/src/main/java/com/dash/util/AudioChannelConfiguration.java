package com.dash.util;

import org.mp4parser.boxes.dolby.AC3SpecificBox;
import org.mp4parser.boxes.dolby.DTSSpecificBox;
import org.mp4parser.boxes.dolby.EC3SpecificBox;
import org.mp4parser.boxes.dolby.MLPSpecificBox;
import org.mp4parser.boxes.iso14496.part1.objectdescriptors.AudioSpecificConfig;
import org.mp4parser.boxes.iso14496.part1.objectdescriptors.DecoderConfigDescriptor;
import org.mp4parser.boxes.iso14496.part14.ESDescriptorBox;
import org.mp4parser.boxes.sampleentry.AudioSampleEntry;
import org.mp4parser.tools.Hex;
import org.mp4parser.tools.Path;

import java.util.List;

public class AudioChannelConfiguration {
    public String schemeIdUri = "";
    public String value = "";

    public static AudioChannelConfiguration getChannelConfiguration(AudioSampleEntry e) {
        DTSSpecificBox ddts = Path.getPath(e, "ddts");
        if (ddts != null) {
            return getDTSChannelConfig(e, ddts);
        }
        MLPSpecificBox dmlp = Path.getPath(e, "dmlp");
        if (dmlp != null) {
            return null; // getMLPChannelConfig(e, dmlp);
        }
        ESDescriptorBox esds = Path.getPath(e, "esds");
        if (esds != null) {
            return getAACChannelConfig(e, esds);
        }
        esds = Path.getPath(e, "..../esds"); // Apple does weird things
        if (esds != null) {
            return getAACChannelConfig(e, esds);
        }
        AC3SpecificBox dac3 = Path.getPath(e, "dac3");
        if (dac3 != null) {
            return getAC3ChannelConfig(e, dac3);
        }
        EC3SpecificBox dec3 = Path.getPath(e, "dec3");
        if (dec3 != null) {
            return getEC3ChannelConfig(e, dec3);
        }

        return null;
    }

    private static AudioChannelConfiguration getEC3ChannelConfig(AudioSampleEntry e, EC3SpecificBox dec3) {
        final List<EC3SpecificBox.Entry> ec3SpecificBoxEntries = dec3.getEntries();
        int audioChannelValue = 0;
        for (EC3SpecificBox.Entry ec3SpecificBoxEntry : ec3SpecificBoxEntries) {
            audioChannelValue |= getDolbyAudioChannelValue(ec3SpecificBoxEntry.acmod, ec3SpecificBoxEntry.lfeon, ec3SpecificBoxEntry.chan_loc);
        }
        AudioChannelConfiguration cc = new AudioChannelConfiguration();
        cc.value = Hex.encodeHex(new byte[]{(byte) ((audioChannelValue >> 8) & 0xFF), (byte) (audioChannelValue & 0xFF)});
        cc.schemeIdUri = "urn:dolby:dash:audio_channel_configuration:2011";
        return cc;
    }

    private static AudioChannelConfiguration getAC3ChannelConfig(AudioSampleEntry e, AC3SpecificBox dac3) {
        AudioChannelConfiguration cc = new AudioChannelConfiguration();
        int audioChannelValue = getDolbyAudioChannelValue(dac3.getAcmod(), dac3.getLfeon(), 0);
        cc.value = Hex.encodeHex(new byte[]{(byte) ((audioChannelValue >> 8) & 0xFF), (byte) (audioChannelValue & 0xFF)});
        cc.schemeIdUri = "urn:dolby:dash:audio_channel_configuration:2011";
        return cc;
    }

    private static int getDolbyAudioChannelValue(int acmod, int lfeon, int chan_loc) {
        int audioChannelValue;
        switch (acmod) {
            case 0:
                audioChannelValue = 0xA000;
                break;
            case 1:
                audioChannelValue = 0x4000;
                break;
            case 2:
                audioChannelValue = 0xA000;
                break;
            case 3:
                audioChannelValue = 0xE000;
                break;
            case 4:
                audioChannelValue = 0xA100;
                break;
            case 5:
                audioChannelValue = 0xE100;
                break;
            case 6:
                audioChannelValue = 0xB800;
                break;
            case 7:
                audioChannelValue = 0xF800;
                break;
            default:
                throw new RuntimeException("Unexpected acmod " + acmod);
        }
        if (lfeon == 1) {
            audioChannelValue += 1;
        }
        int[] chanLoc2audioChannelConfiguration = new int[]{
                0b0000010000000000, // 0 - Lc/Rc
                0b0000001000000000, // 1 - Lls/Lrs
                0b0000000100000000, // 2 - Cs
                0b0000000010000000, // 3 - Ts
                0b0000000001000000, // 4 - Lsd/Rsd
                0b0000000000100000, // 5 - Lw/Rw
                0b0000000000010000, // 6 - Lvh/Rvh
                0b0000000000001000, // 7 - Cvh
                0b0000000000000010, // 8 - LFE2
        };
        for (int i = 0; i < chanLoc2audioChannelConfiguration.length; i++) {
            if ((chan_loc & (0b000000001 << i)) > 0) {
                audioChannelValue |= chanLoc2audioChannelConfiguration[i];
            }
        }

        return audioChannelValue;
    }

    private static AudioChannelConfiguration getAACChannelConfig(AudioSampleEntry e, ESDescriptorBox esds) {

        final DecoderConfigDescriptor decoderConfigDescriptor = esds.getEsDescriptor().getDecoderConfigDescriptor();
        final AudioSpecificConfig audioSpecificConfig = decoderConfigDescriptor.getAudioSpecificInfo();
        AudioChannelConfiguration cc = new AudioChannelConfiguration();
        cc.schemeIdUri = "urn:mpeg:dash:23003:3:audio_channel_configuration:2011";
        cc.value = "2";
        if (audioSpecificConfig != null && audioSpecificConfig.getChannelConfiguration() > 2) {
            // in case of mono let's assume stereo as it will be Parametric Stereo in most cases.
            cc.value = String.valueOf(audioSpecificConfig.getChannelConfiguration());
        }
        return cc;
    }

    private static AudioChannelConfiguration getDTSChannelConfig(AudioSampleEntry e, DTSSpecificBox ddts) {
        AudioChannelConfiguration cc = new AudioChannelConfiguration();
        cc.value = Integer.toString(getNumChannels(ddts));
        cc.schemeIdUri = "urn:dts:dash:audio_channel_configuration:2012";
        return cc;
    }

    private static int getNumChannels(DTSSpecificBox dtsSpecificBox) {
        final int channelLayout = dtsSpecificBox.getChannelLayout();
        int numChannels = 0;
        int dwChannelMask = 0;
        if ((channelLayout & 0x0001) == 0x0001) {
            //0001h Center in front of listener 1
            numChannels += 1;
            dwChannelMask |= 0x00000004; //SPEAKER_FRONT_CENTER
        }
        if ((channelLayout & 0x0002) == 0x0002) {
            //0002h Left/Right in front 2
            numChannels += 2;
            dwChannelMask |= 0x00000001; //SPEAKER_FRONT_LEFT
            dwChannelMask |= 0x00000002; //SPEAKER_FRONT_RIGHT
        }
        if ((channelLayout & 0x0004) == 0x0004) {
            //0004h Left/Right surround on side in rear 2
            numChannels += 2;
            //* if Lss, Rss exist, then this position is equivalent to Lsr, Rsr respectively
            dwChannelMask |= 0x00000010; //SPEAKER_BACK_LEFT
            dwChannelMask |= 0x00000020; //SPEAKER_BACK_RIGHT
        }
        if ((channelLayout & 0x0008) == 0x0008) {
            //0008h Low frequency effects subwoofer 1
            numChannels += 1;
            dwChannelMask |= 0x00000008; //SPEAKER_LOW_FREQUENCY
        }
        if ((channelLayout & 0x0010) == 0x0010) {
            //0010h Center surround in rear 1
            numChannels += 1;
            dwChannelMask |= 0x00000100; //SPEAKER_BACK_CENTER
        }
        if ((channelLayout & 0x0020) == 0x0020) {
            //0020h Left/Right height in front 2
            numChannels += 2;
            dwChannelMask |= 0x00001000; //SPEAKER_TOP_FRONT_LEFT
            dwChannelMask |= 0x00004000; //SPEAKER_TOP_FRONT_RIGHT
        }
        if ((channelLayout & 0x0040) == 0x0040) {
            //0040h Left/Right surround in rear 2
            numChannels += 2;
            dwChannelMask |= 0x00000010; //SPEAKER_BACK_LEFT
            dwChannelMask |= 0x00000020; //SPEAKER_BACK_RIGHT
        }
        if ((channelLayout & 0x0080) == 0x0080) {
            //0080h Center Height in front 1
            numChannels += 1;
            dwChannelMask |= 0x00002000; //SPEAKER_TOP_FRONT_CENTER
        }
        if ((channelLayout & 0x0100) == 0x0100) {
            //0100h Over the listener’s head 1
            numChannels += 1;
            dwChannelMask |= 0x00000800; //SPEAKER_TOP_CENTER
        }
        if ((channelLayout & 0x0200) == 0x0200) {
            //0200h Between left/right and center in front 2
            numChannels += 2;
            dwChannelMask |= 0x00000040; //SPEAKER_FRONT_LEFT_OF_CENTER
            dwChannelMask |= 0x00000080; //SPEAKER_FRONT_RIGHT_OF_CENTER
        }
        if ((channelLayout & 0x0400) == 0x0400) {
            //0400h Left/Right on side in front 2
            numChannels += 2;
            dwChannelMask |= 0x00000200; //SPEAKER_SIDE_LEFT
            dwChannelMask |= 0x00000400; //SPEAKER_SIDE_RIGHT
        }
        if ((channelLayout & 0x0800) == 0x0800) {
            //0800h Left/Right surround on side 2
            numChannels += 2;
            //* if Lss, Rss exist, then this position is equivalent to Lsr, Rsr respectively
            dwChannelMask |= 0x00000010; //SPEAKER_BACK_LEFT
            dwChannelMask |= 0x00000020; //SPEAKER_BACK_RIGHT
        }
        if ((channelLayout & 0x1000) == 0x1000) {
            //1000h Second low frequency effects subwoofer 1
            numChannels += 1;
            dwChannelMask |= 0x00000008; //SPEAKER_LOW_FREQUENCY
        }
        if ((channelLayout & 0x2000) == 0x2000) {
            //2000h Left/Right height on side 2
            numChannels += 2;
            dwChannelMask |= 0x00000010; //SPEAKER_BACK_LEFT
            dwChannelMask |= 0x00000020; //SPEAKER_BACK_RIGHT
        }
        if ((channelLayout & 0x4000) == 0x4000) {
            //4000h Center height in rear 1
            numChannels += 1;
            dwChannelMask |= 0x00010000; //SPEAKER_TOP_BACK_CENTER
        }
        if ((channelLayout & 0x8000) == 0x8000) {
            //8000h Left/Right height in rear 2
            numChannels += 2;
            dwChannelMask |= 0x00008000; //SPEAKER_TOP_BACK_LEFT
            dwChannelMask |= 0x00020000; //SPEAKER_TOP_BACK_RIGHT
        }
        if ((channelLayout & 0x10000) == 0x10000) {
            //10000h Center below in front
            numChannels += 1;
        }
        if ((channelLayout & 0x20000) == 0x20000) {
            //20000h Left/Right below in front
            numChannels += 2;
        }
        return numChannels;
    }
}